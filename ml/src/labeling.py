# coded with the aid of LLMs

"""
Label construction for ride scoring: a 0–100 rating combining
- Profit (net per hour)
- Destination opportunity (predicted EPH at drop)
- Driver wellbeing (fatigue, trip burden)

Weights (default): Profit 0.5, Opportunity 0.3, Wellbeing 0.2
"""

from __future__ import annotations

from dataclasses import dataclass
from typing import Tuple

import numpy as np
import pandas as pd


@dataclass(frozen=True)
class Weights:
    profit: float = 0.5
    opportunity: float = 0.3
    wellbeing: float = 0.2


def _robust_minmax(series: pd.Series, lo_q: float = 0.05, hi_q: float = 0.95) -> pd.Series:
    """Map a numeric series to [0,100] using robust quantile clipping.

    Values below lo_q go to 0; above hi_q go to 100; linear in-between.
    """
    s = series.astype(float)
    lo = s.quantile(lo_q)
    hi = s.quantile(hi_q)
    if not np.isfinite(lo) or not np.isfinite(hi) or hi <= lo:
        return pd.Series(np.full(len(s), 50.0), index=s.index)
    s = s.clip(lower=lo, upper=hi)
    scaled = (s - lo) / (hi - lo) * 100.0
    return scaled


def profit_score(df: pd.DataFrame) -> pd.Series:
    """Compute profit score from realized net earnings per hour.

    net = net_earnings + tips
    rate = net / (duration_hours)
    Scaled robustly to [0,100].
    """
    net = df.get("net_earnings", 0.0).astype(float) + df.get("tips", 0.0).astype(float)
    hours = df["duration_mins"].astype(float).replace(0, np.nan) / 60.0
    eph = net / hours
    eph = eph.replace([np.inf, -np.inf], np.nan).fillna(eph.median())
    return _robust_minmax(eph)


def opportunity_score(df: pd.DataFrame) -> pd.Series:
    """Score based on destination earning potential: predicted EPH at drop hex.

    Uses `predicted_eph_drop` and maps it to [0,100].
    """
    eph = df.get("predicted_eph_drop")
    if eph is None or eph.isna().all():
        return pd.Series(np.full(len(df), 50.0), index=df.index)
    s = eph.astype(float)
    # Fill missing EPH with median so downstream scaling does not propagate NaNs
    med = s.median()
    if not np.isfinite(med):
        return pd.Series(np.full(len(df), 50.0), index=df.index)
    s = s.fillna(med)
    return _robust_minmax(s)


def wellbeing_score(df: pd.DataFrame) -> pd.Series:
    """Composite of driver wellbeing factors.

    Components (equal weights):
    - Shorter trips preferred (inverse of duration)
    - Higher average speed preferred (less stop-and-go fatigue)
    - Lower active minutes since rest preferred (less fatigue)
    All components are mapped to [0,100] with robust scaling.
    """
    # Prepare component scores
    dur = _robust_minmax(df["duration_mins"].astype(float))
    speed = _robust_minmax(df["avg_speed_kmh"].astype(float))
    active = _robust_minmax(df["active_minutes_since_rest"].astype(float))

    comp = (100 - dur) * (1 / 3) + speed * (1 / 3) + (100 - active) * (1 / 3)
    return comp


def build_labels(df_enriched: pd.DataFrame, weights: Weights = Weights()) -> Tuple[pd.Series, pd.DataFrame]:
    """Compute the final rating (0–100) and return (y, components_df).

    components_df contains the individual component scores for diagnostics.
    """
    p = profit_score(df_enriched)
    o = opportunity_score(df_enriched)
    # For wellbeing we require features to be present; caller should have added them.
    w = wellbeing_score(df_enriched)

    rating = weights.profit * p + weights.opportunity * o + weights.wellbeing * w
    rating = rating.clip(lower=0, upper=100)

    comps = pd.DataFrame({"profit": p, "opportunity": o, "wellbeing": w})
    return rating, comps
