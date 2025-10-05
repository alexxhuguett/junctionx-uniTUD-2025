# coded with the aid of LLMs

"""
Feature engineering for ride scoring.

Adds fatigue metrics, speed, and other derived features needed by the model.
This file should remain free of IO; accept/return dataframes only.
"""

from __future__ import annotations

import numpy as np
import pandas as pd


def compute_avg_speed(df: pd.DataFrame) -> pd.DataFrame:
    """Compute average speed in km/h for each trip, clipped to reasonable bounds.

    - duration zero/near-zero is handled by setting speed to NaN, then filled by median.
    - Speed is clipped to [3, 130] to reduce outlier influence (bike lanes to highway).
    """
    df = df.copy()
    dur_hours = df["duration_mins"].astype(float).replace(0, np.nan) / 60.0
    speed = df["distance_km"].astype(float) / dur_hours
    speed = speed.replace([np.inf, -np.inf], np.nan)
    df.loc[:, "avg_speed_kmh"] = speed
    # Fill any remaining NaNs with median per city; fallback to global median
    if "city_id" in df.columns:
        med_per_city = df.groupby("city_id")["avg_speed_kmh"].transform("median")
        df.loc[:, "avg_speed_kmh"] = df["avg_speed_kmh"].fillna(med_per_city)
    df.loc[:, "avg_speed_kmh"] = df["avg_speed_kmh"].fillna(df["avg_speed_kmh"].median())
    df.loc[:, "avg_speed_kmh"] = df["avg_speed_kmh"].clip(lower=3, upper=130)
    return df


def _active_minutes_since_last_rest(group: pd.DataFrame) -> pd.Series:
    """Helper: compute cumulative active minutes since last rest for a driver's trips.

    Definition
    - A rest is a gap >= 15 minutes between consecutive trips.
    - "Active minutes since last rest" for trip i is the sum of durations of all
      consecutive prior trips since the most recent rest gap.
    - For the first trip or after a rest, value is 0.
    """
    group = group.sort_values("start_time").copy()
    prev_end = group["end_time"].shift(1)
    gap_mins = (group["start_time"] - prev_end).dt.total_seconds() / 60.0
    is_rest = gap_mins >= 15.0

    # cumulative active minutes since last rest
    active = []
    cum = 0.0
    for dur, rest in zip(group["duration_mins"].fillna(0.0), is_rest.fillna(True)):
        if rest:
            cum = 0.0
        active.append(cum)
        cum += float(dur)
    return pd.Series(active, index=group.index, name="active_minutes_since_rest")


def compute_fatigue_features(df: pd.DataFrame) -> pd.DataFrame:
    """Compute fatigue-related features per driver.

    Adds columns:
    - active_minutes_since_rest: cumulative driving minutes since last rest gap (>=15m)
    """
    df = df.copy()
    if not {"driver_id", "start_time", "end_time", "duration_mins"}.issubset(df.columns):
        raise ValueError("Dataframe missing required columns for fatigue features")

    # Compute per-driver active minutes since last rest; ensure a 1-D Series
    tmp = df.groupby("driver_id", group_keys=False).apply(_active_minutes_since_last_rest)
    # Some pandas versions may return a DataFrame; squeeze to first column
    if isinstance(tmp, pd.DataFrame):
        tmp = tmp.iloc[:, 0]
    # Align to original index explicitly
    tmp = tmp.reindex(df.index)
    df.loc[:, "active_minutes_since_rest"] = tmp.values
    return df


def finalize_feature_table(df: pd.DataFrame) -> pd.DataFrame:
    """Return a compact model-ready feature table from enriched rides.

    The goal is to select stable, low-leakage features available at decision time.
    We avoid high-cardinality hex IDs and rely on contextual aggregates.
    """
    df = df.copy()
    # If avg_speed_kmh not provided (e.g., Excel path), compute it
    if "avg_speed_kmh" not in df.columns:
        df = compute_avg_speed(df)
    # If fatigue not provided and timestamps exist, compute it
    if "active_minutes_since_rest" not in df.columns:
        if {"start_time", "end_time", "duration_mins"}.issubset(df.columns):
            df = compute_fatigue_features(df)
        else:
            # Leave missing; selection below will error if required
            pass

    # Convenience booleans
    df.loc[:, "home_city_match"] = (df.get("home_city_id") == df.get("city_id")).astype("float")

    # Select features
    feature_cols = [
        # Trip/context
        "city_id",
        "product",
        "surge_multiplier",
        "distance_km",
        "duration_mins",
        "avg_speed_kmh",
        "active_minutes_since_rest",
        "hour",
        "weekday",
        # Destination opportunity
        "predicted_eph_drop",
        "cancellation_rate_drop",
        # Weather
        "weather",
        # Driver
        "vehicle_type",
        "is_ev",
        "experience_months",
        "driver_rating",
        "home_city_match",
    ]

    missing = [c for c in feature_cols if c not in df.columns]
    if missing:
        raise ValueError(f"Missing expected columns: {missing}")

    return df[feature_cols].copy()
