#!/usr/bin/env bash
# Apply Flyway SQL in version order. The API and the ingestion process also
# run these migrations on startup. This script is for a database that is up
# before either process.
set -euo pipefail

if [[ -z "${ECOGUARD_DATABASE_URL:-}" ]]; then
  echo "ECOGUARD_DATABASE_URL is required (postgresql://..., not a JDBC URL)" >&2
  exit 1
fi

root="$(cd "$(dirname "$0")/.." && pwd)"
mapfile -t files < <(find "$root/db/migration" -name 'V*.sql' | sort -V)
for file in "${files[@]}"; do
  echo "Applying $(basename "$file")"
  psql "$ECOGUARD_DATABASE_URL" -v ON_ERROR_STOP=1 -f "$file"
done
