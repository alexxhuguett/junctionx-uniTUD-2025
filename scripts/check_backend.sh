#!/bin/bash
set -e
echo "Checking backend health..."
curl -sf http://localhost:8080/api/health | grep '"db":"up"' \
  && echo "✅ Backend OK" \
  || (echo "❌ Backend not healthy" && exit 1)
