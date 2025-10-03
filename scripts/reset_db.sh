#!/bin/bash
set -e
echo "⚠️  Resetting Postgres schema 'public' (drops ALL objects)..."
docker exec -i junctionx-unitud-2025-postgres \
  psql -U app -d appdb < scripts/reset.sql
echo "✅ Schema recreated."
echo "🌱 (optional) Reseeding..."
if [ -f scripts/seed.sql ]; then
  docker exec -i junctionx-unitud-2025-postgres \
    psql -U app -d appdb < scripts/seed.sql
  echo "✅ Seed applied."
else
  echo "ℹ️  No scripts/seed.sql found. Skipping seed."
fi
