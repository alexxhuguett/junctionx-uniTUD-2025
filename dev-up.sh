#!/usr/bin/env bash
# Simple dev orchestrator for the JunctionX project.
# Runs: Python ML server, Java Spring Boot backend, and Frontend dev server.
#
# Usage (from repo folder: junctionx-uniTUD-2025):
#   bash dev-up.sh
#
# Env overrides (optional):
#   PY_HOST=0.0.0.0 PY_PORT=8000 FRONTEND_DIR=frontend FRONTEND_CMD="npm run dev" bash dev-up.sh

set -euo pipefail

REPO_DIR=$(pwd)
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
LOG_DIR="$REPO_DIR/logs"
PIDS_DIR="$REPO_DIR/.pids"
mkdir -p "$LOG_DIR" "$PIDS_DIR"

# Config
PY_HOST=${PY_HOST:-0.0.0.0}
PY_PORT=${PY_PORT:-8000}
JAVA_HOST=${JAVA_HOST:-127.0.0.1}
JAVA_PORT=${JAVA_PORT:-8080}
PYTHONPATH_ENV=${PYTHONPATH_ENV:-.}
PY_EXCEL=${PY_EXCEL:-ml/data/uber_hackathon_v2_mock_data.xlsx}
PY_MODEL=${PY_MODEL:-ml/artifacts/model.pkl}
VENV_DIR=${VENV_DIR:-.venv}
REQUIREMENTS_TXT=${REQUIREMENTS_TXT:-ml/requirements.txt}

# Find frontend folder if not provided
detect_frontend() {
  local candidates=(frontend web ui app client)
  for d in "${candidates[@]}"; do
    if [[ -f "$REPO_DIR/$d/package.json" ]]; then
      echo "$d"
      return 0
    fi
  done
  return 1
}

FRONTEND_DIR=${FRONTEND_DIR:-}
if [[ -z "${FRONTEND_DIR}" ]]; then
  if fd=$(detect_frontend); then
    FRONTEND_DIR="$fd"
  else
    echo "[warn] Could not auto-detect frontend folder. Set FRONTEND_DIR env var if needed." >&2
  fi
fi

FRONTEND_CMD=${FRONTEND_CMD:-npm run dev}
FRONTEND_INSTALL=${FRONTEND_INSTALL:-npm install}

# ----------------------- helpers (must be before first call) -----------------------
# Return 0 if port is free, 1 if in use
port_free() {
  local host=$1 port=$2
  if command -v lsof >/dev/null 2>&1; then
    if lsof -iTCP -sTCP:LISTEN -n -P 2>/dev/null | grep -q ":$port\\>"; then
      return 1
    else
      return 0
    fi
  elif command -v nc >/dev/null 2>&1; then
    if nc -z "$host" "$port" >/dev/null 2>&1; then
      return 1
    else
      return 0
    fi
  else
    # Python fallback connect check
    python3 - "$host" "$port" <<'PY' >/dev/null 2>&1 || exit 1
import socket, sys
h, p = sys.argv[1], int(sys.argv[2])
s = socket.socket()
s.settimeout(0.3)
try:
    s.connect((h, p))
    sys.exit(1)  # in use
except Exception:
    sys.exit(0)  # free
PY
    return $?
  fi
}

find_free_port() {
  local host=$1
  local start=${2:-0}
  local max=$(( start + 50 ))
  local p=$start
  while [ $p -le $max ]; do
    if port_free "$host" "$p"; then
      echo "$p"; return 0
    fi
    p=$((p+1))
  done
  echo "$start"  # fallback
}

wait_port_open() {
  local host=$1 port=$2 timeout=${3:-30} label=${4:-port}
  echo "[dev-up] Waiting for $label on $host:$port (timeout ${timeout}s)..."
  local t=0
  while [ $t -lt $timeout ]; do
    if ! port_free "$host" "$port"; then
      echo "[dev-up] $label is up on $host:$port"
      return 0
    fi
    sleep 1; t=$((t+1))
  done
  echo "[warn] Timed out waiting for $label on $host:$port" >&2
  return 1
}

ensure_python_venv() {
  # Create venv if missing and install requirements
  if [[ ! -d "$REPO_DIR/$VENV_DIR" ]]; then
    echo "[ml] Creating Python virtualenv at $VENV_DIR ..."
    python3 -m venv "$REPO_DIR/$VENV_DIR"
  fi
  VENV_PY="$REPO_DIR/$VENV_DIR/bin/python"
  VENV_PIP="$REPO_DIR/$VENV_DIR/bin/pip"
  # Upgrade pip and install dependencies if requirements present
  "$VENV_PIP" install --upgrade pip >/dev/null 2>&1 || true
  if [[ -f "$REPO_DIR/$REQUIREMENTS_TXT" ]]; then
    echo "[ml] Installing Python dependencies from $REQUIREMENTS_TXT ..."
    "$VENV_PIP" install -r "$REPO_DIR/$REQUIREMENTS_TXT" >>"$LOG_DIR/python.log" 2>&1 || {
      echo "[warn] pip install encountered issues; check $LOG_DIR/python.log" >&2
    }
  else
    echo "[ml] No requirements file at $REQUIREMENTS_TXT; skipping dependency install."
  fi
  export VENV_PY
}

start_python() {
  # pick a free port for ML if desired one is busy
  PY_PORT=$(find_free_port "$PY_HOST" "$PY_PORT")
  echo "[ml] Starting Python ML server on $PY_HOST:$PY_PORT ..."
  (
    export PYTHONPATH="$PYTHONPATH_ENV"
    exec "$VENV_PY" -m ml.server \
      --excel "$PY_EXCEL" \
      --model "$PY_MODEL" \
      --host "$PY_HOST" \
      --port "$PY_PORT"
  ) >"$LOG_DIR/python.log" 2>&1 &
  echo $! > "$PIDS_DIR/python.pid"
  wait_port_open "$PY_HOST" "$PY_PORT" 30 "ML"
}

start_java() {
  # pick a free port for Java API if 8080 is busy
  JAVA_PORT=$(find_free_port "$JAVA_HOST" "$JAVA_PORT")
  echo "[api] Starting Java Spring Boot backend on $JAVA_HOST:$JAVA_PORT via ./mvnw ..."
  (
    cd "$REPO_DIR/backend"
    exec ./mvnw -q spring-boot:run -Dspring-boot.run.arguments="--server.port=$JAVA_PORT"
  ) >"$LOG_DIR/java.log" 2>&1 &
  echo $! > "$PIDS_DIR/java.pid"
  wait_port_open "$JAVA_HOST" "$JAVA_PORT" 60 "Java"
}

start_frontend() {
  if [[ -z "${FRONTEND_DIR}" ]]; then
    echo "[fe] Skipping frontend (FRONTEND_DIR not set and not detected)." >&2
    return 0
  fi
  echo "[fe] Starting Frontend in $FRONTEND_DIR ..."
  (
    cd "$REPO_DIR/$FRONTEND_DIR"
    # Install deps if node_modules missing
    if [[ ! -d node_modules ]]; then
      echo "[fe] Installing dependencies ..." >&2
      $FRONTEND_INSTALL >&2
    fi
    exec $FRONTEND_CMD
  ) >"$LOG_DIR/frontend.log" 2>&1 &
  echo $! > "$PIDS_DIR/frontend.pid"
}

stop_all() {
  echo "\n[dev-up] Stopping processes ..."
  for f in "$PIDS_DIR"/*.pid; do
    [[ -f "$f" ]] || continue
    pid=$(cat "$f" || true)
    if [[ -n "$pid" ]] && kill -0 "$pid" >/dev/null 2>&1; then
      kill "$pid" >/dev/null 2>&1 || true
    fi
    rm -f "$f"
  done
}

trap stop_all EXIT INT TERM

ensure_python_venv
start_python
start_java
start_frontend

echo "[dev-up] All services launched. Logs: $LOG_DIR"
echo "[dev-up] Python ML:   http://$PY_HOST:$PY_PORT/simulation"
echo "[dev-up] Java API:    check $LOG_DIR/java.log for port info (Spring Boot)"
echo "[dev-up] Frontend:    check $LOG_DIR/frontend.log for dev server URL"

# Tail the three logs (Ctrl+C to stop all)
tail -n +1 -F "$LOG_DIR"/*.log || true
