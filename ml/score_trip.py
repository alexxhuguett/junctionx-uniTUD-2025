# coded with the aid of LLMs

"""
CLI to score rides using a saved model.

Modes:
1) Score a specific ride by `ride_id` from the Excel:
     python -m ml.score_trip --excel ...xlsx --model ml/artifacts/model.pkl --ride-id 58e20...
2) Score a CSV containing the feature columns expected by the model:
     python -m ml.score_trip --model ml/artifacts/model.pkl --features-csv path/to/features.csv
3) Print a sample/top-N rides from the Excel with predicted ratings:
     python -m ml.score_trip --excel ...xlsx --model ml/artifacts/model.pkl --sample 20
     python -m ml.score_trip --excel ...xlsx --model ml/artifacts/model.pkl --top 20
"""

from __future__ import annotations

import argparse
from pathlib import Path

import pandas as pd

from .src.data_loader import load_and_prepare
from .src.features import finalize_feature_table
from .src.model import RideScoringModel


def main() -> None:
    ap = argparse.ArgumentParser(description="Score rides using the saved model")
    ap.add_argument("--model", required=True, help="Path to saved model.pkl")
    ap.add_argument("--excel", help="Path to Excel dataset (required if using --ride-id)")
    ap.add_argument("--ride-id", help="Ride UUID to score from the Excel")
    ap.add_argument("--features-csv", help="CSV with prebuilt feature rows to score")
    ap.add_argument("--sample", type=int, help="Print N random rides with predicted ratings")
    ap.add_argument("--top", type=int, help="Print top N rides by predicted rating")
    args = ap.parse_args()

    model = RideScoringModel.load(Path(args.model))

    if args.ride_id:
        if not args.excel:
            raise SystemExit("--excel is required when using --ride-id")
        df = load_and_prepare(args.excel)
        row = df[df["ride_id"] == args.ride_id]
        if row.empty:
            raise SystemExit("ride_id not found in Excel")
        X = finalize_feature_table(row)
        preds = model.predict(X)
        print(float(preds[0]))
        return

    if args.features_csv:
        X = pd.read_csv(args.features_csv)
        preds = model.predict(X)
        for p in preds:
            print(float(p))
        return

    # Sample/top-N listing from the Excel
    if args.sample or args.top:
        if not args.excel:
            raise SystemExit("--excel is required when using --sample/--top")
        df = load_and_prepare(args.excel)
        X = finalize_feature_table(df)
        preds = model.predict(X)
        out = df[[
            "ride_id",
            "driver_id",
            "city_id",
            "product",
            "duration_mins",
            "distance_km",
        ]].copy()
        out.loc[:, "rating"] = preds
        if args.sample:
            print(out.sample(args.sample, random_state=42).to_string(index=False))
            return
        if args.top:
            print(out.sort_values("rating", ascending=False).head(args.top).to_string(index=False))
            return

    raise SystemExit("Provide either --ride-id with --excel, or --features-csv")


if __name__ == "__main__":
    main()
