"""
CLI: simulate a driver's day and compare actual vs. model-greedy selection.

Usage (Excel path):
  PYTHONPATH=junctionx-uniTUD-2025 python3 -m ml.simulate_day \
    --excel junctionx-uniTUD-2025/ml/data/uber_hackathon_v2_mock_data.xlsx \
    --model junctionx-uniTUD-2025/ml/artifacts/model.pkl \
    --driver-id E10001 --date 2023-02-10 --window 30

If using Postgres, your view must include start_time/end_time; otherwise use Excel.
"""

from __future__ import annotations

import argparse
from datetime import date
from pathlib import Path

import pandas as pd

from .src.data_loader import load_and_prepare, load_postgres_view
from .src.model import RideScoringModel
from .src.simulation import simulate_driver_day


def main() -> None:
    ap = argparse.ArgumentParser(description="Simulate a driver's day vs greedy model policy")
    src = ap.add_mutually_exclusive_group(required=True)
    src.add_argument("--excel", help="Path to Excel dataset")
    src.add_argument("--postgres-conn", help="Postgres connection string")
    ap.add_argument("--pg-view", default="public.v_ride_features", help="Postgres view/table to read")
    ap.add_argument("--model", required=True, help="Path to saved model.pkl")
    ap.add_argument("--driver-id", required=True, help="Driver ID (e.g., E10001)")
    ap.add_argument("--date", required=True, help="Day in YYYY-MM-DD")
    ap.add_argument("--window", type=int, default=30, help="Decision window minutes (default 30)")
    args = ap.parse_args()

    target_day = date.fromisoformat(args.date)

    if args.postgres_conn:
        df = load_postgres_view(args.postgres_conn, view=args.pg_view)
    else:
        df = load_and_prepare(args.excel)

    model = RideScoringModel.load(Path(args.model))
    res = simulate_driver_day(df, model, driver_id=args.driver_id, day=target_day, window_mins=args.window)

    print(f"Driver: {res.driver_id}  Day: {res.day}")
    print(f"Actual:    {res.actual_count} rides, earnings €{res.actual_earnings:.2f}")
    print(f"Simulated: {res.simulated_count} rides, earnings €{res.simulated_earnings:.2f}")
    delta = res.simulated_earnings - res.actual_earnings
    print(f"Delta:     €{delta:.2f}")

    print("\nTop simulated picks:")
    if not res.details_simulated.empty:
        print(res.details_simulated.head(10).to_string(index=False))
    else:
        print("(none)")


if __name__ == "__main__":
    main()

