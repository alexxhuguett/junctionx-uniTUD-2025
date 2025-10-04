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

from .src.data_loader import load_and_prepare
from .src.features import finalize_feature_table
from .src.model import RideScoringModel


app = Flask(__name__)


@dataclass
class ServingState:
    excel_path: Path
    model_path: Path
    df: pd.DataFrame
    X: pd.DataFrame
    preds: np.ndarray
    index_by_ride: Dict[str, int]


STATE: Optional[ServingState] = None


def load_state(excel_path: Path, model_path: Path) -> ServingState:
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
        df=df,
        X=X,
        preds=preds,
        index_by_ride=index_by_ride,
    )


@app.route("/health", methods=["GET"])
def health() -> Any:
    return jsonify({"status": "ok"})


@app.route("/prediction/<ride_id>", methods=["GET"])
def predict_one(ride_id: str) -> Any:
    global STATE
    if STATE is None:
        abort(503, description="Model not loaded")
    idx = STATE.index_by_ride.get(str(ride_id))
    if idx is None:
        abort(404, description="ride_id not found")
    rating = float(STATE.preds[idx])
    return jsonify({"ride_id": ride_id, "rating": rating})


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
    ap.add_argument("--host", default="127.0.0.1", help="Host address (default 127.0.0.1)")
    ap.add_argument("--port", type=int, default=8000, help="Port (default 8000)")
    args = ap.parse_args()

    global STATE
    STATE = load_state(Path(args.excel), Path(args.model))
    app.run(host=args.host, port=args.port, debug=False)


if __name__ == "__main__":
    main()

