# Postgres 18 from the official image. PostGIS and pgvector must be the
# matching major (postgresql-18-*), not the 16 packages Dependabot left in place.
# Those 16 packages install, but CREATE EXTENSION postgis fails on the 18 server.
FROM postgres:18-bookworm

USER root
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        postgresql-18-postgis-3 \
        postgresql-18-pgvector \
    && rm -rf /var/lib/apt/lists/*

# Stay root for the official entrypoint. It chowns the data directory, then
# drops to the postgres user with gosu before the server accepts connections.
# A fresh volume is root-owned; USER postgres would make initdb fail.

HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=12 \
  CMD ["sh", "-c", "pg_isready -U \"${POSTGRES_USER:-ecoguard}\" -d \"${POSTGRES_DB:-ecoguard}\""]
