from __future__ import annotations
from abc import ABC, abstractmethod
from dataclasses import dataclass
from typing import Iterable, Optional, Tuple, Literal, Dict, Any
import pandas as pd
import numpy as np

# Abstract data-fetching class aided by an LLM

# ---------- Contracts / Configs ----------

class BaseDataSource(ABC):
    """
    Java-interface style base class that defines the data access contract.
    Swap implementations (Excel, Postgres, etc.) without changing call sites.
    """

    # ---- Core fetchers (implement in subclasses) ----
    @abstractmethod
    def fetch_rides(
        self,
        since: Optional[pd.Timestamp] = None,
        until: Optional[pd.Timestamp] = None,
        columns: Optional[Iterable[str]] = None,
    ) -> pd.DataFrame:
        """Return trip-level data (rides_trips)."""

    @abstractmethod
    def fetch_earners(
        self,
        columns: Optional[Iterable[str]] = None,
    ) -> pd.DataFrame:
        """Return driver profiles (earners)."""

    # ---- Optional generic table hook (implement if you want) ----
    def fetch_table(
        self,
        table: str,
        where: Optional[str] = None,
        columns: Optional[Iterable[str]] = None,
    ) -> pd.DataFrame:
        """Optional generic accessor. Default: NotImplemented."""
        raise NotImplementedError("fetch_table not implemented for this data source")

    # ---- Common helpers (shared across implementations) ----
    def join_rides_earners(
        self,
        rides: Optional[pd.DataFrame] = None,
        earners: Optional[pd.DataFrame] = None,
        rides_driver_key: str = "driver_id",
        earners_key: str = "earner_id",
        how: Literal["left","inner","right","outer"] = "left",
    ) -> pd.DataFrame:
        """Join rides with earners on driver key (defaults to left-join)."""
        if rides is None:
            rides = self.fetch_rides()
        if earners is None:
            earners = self.fetch_earners()
        return rides.merge(earners, left_on=rides_driver_key, right_on=earners_key, how=how)

    def compute_derived_features(
        self,
        df: pd.DataFrame,
        *,
        start_col: str = "start_time",
        end_col: str = "end_time",
        date_col: str = "date",
        pickup_lat: str = "pickup_lat",
        pickup_lon: str = "pickup_lon",
        drop_lat: str = "drop_lat",
        drop_lon: str = "drop_lon",
        duration_mins: str = "duration_mins",
        net_earnings: str = "net_earnings",
        group_driver: str = "driver_id",
    ) -> pd.DataFrame:
        """
        Adds commonly used engineered features (safe to call on both Excel/DB data):
          - hour_of_day, weekday
          - shift_minutes (minutes since first ride of the day per driver)
          - pickup_drop_distance_km (very rough haversine proxy)
          - earning_rate (net_earnings per minute, guarded)
        Override in subclasses if you need custom behavior.
        """
        df = df.copy()

        # Timestamps
        if start_col in df and pd.api.types.is_datetime64_any_dtype(df[start_col]) is False:
            df[start_col] = pd.to_datetime(df[start_col], errors="coerce")
        if end_col in df and pd.api.types.is_datetime64_any_dtype(df[end_col]) is False:
            df[end_col] = pd.to_datetime(df[end_col], errors="coerce")
        if date_col in df and pd.api.types.is_datetime64_any_dtype(df[date_col]) is False:
            df[date_col] = pd.to_datetime(df[date_col], errors="coerce")

        # Hour / weekday (robust to missing)
        if start_col in df:
            df["hour_of_day"] = df[start_col].dt.hour
            df["weekday"] = df[start_col].dt.dayofweek

        # Shift minutes from first ride of the day per driver
        if {group_driver, date_col, start_col, end_col}.issubset(df.columns):
            day_key = df[date_col].dt.date
            first_start = df.groupby([group_driver, day_key])[start_col].transform("min")
            delta = (df[end_col] - first_start).dt.total_seconds() / 60.0
            df["shift_minutes"] = delta

        # Simple lat/lon distance proxy in km (not accurate haversine, but OK for ranking)
        if {pickup_lat, pickup_lon, drop_lat, drop_lon}.issubset(df.columns):
            df["pickup_drop_distance_km"] = np.sqrt(
                (df[pickup_lat] - df[drop_lat]) ** 2 +
                (df[pickup_lon] - df[drop_lon]) ** 2
            ) * 111.0

        # Earning rate guard
        if {net_earnings, duration_mins}.issubset(df.columns):
            dur = df[duration_mins].replace(0, np.nan)
            df["earning_rate"] = df[net_earnings] / dur

        return df