# Runbook — local development

## Install

Requirements: Java 21, Node 22, pnpm 10, Docker with Compose.

```bash
cp .env.example .env
pnpm install
docker compose -f infra/docker-compose.yml up -d --build --wait
```

The database password in compose is for this container only. Do not reuse it.

Flyway applies `db/migration` when the API or the ingestion process starts. To apply the same files with `psql` to a database that is already up:

```bash
export ECOGUARD_DATABASE_URL=postgresql://ecoguard:ecoguard@localhost:5432/ecoguard
./scripts/apply-migrations.sh
```

Reset the volume:

```bash
docker compose -f infra/docker-compose.yml down -v
```

## Processes

API (Flyway runs on startup):

```bash
./backend/gradlew -p backend :api:bootRun
```

One collector:

```bash
./backend/gradlew -p backend :ingestion:bootRun --args='--ecoguard.job=infodengue'
./backend/gradlew -p backend :ingestion:bootRun --args='--ecoguard.job=open-meteo'
./backend/gradlew -p backend :ingestion:bootRun --args='--ecoguard.job=geosampa-districts'
```

Web:

```bash
pnpm --filter @ecoguard/web dev
```

Open `http://127.0.0.1:3000`. That route is screen 00. `/map` and `/analyst` are the calls to action. The map lists districts only after the GeoSampa job has run. Score charts stay on the no-data state.

## Checks

```bash
make lint
make test
```

`make test` runs Gradle. The JDBC Testcontainers test runs when Docker is available, which it is in CI.
