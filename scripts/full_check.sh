#!/bin/bash
set -e

echo "🔍 Running full system check..."

# 1. Check Docker containers
echo "➡️ Checking Docker containers..."
docker ps | grep "junctionx-unitud-2025-postgres" > /dev/null \
  && echo "✅ Postgres container running" \
  || (echo "❌ Postgres container not running" && exit 1)

docker ps | grep "junctionx-unitud-2025-pgadmin" > /dev/null \
  && echo "✅ pgAdmin container running" \
  || (echo "❌ pgAdmin container not running" && exit 1)

# 2. Check DB connection
echo "➡️ Checking database tables..."
docker exec -i junctionx-unitud-2025-postgres \
  psql -U app -d appdb -c "\dt" \
  || (echo "❌ Could not connect to database" && exit 1)

# 3. Check backend health
echo "➡️ Checking backend health..."
curl -sf http://localhost:8080/api/health | grep '"db":"up"' > /dev/null \
  && echo "✅ Backend connected to DB" \
  || (echo "❌ Backend not healthy" && exit 1)

echo ""
echo "🎉 ALL CHECKS PASSED — System is ready!"
