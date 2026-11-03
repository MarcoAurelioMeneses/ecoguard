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
USER postgres
