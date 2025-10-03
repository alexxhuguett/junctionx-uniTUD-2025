#!/bin/bash
set -e
echo "Listing tables in appdb..."
docker exec -i junctionx-unitud-2025-postgres \
  psql -U app -d appdb -c "\dt"
