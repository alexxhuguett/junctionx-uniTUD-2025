# coded with the aid of LLMs

"""
Human-friendly data loading utilities for the driver ride scoring model.

Reads the hackathon Excel, parses key sheets, and prepares core dataframes
ready for feature engineering. Keep this file focused on IO and light parsing.
"""

from __future__ import annotations

from dataclasses import dataclass
from typing import Dict, Optional

import pandas as pd
from sqlalchemy import create_engine


@dataclass
class RawData:
    """Container for raw (lightly parsed) dataframes from Excel."""

    rides: pd.DataFrame
    earners: pd.DataFrame
    heatmap: pd.DataFrame
    cancellation_rates: Optional[pd.DataFrame] = None
    weather_daily: Optional[pd.DataFrame] = None
    surge_by_hour: Optional[pd.DataFrame] = None


def load_excel(path: str) -> RawData:
    """Load the Excel workbook and return a RawData bundle.

    Notes
    - Only loads sheets relevant to rides. Eats/orders are intentionally ignored.
    - Performs minimal parsing: datetime conversion and column standardization.
    """

    xls = pd.ExcelFile(path)

    # rides_trips
    rides = pd.read_excel(xls, "rides_trips")
    # Parse datetimes
    for col in ["start_time", "end_time", "date"]:
        if col in rides.columns:
            rides[col] = pd.to_datetime(rides[col])

    # earner metadata
    earners = pd.read_excel(xls, "earners")

    # Opportunity heatmap (destination earning potential)
    heatmap = pd.read_excel(xls, "heatmap")
    # Normalize column names for join
    heatmap = heatmap.rename(
        columns={
            "msg.city_id": "city_id",
            "msg.predictions.hexagon_id_9": "hexagon_id9",
            "msg.predictions.predicted_eph": "predicted_eph",
            "msg.predictions.in_final_heatmap": "in_final_heatmap",
        }
    )

    # Optional contextual sheets
    cancellation_rates = (
        pd.read_excel(xls, "cancellation_rates") if "cancellation_rates" in xls.sheet_names else None
    )
    weather_daily = (
        pd.read_excel(xls, "weather_daily") if "weather_daily" in xls.sheet_names else None
    )
    if weather_daily is not None:
        weather_daily["date"] = pd.to_datetime(weather_daily["date"])  # type: ignore[index]

    surge_by_hour = (
        pd.read_excel(xls, "surge_by_hour") if "surge_by_hour" in xls.sheet_names else None
    )

    return RawData(
        rides=rides,
        earners=earners,
        heatmap=heatmap,
        cancellation_rates=cancellation_rates,
        weather_daily=weather_daily,
        surge_by_hour=surge_by_hour,
    )


def base_join(rides: pd.DataFrame, earners: pd.DataFrame) -> pd.DataFrame:
    """Attach earner metadata onto rides using driver_id -> earner_id.

    - Keeps original ride rows.
    - Renames potentially ambiguous columns to explicit names.
    """
    earners_renamed = earners.rename(
        columns={
            "earner_id": "driver_id",
            "rating": "driver_rating",
        }
    )
    cols_to_use = [
        "driver_id",
        "earner_type",
        "vehicle_type",
        "fuel_type",
        "is_ev",
        "experience_months",
        "driver_rating",
        "status",
        "home_city_id",
    ]
    earners_renamed = earners_renamed[cols_to_use]

    merged = rides.merge(earners_renamed, on="driver_id", how="left", suffixes=("_ride", "_earner"))

    # If rides and earners both provide overlapping columns, prefer ride's observed values,
    # but fall back to earner attributes if ride value is missing.
    for col in ["vehicle_type", "is_ev"]:
        ride_col = f"{col}_ride"
        earner_col = f"{col}_earner"
        if ride_col in merged.columns and earner_col in merged.columns:
            merged[col] = merged[ride_col].where(merged[ride_col].notna(), merged[earner_col])
            merged.drop(columns=[ride_col, earner_col], inplace=True)
        elif ride_col in merged.columns:
            merged.rename(columns={ride_col: col}, inplace=True)
        elif earner_col in merged.columns:
            merged.rename(columns={earner_col: col}, inplace=True)
    return merged


def attach_destination_context(
    rides: pd.DataFrame, heatmap: pd.DataFrame, cancellation_rates: Optional[pd.DataFrame]
) -> pd.DataFrame:
    """Attach destination opportunity (predicted EPH) and cancellation risk.

    Joins on (city_id, drop_hex_id9) for rides to hexagon-based context.
    """
    # Heatmap join: predicted earning potential at destination
    hm = heatmap[["city_id", "hexagon_id9", "predicted_eph", "in_final_heatmap"]].drop_duplicates()
    rides = rides.merge(
        hm,
        left_on=["city_id", "drop_hex_id9"],
        right_on=["city_id", "hexagon_id9"],
        how="left",
    )
    rides = rides.drop(columns=["hexagon_id9"], errors="ignore")
    rides = rides.rename(columns={"predicted_eph": "predicted_eph_drop", "in_final_heatmap": "drop_in_heatmap"})

    # Cancellation rates at destination
    if cancellation_rates is not None:
        cr = cancellation_rates.rename(columns={"hexagon_id9": "drop_hex_id9"})
        cr = cr[["city_id", "drop_hex_id9", "cancellation_rate_pct"]].drop_duplicates()
        rides = rides.merge(cr, on=["city_id", "drop_hex_id9"], how="left")
        rides = rides.rename(columns={"cancellation_rate_pct": "cancellation_rate_drop"})
    else:
        rides["cancellation_rate_drop"] = pd.NA

    return rides


def attach_temporal_context(rides: pd.DataFrame, weather_daily: Optional[pd.DataFrame]) -> pd.DataFrame:
    """Add time-derived columns and weather by (city_id, date)."""
    rides["hour"] = rides["start_time"].dt.hour
    rides["weekday"] = rides["start_time"].dt.weekday
    rides["date_only"] = rides["date"].dt.date

    if weather_daily is not None:
        wx = weather_daily.copy()
        wx["date_only"] = wx["date"].dt.date
        rides = rides.merge(wx[["city_id", "date_only", "weather"]], on=["city_id", "date_only"], how="left")
    else:
        rides["weather"] = pd.NA

    return rides


def load_and_prepare(path: str) -> pd.DataFrame:
    """Convenience: load Excel and apply base joins and context.

    Returns a dataframe ready for feature engineering.
    """
    raw = load_excel(path)
    df = base_join(raw.rides, raw.earners)
    df = attach_destination_context(df, raw.heatmap, raw.cancellation_rates)
    df = attach_temporal_context(df, raw.weather_daily)
    return df


def load_postgres_view(conn_str: str, view: str = "public.v_ride_features") -> pd.DataFrame:
    """Load model-ready rows from a Postgres view.

    Expects the view to expose the columns used by features and labeling, e.g.
    the `v_ride_features` view created from the provided SQL.
    """
    eng = create_engine(conn_str)
    df = pd.read_sql(f"SELECT * FROM {view}", eng)
    return df
