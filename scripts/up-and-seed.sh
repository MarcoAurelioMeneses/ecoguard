#!/usr/bin/env bash
# Start local Postgres and load anonymous São Paulo data, then compute scores.
set -euo pipefail
root="$(cd "$(dirname "$0")/.." && pwd)"
docker compose -f "$root/infra/docker-compose.yml" up -d --build --wait
export ECOGUARD_BACKFILL_DAYS="${ECOGUARD_BACKFILL_DAYS:-30}"
"$root/backend/gradlew" -p "$root/backend" :ingestion:bootRun \
  --args="--ecoguard.job=backfill --spring.main.web-application-type=none"
echo "Seed finished. Start the API with ./backend/gradlew -p backend :api:bootRun"
