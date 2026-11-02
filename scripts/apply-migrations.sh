#!/usr/bin/env bash
# Apply baseline SQL to an already-running Postgres. Safe to re-run:
# objects use IF NOT EXISTS and the weight row uses ON CONFLICT DO NOTHING.
set -euo pipefail

if [[ -z "${ECOGUARD_DATABASE_URL:-}" ]]; then
  echo "ECOGUARD_DATABASE_URL is required" >&2
  exit 1
fi

root="$(cd "$(dirname "$0")/.." && pwd)"
for file in "$root"/db/migrations/*.sql; do
  echo "Applying $(basename "$file")"
  psql "$ECOGUARD_DATABASE_URL" -v ON_ERROR_STOP=1 -f "$file"
done
