#Important

export PYTHONPATH=junctionx-uniTUD-2025

do this from parent of project root. 

Ride Scoring ML

Overview
- Predicts how good a ride is for a driver (0–100).
- Optimizes for driver profit, wellbeing, and destination opportunity.

Structure
- `src/data_loader.py` — reads Excel, joins earner + destination context.
- `src/features.py` — fatigue (active minutes since 15m rest), speed, time.
- `src/labeling.py` — builds 0–100 rating from Profit/Opportunity/Wellbeing.
- `src/model.py` — sklearn pipeline + save/load wrapper.
- `train_model.py` — CLI to train and save model.
- `score_trip.py` — CLI to score rides using saved model.

Usage
Train (run from repo root):
  python -m ml.train_model \
    --excel junctionx-uniTUD-2025/ml/data/uber_hackathon_v2_mock_data.xlsx \
    --outdir junctionx-uniTUD-2025/ml/artifacts

Score an existing ride by `ride_id` (quick sanity):
  python -m ml.score_trip \
    --excel junctionx-uniTUD-2025/ml/data/uber_hackathon_v2_mock_data.xlsx \
    --model junctionx-uniTUD-2025/ml/artifacts/model.pkl \
    --ride-id <uuid>

List rides with ratings:
  # 20 random rides
  python -m ml.score_trip \
    --excel junctionx-uniTUD-2025/ml/data/uber_hackathon_v2_mock_data.xlsx \
    --model junctionx-uniTUD-2025/ml/artifacts/model.pkl \
    --sample 20

  # top 20 rides by predicted rating
  python -m ml.score_trip \
    --excel junctionx-uniTUD-2025/ml/data/uber_hackathon_v2_mock_data.xlsx \
    --model junctionx-uniTUD-2025/ml/artifacts/model.pkl \
    --top 20

Serve predictions (HTTP)
- Install Flask:
  python3 -m pip install flask

- Run server (from project root):
  PYTHONPATH=junctionx-uniTUD-2025 python3 -m ml.server \
    --excel junctionx-uniTUD-2025/ml/data/uber_hackathon_v2_mock_data.xlsx \
    --model junctionx-uniTUD-2025/ml/artifacts/model.pkl \
    --host 0.0.0.0 --port 8000

- Endpoints:
  GET http://localhost:8000/health
  GET http://localhost:8000/prediction/<ride_id>
  GET http://localhost:8000/prediction/top/<n>

Notes
- If scikit-learn is unavailable, training falls back to a no-op model; you can
  still compute labels directly or install sklearn to enable training.
- Destination opportunity uses `heatmap.predicted_eph` at the drop hex.
- Fatigue uses cumulative minutes driven since the last >=15m gap.

Schema tools
- Print the model feature schema as JSON:
  PYTHONPATH=junctionx-uniTUD-2025 python3 -m ml.tools.schema --print

- Validate a CSV file against the schema (all columns present and basic types):
  PYTHONPATH=junctionx-uniTUD-2025 python3 -m ml.tools.schema --validate-csv path/to/features.csv
