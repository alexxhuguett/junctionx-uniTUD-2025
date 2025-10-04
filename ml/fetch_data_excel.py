from __future__ import annotations
from fetch_data import BaseDataSource
from abc import ABC, abstractmethod
from dataclasses import dataclass
from typing import Iterable, Optional, Tuple, Literal, Dict, Any
from pathlib import Path
import pandas as pd
import numpy as np

@dataclass
class ExcelConfig:
    path: str
    rides_sheet: str = "rides_trips"
    earners_sheet: str = "earners"
    read_excel_kwargs: Dict[str, Any] = None  # e.g., {"engine": "openpyxl"}

class ExcelDataSource(BaseDataSource):
    def __init__(self, cfg: ExcelConfig):
        self.cfg = cfg
        self._excel = pd.ExcelFile(cfg.path)  # caches sheet names, speeds up reads

    def _read_sheet(self, sheet: str, usecols: Optional[Iterable[str]] = None) -> pd.DataFrame:
        kwargs = self.cfg.read_excel_kwargs or {}
        return pd.read_excel(self._excel, sheet_name=sheet, usecols=usecols, **kwargs)

    def fetch_rides(
        self,
        since: Optional[pd.Timestamp] = None,
        until: Optional[pd.Timestamp] = None,
        columns: Optional[Iterable[str]] = None,
    ) -> pd.DataFrame:
        df = self._read_sheet(self.cfg.rides_sheet, usecols=columns)
        # Standardize timestamps for filtering
        for c in ("start_time", "end_time", "date"):
            if c in df.columns:
                df[c] = pd.to_datetime(df[c], errors="coerce")
        if since is not None and "start_time" in df.columns:
            df = df[df["start_time"] >= pd.Timestamp(since)]
        if until is not None and "start_time" in df.columns:
            df = df[df["start_time"] < pd.Timestamp(until)]
        return df

    def fetch_earners(
        self,
        columns: Optional[Iterable[str]] = None,
    ) -> pd.DataFrame:
        return self._read_sheet(self.cfg.earners_sheet, usecols=columns)

    def fetch_table(
        self,
        table: str,
        where: Optional[str] = None,
        columns: Optional[Iterable[str]] = None,
    ) -> pd.DataFrame:
        # Map to sheet if present
        if table in self._excel.sheet_names:
            df = self._read_sheet(table, usecols=columns)
            # 'where' is ignored for Excel; caller can filter after
            return df
        raise ValueError(f"Sheet '{table}' not found in workbook.")

if __name__ == "__main__":
    data_path = Path(__file__).parent / "data" / "uber_hackathon_v2_mock_data.xlsx"
    cfg = ExcelConfig(path=str(data_path))
    loader = ExcelDataSource(cfg)

    rides = loader.fetch_rides()
    earners = loader.fetch_earners()
    merged = loader.join_rides_earners(rides, earners)
    features = loader.compute_derived_features(merged)

    print(features.head())
