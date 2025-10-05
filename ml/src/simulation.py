"""
Driver-day counterfactual simulation.

Given a driver and a date, simulate a policy that every `window_mins` looks at
rides starting in [t, t+window) and picks the highest-rated ride (by our model),
then advances time to the end of that ride. Compare to the actual rides taken.

Notes
- Works best with the Excel path because it includes start_time/end_time.
- If using a Postgres view, ensure it includes start_time/end_time columns.
"""

from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime, timedelta, date
from typing import List, Optional, Tuple

import numpy as np
import pandas as pd

from .features import finalize_feature_table, compute_avg_speed, compute_fatigue_features
from .model import RideScoringModel


@dataclass
class SimulationResult:
    driver_id: str
    day: date
    actual_count: int
    actual_earnings: float
    simulated_count: int
    simulated_earnings: float
    details_actual: pd.DataFrame
    details_simulated: pd.DataFrame


def _prepare_for_scoring(df: pd.DataFrame) -> Tuple[pd.DataFrame, np.ndarray]:
    """Finalize features and compute predictions with the saved model.

    Returns (df_with_preds, preds)
    """
    X = finalize_feature_table(df)
    # Model is expected to be passed by caller; we score outside
    return X


def _score_with_model(model: RideScoringModel, df: pd.DataFrame) -> np.ndarray:
    X = finalize_feature_table(df)
    return model.predict(X)


def simulate_driver_day(
    df: pd.DataFrame,
    model: RideScoringModel,
    driver_id: str,
    day: date,
    window_mins: int = 30,
    city_id: Optional[int] = None,
) -> SimulationResult:
    """Simulate a driver's day and compare to actual.

    Assumptions
    - Candidate rides are rides in the same city (if provided) that start
      within each decision window.
    - We pick at most one ride per window; after picking a ride, time advances
      to that ride's end time (no overlap).
    - Earnings use net_earnings + tips.
    """
    required_cols = {"ride_id", "driver_id", "city_id", "start_time", "end_time", "net_earnings", "tips"}
    if not required_cols.issubset(df.columns):
        missing = required_cols - set(df.columns)
        raise ValueError(f"simulate_driver_day requires columns: {sorted(required_cols)}; missing {sorted(missing)}")

    # Filter to the day window
    day_start = pd.Timestamp(datetime.combine(day, datetime.min.time()))
    day_end = day_start + pd.Timedelta(days=1)
    df_day = df[(df["start_time"] >= day_start) & (df["start_time"] < day_end)].copy()

    if city_id is None:
        # Default city: the driver's most common city that day (or overall if none)
        df_driver_day = df_day[df_day["driver_id"] == driver_id]
        if not df_driver_day.empty:
            city_id = int(df_driver_day["city_id"].mode().iloc[0])
        else:
            city_id = int(df_day["city_id"].mode().iloc[0])

    # Score all rides for the day (in selected city)
    candidates = df_day[df_day["city_id"] == city_id].copy()
    # Ensure derived features exist when using Excel path
    if "avg_speed_kmh" not in candidates.columns:
        candidates = compute_avg_speed(candidates)
    if "active_minutes_since_rest" not in candidates.columns and {"start_time", "end_time", "duration_mins"}.issubset(candidates.columns):
        candidates = compute_fatigue_features(candidates)

    candidates["pred_rating"] = _score_with_model(model, candidates)

    # Actual rides for the driver that day
    actual = df_day[(df_day["driver_id"] == driver_id)].copy()
    actual = actual.sort_values("start_time")
    actual_earn = (actual.get("net_earnings", 0.0).astype(float) + actual.get("tips", 0.0).astype(float)).sum()

    # Simulate greedy selection by windows
    t = day_start
    sim_rows: List[pd.Series] = []
    used_ids: set = set()
    while t < day_end:
        wnd_end = t + pd.Timedelta(minutes=window_mins)
        window_candidates = candidates[(candidates["start_time"] >= t) & (candidates["start_time"] < wnd_end) & (~candidates["ride_id"].isin(used_ids))]
        if window_candidates.empty:
            t = wnd_end
            continue
        best = window_candidates.sort_values("pred_rating", ascending=False).iloc[0]
        sim_rows.append(best)
        used_ids.add(best["ride_id"])
        # Advance time to end of chosen ride
        t = pd.Timestamp(best["end_time"]) if pd.notna(best["end_time"]) else wnd_end

    simulated = pd.DataFrame(sim_rows)
    simulated = simulated.sort_values("start_time") if not simulated.empty else simulated
    simulated_earn = (simulated.get("net_earnings", 0.0).astype(float) + simulated.get("tips", 0.0).astype(float)).sum()

    return SimulationResult(
        driver_id=driver_id,
        day=day,
        actual_count=len(actual),
        actual_earnings=float(actual_earn),
        simulated_count=len(simulated),
        simulated_earnings=float(simulated_earn),
        details_actual=actual[[
            "ride_id", "start_time", "end_time", "city_id", "product", "distance_km", "duration_mins", "net_earnings", "tips"
        ]].copy(),
        details_simulated=simulated[[
            "ride_id", "start_time", "end_time", "city_id", "product", "distance_km", "duration_mins", "pred_rating", "net_earnings", "tips"
        ]].copy(),
    )

