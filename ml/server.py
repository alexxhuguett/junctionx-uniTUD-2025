"""
Simple HTTP server to serve ride rating predictions.

Endpoints:
- GET /health -> {status: ok}
- GET /prediction/<ride_id> -> {ride_id, rating}
- GET /prediction/top/<n> -> [{ride_id, rating, ...}, ...]

Run (from project root):
  python3 -m pip install flask
  PYTHONPATH=junctionx-uniTUD-2025 python3 -m ml.server \
    --excel junctionx-uniTUD-2025/ml/data/uber_hackathon_v2_mock_data.xlsx \
    --model junctionx-uniTUD-2025/ml/artifacts/model.pkl \
    --host 0.0.0.0 --port 8000
"""

from __future__ import annotations

import argparse
from dataclasses import asdict, dataclass
from pathlib import Path
from typing import Any, Dict, List, Optional

import numpy as np
import pandas as pd

try:
    from flask import Flask, jsonify, abort
except Exception as e:  # pragma: no cover
    raise SystemExit("Flask is required. Install with: python3 -m pip install flask")

# requests is optional; used to fetch external features from Java backend if configured
try:  # pragma: no cover
    import requests  # type: ignore
except Exception:
    requests = None  # type: ignore

from .src.data_loader import load_and_prepare
from .src.features import finalize_feature_table
from .src.model import RideScoringModel, NUMERIC_FEATURES, CATEGORICAL_FEATURES
from .src.simulation import simulate_driver_day


app = Flask(__name__)


@dataclass
class ServingState:
    excel_path: Path
    model_path: Path
    rides_base: Optional[str]
    model: RideScoringModel
    df: pd.DataFrame
    X: pd.DataFrame
    preds: np.ndarray
    index_by_ride: Dict[str, int]


STATE: Optional[ServingState] = None


def load_state(excel_path: Path, model_path: Path, rides_base: Optional[str]) -> ServingState:
    df = load_and_prepare(str(excel_path))
    X = finalize_feature_table(df)
    model = RideScoringModel.load(model_path)
    preds = model.predict(X)
    # Build fast ride_id index
    ids = df["ride_id"].astype(str).tolist()
    index_by_ride = {rid: i for i, rid in enumerate(ids)}
    return ServingState(
        excel_path=excel_path,
        model_path=model_path,
        rides_base=rides_base,
        model=model,
        df=df,
        X=X,
        preds=preds,
        index_by_ride=index_by_ride,
    )


def _fetch_external_features(ride_id: str) -> Optional[dict]:
    """Try to fetch a feature row from an external rides service.

    Expects a GET endpoint that returns a JSON object with model-ready columns.
    Returns None if not configured, requests is unavailable, or not found.
    """
    if STATE is None or not STATE.rides_base:
        return None
    if requests is None:
        return None
    base = STATE.rides_base.rstrip("/")
    url = f"{base}/{ride_id}"
    try:
        resp = requests.get(url, timeout=2.5)
        if resp.status_code == 200:
            data = resp.json()
            # Some services may return null
            if data is None:
                return None
            if not isinstance(data, dict):
                return None
            return data
        return None
    except Exception:
        return None


@app.route("/health", methods=["GET"])
def health() -> Any:
    return jsonify({"status": "ok"})


@app.route("/prediction/<ride_id>", methods=["GET"])
def predict_one(ride_id: str) -> Any:
    global STATE
    if STATE is None:
        abort(503, description="Model not loaded")
    # 1) Try external features (Java backend) if configured
    features = _fetch_external_features(str(ride_id))
    if features is not None:
        # Build one-row DataFrame with expected columns
        cols = CATEGORICAL_FEATURES + NUMERIC_FEATURES
        row = {c: features.get(c) for c in cols}
        X_one = pd.DataFrame([row], columns=cols)
        pred = STATE.model.predict(X_one)
        rating = float(pred[0])
        return jsonify({"ride_id": ride_id, "rating": rating, "source": "external"})

    # 2) Fallback to preloaded Excel rows
    idx = STATE.index_by_ride.get(str(ride_id))
    if idx is None:
        abort(404, description="ride_id not found")
    rating = float(STATE.preds[idx])
    return jsonify({"ride_id": ride_id, "rating": rating, "source": "excel"})


def _calc_idle_and_rest_minutes(seq: pd.DataFrame) -> Dict[str, float]:
    """Compute idle and rest minutes from a sorted sequence of rides.

    - idle_minutes: sum of positive gaps between prior end_time and next start_time
    - rest_minutes: sum of gaps that are >= 15 minutes (the team's definition)
    """
    if seq is None or seq.empty:
        return {"idle_minutes": 0.0, "rest_minutes": 0.0}
    seq = seq.sort_values("start_time")
    prev_end = seq["end_time"].shift(1)
    gaps = (seq["start_time"] - prev_end).dt.total_seconds() / 60.0
    gaps = gaps.fillna(0.0)
    idle = float(gaps.clip(lower=0).sum())
    rest = float(gaps[gaps >= 15.0].sum())
    return {"idle_minutes": idle, "rest_minutes": rest}


@app.route("/api/driver-days", methods=["GET"])
def list_driver_days() -> Any:
    """Return driver/day pairs with counts to help fill the UI.

    Query params:
    - limit (int, optional): max number of rows (default 100)
    - min_rides (int, optional): minimum rides per day (default 1)
    """
    global STATE
    if STATE is None:
        abort(503, description="Model not loaded")
    from flask import request
    limit = int(request.args.get("limit", 100))
    min_rides = int(request.args.get("min_rides", 1))
    df = STATE.df.copy()
    grp = (
        df.groupby(["driver_id", df["start_time"].dt.date])
        .size()
        .reset_index(name="rides")
        .query("rides >= @min_rides")
        .sort_values(["rides", "driver_id"], ascending=[False, True])
        .head(limit)
    )
    renamed = grp.rename(columns={"start_time": "date"})
    out = [
        {"driver_id": str(r["driver_id"]), "date": str(r["date"]), "rides": int(r["rides"])}
        for _, r in renamed.iterrows()
    ]
    return jsonify(out)


@app.route("/api/simulation", methods=["GET"])
def api_simulation() -> Any:
    """Run the 30-minute window simulation for a driver and date and return metrics.

    Query params:
    - driver_id (required)
    - date (YYYY-MM-DD, required)
    - window (minutes, optional, default 30)
    """
    global STATE
    if STATE is None:
        abort(503, description="Model not loaded")
    from flask import request
    driver_id = request.args.get("driver_id")
    datestr = request.args.get("date")
    window = int(request.args.get("window", 30))
    if not driver_id or not datestr:
        abort(400, description="driver_id and date are required")
    try:
        target_day = pd.to_datetime(datestr).date()
    except Exception:
        abort(400, description="invalid date; expected YYYY-MM-DD")

    res = simulate_driver_day(STATE.df.copy(), STATE.model, driver_id=str(driver_id), day=target_day, window_mins=window)
    # Compute idle/rest minutes
    actual_stats = _calc_idle_and_rest_minutes(res.details_actual)
    simulated_stats = _calc_idle_and_rest_minutes(res.details_simulated)
    # Compute earnings per hour based on total driving time
    def _eph(earn: float, df: pd.DataFrame) -> float:
        mins = float(df.get("duration_mins", pd.Series(dtype=float)).fillna(0).sum())
        hrs = mins / 60.0
        return float(earn / hrs) if hrs > 0 else 0.0
    eph_a = _eph(res.actual_earnings, res.details_actual)
    eph_s = _eph(res.simulated_earnings, res.details_simulated)

    payload = {
        "driver_id": res.driver_id,
        "date": str(res.day),
        "actual": {
            "rides": res.actual_count,
            "earnings": res.actual_earnings,
            "eph": eph_a,
            **actual_stats,
        },
        "simulated": {
            "rides": res.simulated_count,
            "earnings": res.simulated_earnings,
            "eph": eph_s,
            **simulated_stats,
        },
        "details_simulated": res.details_simulated.head(20).to_dict(orient="records"),
    }
    return jsonify(payload)


@app.route("/simulation", methods=["GET"])
def simulation_page() -> Any:
    """Serve the advanced simulation viewer page."""
    return """<!DOCTYPE html>
<html lang=\"en\"><head><meta charset=\"utf-8\"/><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"/>\n<title>Simulation Viewer</title>\n<script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>\n<style>:root{--bg:#0e1116;--card:#151a22;--text:#e8eef8;--muted:#9ab;--ok:#2bd67b;--idle:#8a93a6;--rest:#6aa7ff;--trip:#2bd67b;--bad:#ff6b6b;--accent:#7aa2f7}*{box-sizing:border-box} body{margin:0;font-family:ui-sans-serif,system-ui,Segoe UI,Roboto,Arial;background:var(--bg);color:var(--text)} .wrap{max-width:1100px;margin:28px auto;padding:0 16px} .title{font-size:22px;font-weight:700;margin:0 0 16px} .row{display:flex;gap:16px;flex-wrap:wrap} .card{background:var(--card);border:1px solid #1f2631;border-radius:14px;padding:14px} .card.small{flex:1 1 220px} .card.full{flex:1 1 100%} .card.half{flex:1 1 520px} label{font-size:12px;color:var(--muted);display:block;margin-bottom:6px} input,select,button{border-radius:10px;border:1px solid #283040;background:#121721;color:var(--text);padding:10px 12px;font-size:14px;width:100%} .form-row{display:grid;grid-template-columns:repeat(5,1fr);gap:12px} .btn{background:var(--accent);border:0;color:white;font-weight:600;cursor:pointer} .btn:hover{opacity:.95} .kpi{font-size:20px;font-weight:700} .kpi small{display:block;font-size:12px;color:var(--muted);font-weight:500;margin-top:4px} .delta-pos{color:var(--ok)} .delta-neg{color:var(--bad)} .delta-zero{color:var(--muted)} .timeline{position:relative;height:36px;background:#10151d;border:1px solid #1f2631;border-radius:10px;overflow:hidden} .tl-bar{position:absolute;top:0;bottom:0} .tl-trip{background:var(--trip)} .tl-idle{background:var(--idle)} .tl-rest{background:var(--rest)} .legend{display:flex;gap:10px;align-items:center;font-size:12px;color:var(--muted)} .legend .swatch{width:12px;height:12px;border-radius:3px;display:inline-block;margin-right:6px} .muted{color:var(--muted)} .mono{font-family:ui-monospace,SFMono-Regular,Menlo,monospace} .foot{font-size:12px;color:var(--muted);margin-top:12px}</style></head><body>\n<div class=\"wrap\"> <div class=\"title\">Alternative Day Simulation — Quick Viewer</div> <div class=\"card full\"> <form id=\"frm\" class=\"form\"><div class=\"form-row\"> <div><label>Driver ID</label><input id=\"driverId\" placeholder=\"E10049\" required></div> <div><label>Date (YYYY-MM-DD)</label><input id=\"date\" placeholder=\"2023-03-17\" required></div> <div><label>Tolerance (min)</label><input id=\"tol\" type=\"number\" min=\"0\" value=\"5\"></div> <div><label>Lookahead (min)</label><input id=\"lookahead\" type=\"number\" min=\"5\" value=\"30\"></div> <div style=\"display:flex;align-items:flex-end;\"><button class=\"btn\" type=\"submit\">Run</button></div> </div></form> <div id=\"status\" class=\"foot\"></div> </div> <div class=\"row\" style=\"margin-top:14px\"> <div class=\"card half\"><canvas id=\"cmp\" height=\"160\"></canvas></div> <div class=\"card half\"> <div class=\"row\"> <div class=\"card small\" style=\"flex:1\"><div class=\"kpi\" id=\"kE\"></div><small>Earnings Δ (abs / %)</small></div> <div class=\"card small\" style=\"flex:1\"><div class=\"kpi\" id=\"kD\"></div><small>Drive Δ (min / %)</small></div> <div class=\"card small\" style=\"flex:1\"><div class=\"kpi\" id=\"kI\"></div><small>EPH Δ (€/h / %)</small></div> <div class=\"card small\" style=\"flex:1\"><div class=\"kpi\" id=\"kR\"></div><small>Rest Δ (min / %)</small></div> </div> <div class=\"foot\" id=\"meta\"></div> </div> </div> <div class=\"card full\" style=\"margin-top:14px\"> <div class=\"legend\" style=\"margin-bottom:8px\"><span class=\"swatch\" style=\"background:var(--trip)\"></span> Trip <span class=\"swatch\" style=\"background:var(--idle)\"></span> Short Gap <span class=\"swatch\" style=\"background:var(--rest)\"></span> Rest <span class=\"muted\" style=\"margin-left:auto\">Timeline (simulated day)</span></div> <div id=\"timeline\" class=\"timeline\"></div> <div class=\"foot mono\" id=\"tlRange\"></div> </div> <div class=\"card full\" style=\"margin-top:14px\"> <details><summary>Raw JSON (debug)</summary><pre id=\"raw\" class=\"mono\" style=\"white-space:pre-wrap; font-size:12px;\"></pre></details> </div> </div>\n<script>let chart;function fmtPct(x){if(x==null||Number.isNaN(x))return'—';const s=(x>=0?'+':'')+x.toFixed(1)+'%';const cls=x>0?'delta-pos':(x<0?'delta-neg':'delta-zero');return `<span class=\"${cls}\">${s}</span>`;} function fmtCurLines(x){ if (x==null) return ['—','']; const s=(x>=0?'+':'')+x.toFixed(2); const cls=x>0?'delta-pos':(x<0?'delta-neg':'delta-zero'); return [`<span class=\"${cls}\">${s}</span>`,`<span class=\"${cls}\">€</span>`]; } function fmtMin(x){if(x==null)return'—';const s=(x>=0?'+':'')+x.toFixed(0)+' min';const cls=x>0?'delta-pos':(x<0?'delta-neg':'delta-zero');return `<span class=\"${cls}\">${s}</span>`;} function setKpi(el,abs,pct,isMoney=false,equal=false){ if(equal){el.innerHTML = "=<br/>="; return;} if(isMoney){ const [l1, sym] = fmtCurLines(abs); const p = fmtPct(pct); el.innerHTML = `${l1}<br/>${sym}<br/>${p}`; } else { const absS = fmtMin(abs); const p = fmtPct(pct); el.innerHTML = `${absS}<br/>${p}`; } } function drawChart(b,s){const labels=['Earnings (€)','Earnings/h (€/h)','Drive (min)','Rest (min)'];const base=[b.earnings,b.eph,b.driveMins,b.restMins];const sim=[s.earnings,s.eph,s.driveMins,s.restMins];const data={labels,datasets:[{label:'Baseline',data:base},{label:'Simulated',data:sim}]};const options={responsive:true,plugins:{legend:{labels:{color:'#cbd5e1'}}},scales:{x:{ticks:{color:'#94a3b8'},grid:{color:'#1f2937'}},y:{ticks:{color:'#94a3b8'},grid:{color:'#1f2937'}}}};const ctx=document.getElementById('cmp').getContext('2d');if(chart)chart.destroy();chart=new Chart(ctx,{type:'bar',data,options});} function drawTimeline(timeline,shiftStart,shiftEnd){const container=document.getElementById('timeline');container.innerHTML='';if(!timeline||timeline.length===0){container.innerHTML='<div class=\"muted\" style=\"padding:8px\">No timeline events.</div>';return;}const start=new Date(shiftStart).getTime();const end=new Date(shiftEnd).getTime();const span=Math.max(1,end-start);for(const ev of timeline){const s=new Date(ev.start).getTime();const e=new Date(ev.end).getTime();const left=Math.max(0,Math.min(100,((s-start)/span)*100));const width=Math.max(0.5,Math.min(100-left,((e-s)/span)*100));const div=document.createElement('div');div.className='tl-bar '+(ev.type==='trip'?'tl-trip':(ev.type==='rest'?'tl-rest':'tl-idle'));div.style.left=left+'%';div.style.width=width+'%';div.title=`${ev.type.toUpperCase()}  ${ev.start} → ${ev.end}`+(ev.rideId?`\n${ev.rideId}`:'');container.appendChild(div);}document.getElementById('tlRange').textContent=`Shift: ${new Date(shiftStart).toISOString()} → ${new Date(shiftEnd).toISOString()}`;} async function runSim(params){const qs=new URLSearchParams(params).toString();const url=`/simulate?${qs}`;const t0=performance.now();const r=await fetch(url);if(!r.ok)throw new Error(`HTTP ${r.status}`);const json=await r.json();const t1=performance.now();document.getElementById('raw').textContent=JSON.stringify(json,null,2);const b=json.baseline,s=json.simulated,imp=json.improvements;drawChart(b,s);setKpi(document.getElementById('kE'),imp.earningsAbs,imp.earningsPct,true);setKpi(document.getElementById('kD'),imp.driveMinsAbs,imp.driveMinsPct);const ephEqual=Math.abs(s.eph - b.eph) < 1e-9; setKpi(document.getElementById('kI'),imp.ephAbs,imp.ephPct,false,ephEqual);setKpi(document.getElementById('kR'),imp.restMinsAbs,imp.restMinsPct);const notes=(json.notes||[]).join(' • ');const meta=`City ${b.cityId ?? '—'} · Trips ${s.tripsCount} · Ran in ${(t1-t0).toFixed(0)} ms ${notes?('· '+notes):''}`;document.getElementById('meta').textContent=meta;drawTimeline(json.timeline,b.shiftStart,b.shiftEnd);} function fromQuery(){const url=new URL(window.location.href);return{driverId:url.searchParams.get('driverId')||'E10010',date:url.searchParams.get('date')||'2023-01-16',tol:url.searchParams.get('tol')||'5',lookahead:url.searchParams.get('lookahead')||'30'};} function toQuery(p){const q=new URLSearchParams(p).toString();history.replaceState(null,'',`?${q}`);} document.getElementById('frm').addEventListener('submit',async(e)=>{e.preventDefault();const p={driverId:document.getElementById('driverId').value.trim(),date:document.getElementById('date').value.trim(),tol:document.getElementById('tol').value.trim(),lookahead:document.getElementById('lookahead').value.trim()};if(!p.driverId||!p.date){alert('Please fill driverId and date.');return;}document.getElementById('status').textContent='Running…';try{await runSim(p);toQuery(p);document.getElementById('status').textContent='Done.';}catch(err){document.getElementById('status').textContent='Error: '+err.message;}}); const init=fromQuery(); if(!init.driverId) init.driverId='E10010'; if(!init.date) init.date='2023-01-16'; document.getElementById('driverId').value=init.driverId; document.getElementById('date').value=init.date; document.getElementById('tol').value=init.tol; document.getElementById('lookahead').value=init.lookahead; if(init.driverId&&init.date){runSim(init).catch(()=>{});} </script></body></html>"""

@app.route("/simulate", methods=["GET"])
def simulate_view_api() -> Any:
    """API for the Simulation Viewer page.

    Query params:
      - driverId (required)
      - date (YYYY-MM-DD, required)
      - lookahead (mins, default 30)
      - tol (mins, default 5) — informational
    """
    global STATE
    if STATE is None:
        abort(503, description="Model not loaded")
    from flask import request
    driver_id = request.args.get("driverId")
    datestr = request.args.get("date")
    lookahead = int(request.args.get("lookahead", 30))
    tol = int(request.args.get("tol", 5))
    if not driver_id or not datestr:
        abort(400, description="driverId and date are required")
    try:
        target_day = pd.to_datetime(datestr).date()
    except Exception:
        abort(400, description="invalid date; expected YYYY-MM-DD")

    res = simulate_driver_day(STATE.df.copy(), STATE.model, driver_id=str(driver_id), day=target_day, window_mins=lookahead)

    def _stats(df: pd.DataFrame, earnings: float) -> Dict[str, Any]:
        drive = float(df.get("duration_mins", pd.Series(dtype=float)).fillna(0).sum())
        idle_rest = _calc_idle_and_rest_minutes(df)
        eph = float(earnings / (drive / 60.0)) if drive > 0 else 0.0
        return {
            "earnings": float(earnings),
            "eph": eph,
            "driveMins": drive,
            "idleMins": idle_rest["idle_minutes"],
            "restMins": idle_rest["rest_minutes"],
        }

    baseline = _stats(res.details_actual, res.actual_earnings)
    if not res.details_actual.empty:
        baseline["shiftStart"] = pd.to_datetime(res.details_actual["start_time"].min()).isoformat()
        baseline["shiftEnd"] = pd.to_datetime(res.details_actual["end_time"].max()).isoformat()
        try:
            baseline["cityId"] = int(res.details_actual["city_id"].mode().iloc[0])
        except Exception:
            baseline["cityId"] = None
    else:
        day_start = pd.Timestamp.combine(pd.Timestamp(target_day), pd.Timestamp.min.time())
        baseline["shiftStart"] = day_start.isoformat()
        baseline["shiftEnd"] = (day_start + pd.Timedelta(days=1)).isoformat()
        baseline["cityId"] = None

    simulated = _stats(res.details_simulated, res.simulated_earnings)
    simulated["tripsCount"] = int(res.simulated_count)

    def _delta(sim_v: float, base_v: float) -> Dict[str, float]:
        abs_val = sim_v - base_v
        pct = (abs_val / base_v * 100.0) if base_v not in (0, None) else 0.0
        return {"abs": float(abs_val), "pct": float(pct)}

    dE = _delta(simulated["earnings"], baseline["earnings"])
    dD = _delta(simulated["driveMins"], baseline["driveMins"])
    dEPH = _delta(simulated.get("eph", 0.0), baseline.get("eph", 0.0))
    dR = _delta(simulated["restMins"], baseline["restMins"])

    improvements = {
        "earningsAbs": dE["abs"], "earningsPct": dE["pct"],
        "driveMinsAbs": dD["abs"], "driveMinsPct": dD["pct"],
        "ephAbs": dEPH["abs"], "ephPct": dEPH["pct"],
        "restMinsAbs": dR["abs"], "restMinsPct": dR["pct"],
    }

    timeline: List[Dict[str, Any]] = []
    sim = res.details_simulated.sort_values("start_time") if not res.details_simulated.empty else res.details_simulated
    if sim is not None and not sim.empty:
        last_end = pd.NaT
        for _, row in sim.iterrows():
            st = pd.to_datetime(row["start_time"]).isoformat()
            en = pd.to_datetime(row["end_time"]).isoformat() if pd.notna(row["end_time"]) else st
            if pd.notna(last_end):
                gap_mins = (pd.to_datetime(row["start_time"]) - pd.to_datetime(last_end)).total_seconds() / 60.0
                if gap_mins > 0:
                    typ = "rest" if gap_mins >= 15.0 else "idle"
                    timeline.append({"type": typ, "start": pd.to_datetime(last_end).isoformat(), "end": pd.to_datetime(row["start_time"]).isoformat()})
            timeline.append({"type": "trip", "start": st, "end": en, "rideId": str(row.get("ride_id", ""))})
            last_end = row["end_time"]

    notes = [f"lookahead={lookahead}m", f"tol={tol}m"]
    return jsonify({"baseline": baseline, "simulated": simulated, "improvements": improvements, "timeline": timeline, "notes": notes})

@app.route("/prediction/top/<int:n>", methods=["GET"])
def predict_top(n: int) -> Any:
    global STATE
    if STATE is None:
        abort(503, description="Model not loaded")
    if n <= 0:
        abort(400, description="n must be > 0")
    n = min(n, len(STATE.preds))
    order = np.argsort(STATE.preds)[::-1][:n]
    subset = STATE.df.iloc[order][["ride_id", "driver_id", "city_id", "product", "duration_mins", "distance_km"]].copy()
    subset.loc[:, "rating"] = STATE.preds[order]
    # Convert to plain python types for JSON
    records = []
    for _, row in subset.iterrows():
        records.append(
            {
                "ride_id": str(row["ride_id"]),
                "driver_id": str(row["driver_id"]),
                "city_id": int(row["city_id"]),
                "product": str(row["product"]),
                "duration_mins": float(row["duration_mins"]),
                "distance_km": float(row["distance_km"]),
                "rating": float(row["rating"]),
            }
        )
    return jsonify(records)


def main() -> None:
    ap = argparse.ArgumentParser(description="Serve ride rating predictions")
    ap.add_argument("--excel", required=True, help="Path to Excel dataset")
    ap.add_argument("--model", required=True, help="Path to saved model.pkl")
    ap.add_argument("--rides-base", help="Optional base URL for an external rides service (e.g., http://localhost:8080/api/rides)")
    ap.add_argument("--host", default="127.0.0.1", help="Host address (default 127.0.0.1)")
    ap.add_argument("--port", type=int, default=8000, help="Port (default 8000)")
    args = ap.parse_args()

    global STATE
    STATE = load_state(Path(args.excel), Path(args.model), args.rides_base)
    app.run(host=args.host, port=args.port, debug=False)


if __name__ == "__main__":
    main()
