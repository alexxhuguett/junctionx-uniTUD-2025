# coded with the aid of LLMs

"""
CLI to train the ride scoring model from the Excel dataset.

Usage (run from repo root):
  python -m ml.train_model \
    --excel junctionx-uniTUD-2025/ml/data/uber_hackathon_v2_mock_data.xlsx \
    --outdir junctionx-uniTUD-2025/ml/artifacts

This script intentionally keeps logs minimal and human-readable.
"""

from __future__ import annotations

import argparse
from pathlib import Path

import pandas as pd

from .src.data_loader import load_and_prepare, load_postgres_view
from .src.features import finalize_feature_table, compute_avg_speed, compute_fatigue_features
from .src.labeling import Weights, build_labels
from .src.model import RideScoringModel


def main() -> None:
    ap = argparse.ArgumentParser(description="Train ride scoring model")
    src = ap.add_mutually_exclusive_group(required=True)
    src.add_argument("--excel", help="Path to Excel dataset")
    src.add_argument("--postgres-conn", help="Postgres connection string (e.g., postgresql+psycopg2://user:pass@host:port/db)")
    ap.add_argument("--pg-view", default="public.v_ride_features", help="Postgres view to read when using --postgres-conn")
    ap.add_argument("--outdir", required=True, help="Directory to save model artifacts")
    ap.add_argument("--w_profit", type=float, default=0.5, help="Weight for profit score")
    ap.add_argument("--w_opportunity", type=float, default=0.3, help="Weight for opportunity score")
    ap.add_argument("--w_wellbeing", type=float, default=0.2, help="Weight for wellbeing score")
    ap.add_argument("--model", choices=["hgb", "rf", "ridge"], default="hgb", help="Regressor type")
    args = ap.parse_args()

    outdir = Path(args.outdir)

    # Load data either from Postgres view or Excel
    if args.postgres_conn:
        df_enriched = load_postgres_view(args.postgres_conn, view=args.pg_view)
    else:
        df = load_and_prepare(args.excel)
        # Ensure labeling prerequisites exist (avg_speed_kmh, active_minutes_since_rest)
        df_enriched = compute_avg_speed(df.copy())
        df_enriched = compute_fatigue_features(df_enriched)

    # Feature table (built from enriched data)
    X = finalize_feature_table(df_enriched)

    # Labels from enriched data
    y, comps = build_labels(
        df_enriched, Weights(args.w_profit, args.w_opportunity, args.w_wellbeing)
    )

    # Train
    model = RideScoringModel(model_type=args.model)
    result = model.train(X, y, model_dir=outdir)

    print("Model saved to:", result.model_path)
    if result.mae is not None:
        print(f"Validation MAE: {result.mae:.3f}")
        print(f"Validation R2:  {result.r2:.3f}")


if __name__ == "__main__":
    main()
