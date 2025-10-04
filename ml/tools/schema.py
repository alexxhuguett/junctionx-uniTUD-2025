# aided by LLMs

"""
CLI helper to print and validate the feature schema expected by the model.

Usage (from project root):
  # Print schema JSON
  PYTHONPATH=junctionx-uniTUD-2025 python3 -m ml.tools.schema --print

  # Validate a CSV file contains required columns with basic type checks
  PYTHONPATH=junctionx-uniTUD-2025 python3 -m ml.tools.schema --validate-csv path/to/features.csv
"""

from __future__ import annotations

import argparse
import json
from typing import Dict, List, Tuple

import pandas as pd

from ml.src.model import NUMERIC_FEATURES, CATEGORICAL_FEATURES


def get_schema() -> Dict[str, object]:
    """Return the feature schema the model expects.

    Includes feature groups and light constraints helpful for validation.
    """
    constraints = {
        # Non-negative
        "distance_km": {"min": 0},
        "duration_mins": {"min": 0},
        "predicted_eph_drop": {"min": 0},
        "cancellation_rate_drop": {"min": 0},
        # Bounded time fields
        "hour": {"min": 0, "max": 23},
        "weekday": {"min": 0, "max": 6},
        # Boolean-like numeric
        "is_ev": {"allowed": [0, 1]},
        # Similar to a boolean but floating
        "home_city_match": {"allowed": [0, 1]},
    }
    return {
        "numeric": NUMERIC_FEATURES,
        "categorical": CATEGORICAL_FEATURES,
        "constraints": constraints,
        "notes": "All columns are required. Categorical values outside training may be handled via OHE handle_unknown=ignore.",
    }


def _as_numeric(series: pd.Series) -> pd.Series:
    return pd.to_numeric(series, errors="coerce")


def validate_df(df: pd.DataFrame) -> Tuple[bool, List[str]]:
    schema = get_schema()
    numeric = list(schema["numeric"])  # type: ignore[index]
    categorical = list(schema["categorical"])  # type: ignore[index]
    constraints = dict(schema.get("constraints", {}))

    errors: List[str] = []

    # Presence
    missing = [c for c in numeric + categorical if c not in df.columns]
    if missing:
        errors.append(f"Missing columns: {missing}")

    # Type checks for those present
    for col in numeric:
        if col in df.columns:
            vals = _as_numeric(df[col])
            # If too many non-convertible, flag
            n_bad = int(vals.isna().sum()) - int(df[col].isna().sum())
            if n_bad > 0:
                errors.append(f"Non-numeric values in numeric column '{col}' (count={n_bad})")
            # Range/allowed
            if col in constraints:
                c = constraints[col]
                if "min" in c and (vals.dropna() < c["min"]).any():
                    errors.append(f"Values below min for '{col}' (< {c['min']})")
                if "max" in c and (vals.dropna() > c["max"]).any():
                    errors.append(f"Values above max for '{col}' (> {c['max']})")
                if "allowed" in c:
                    allowed = set(c["allowed"])  # type: ignore[assignment]
                    bad = set(vals.dropna().unique()) - allowed
                    if bad:
                        errors.append(f"Unexpected values in '{col}', only {sorted(allowed)} allowed; saw {sorted(bad)}")

    # Categorical columns: ensure exist; values will be OHE'd (unknown permitted)
    for col in categorical:
        if col in df.columns:
            # Convert to string-like for a quick sanity check; allow NaN
            _ = df[col].astype("string")

    return (len(errors) == 0, errors)


def main() -> None:
    ap = argparse.ArgumentParser(description="Print and validate model feature schema")
    ap.add_argument("--print", action="store_true", help="Print schema as JSON")
    ap.add_argument("--validate-csv", help="Validate a CSV file against the schema")
    args = ap.parse_args()

    if args.print:
        print(json.dumps(get_schema(), indent=2))
        return

    if args.validate_csv:
        df = pd.read_csv(args.validate_csv)
        ok, issues = validate_df(df)
        if ok:
            print("OK: CSV matches schema")
        else:
            print("Schema validation errors:")
            for e in issues:
                print("-", e)
            raise SystemExit(1)
        return

    ap.print_help()


if __name__ == "__main__":
    main()

