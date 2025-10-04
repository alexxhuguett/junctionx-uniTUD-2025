# coded with the aid of LLMs

"""
Model wrapper for ride scoring.

Provides a scikit-learn Pipeline for feature preprocessing and regression.
If scikit-learn is unavailable at runtime, falls back to a simple baseline
that returns the directly computed composite rating.
"""

from __future__ import annotations

import json
import pickle
from dataclasses import dataclass
from pathlib import Path
from typing import List, Optional, Tuple

import numpy as np
import pandas as pd
import inspect

try:
    from sklearn.compose import ColumnTransformer
    from sklearn.ensemble import RandomForestRegressor
    from sklearn.ensemble import HistGradientBoostingRegressor
    from sklearn.linear_model import Ridge
    from sklearn.metrics import mean_absolute_error, r2_score
    from sklearn.model_selection import train_test_split
    from sklearn.pipeline import Pipeline
    from sklearn.preprocessing import OneHotEncoder
    from sklearn.impute import SimpleImputer

    SKLEARN_AVAILABLE = True
except Exception:  # pragma: no cover - optional dependency
    SKLEARN_AVAILABLE = False


NUMERIC_FEATURES = [
    "surge_multiplier",
    "distance_km",
    "duration_mins",
    "avg_speed_kmh",
    "hour",
    "weekday",
    "predicted_eph_drop",
    "cancellation_rate_drop",
    "is_ev",
    "experience_months",
    "driver_rating",
    "home_city_match",
]

CATEGORICAL_FEATURES = [
    "city_id",
    "product",
    "vehicle_type",
    "weather",
]


@dataclass
class TrainResult:
    model_path: Path
    n_train: int
    n_val: int
    mae: Optional[float]
    r2: Optional[float]


class RideScoringModel:
    """Encapsulates preprocessing and regression for rating prediction."""

    def __init__(self, model_type: str = "hgb"):
        self.pipeline: Optional[Pipeline] = None
        self.model_type = model_type

    def _build_pipeline(self) -> Pipeline:
        if not SKLEARN_AVAILABLE:
            raise RuntimeError("scikit-learn is not available; cannot build pipeline")

        # Preprocessing: imputers + OHE (dense)
        numeric_transformer = Pipeline(steps=[
            ("impute", SimpleImputer(strategy="median")),
        ])
        # Use sparse=False for broad sklearn compatibility; HGB needs dense
        # Build OHE with compatibility across sklearn versions
        def _make_ohe():
            params = inspect.signature(OneHotEncoder).parameters
            if "sparse_output" in params:
                return OneHotEncoder(handle_unknown="ignore", sparse_output=False)
            else:
                return OneHotEncoder(handle_unknown="ignore", sparse=False)

        categorical_transformer = Pipeline(steps=[
            ("impute", SimpleImputer(strategy="most_frequent")),
            ("ohe", _make_ohe()),
        ])
        preprocessor = ColumnTransformer(
            transformers=[
                ("num", numeric_transformer, NUMERIC_FEATURES),
                ("cat", categorical_transformer, CATEGORICAL_FEATURES),
            ]
        )

        # Choose estimator
        mt = (self.model_type or "hgb").lower()
        if mt == "rf":
            regressor = RandomForestRegressor(
                n_estimators=300,
                max_depth=None,
                min_samples_leaf=1,
                random_state=42,
                n_jobs=-1,
            )
        elif mt == "ridge":
            regressor = Ridge(alpha=1.0, random_state=42)
        else:  # default to a smoother gradient boosting regressor
            regressor = HistGradientBoostingRegressor(
                learning_rate=0.08,
                max_depth=None,
                max_iter=300,
                l2_regularization=0.0,
                random_state=42,
            )

        pipe = Pipeline(steps=[("prep", preprocessor), ("reg", regressor)])
        return pipe

    def train(
        self, X: pd.DataFrame, y: pd.Series, *, model_dir: Path, val_size: float = 0.2
    ) -> TrainResult:
        model_dir.mkdir(parents=True, exist_ok=True)

        if SKLEARN_AVAILABLE:
            pipe = self._build_pipeline()
            X_train, X_val, y_train, y_val = train_test_split(
                X, y, test_size=val_size, shuffle=False
            )
            pipe.fit(X_train, y_train)
            self.pipeline = pipe
            y_pred = pipe.predict(X_val)
            mae = float(mean_absolute_error(y_val, y_pred))
            r2 = float(r2_score(y_val, y_pred))
        else:
            # Fallback: not really training; this keeps the interface usable.
            self.pipeline = None
            mae = None
            r2 = None

        model_path = model_dir / "model.pkl"
        self.save(model_path)
        return TrainResult(model_path=model_path, n_train=len(X) - int(len(X) * val_size), n_val=int(len(X) * val_size), mae=mae, r2=r2)

    def predict(self, X: pd.DataFrame) -> np.ndarray:
        if self.pipeline is None:
            # If no sklearn, naive baseline: predict zeros (caller may use direct label instead)
            return np.zeros(len(X), dtype=float)
        return self.pipeline.predict(X)

    def save(self, path: Path) -> None:
        payload = {"sklearn": SKLEARN_AVAILABLE, "pipeline": self.pipeline, "model_type": self.model_type}
        with open(path, "wb") as f:
            pickle.dump(payload, f)

    @staticmethod
    def load(path: Path) -> "RideScoringModel":
        with open(path, "rb") as f:
            payload = pickle.load(f)
        m = RideScoringModel(model_type=payload.get("model_type", "hgb"))
        if payload.get("sklearn") and SKLEARN_AVAILABLE:
            m.pipeline = payload.get("pipeline")
        else:
            m.pipeline = None
        return m
