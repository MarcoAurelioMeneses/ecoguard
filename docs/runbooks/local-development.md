# Runbook — local development

## Install

Requirements: Java 21, Node 22, pnpm 10, Docker with Compose.

```bash
cp .env.example .env
pnpm install
docker compose -f infra/docker-compose.yml up -d --build --wait
```

The database password in compose is for this container only. Do not reuse it.

Migrations in `db/migrations` run on the first start of the volume. To apply them again to a database that already exists:

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
```

Web:

```bash
pnpm --filter @ecoguard/web dev
```

Open `http://127.0.0.1:3000`. That route is screen 00. `/map` and `/analyst` are the calls to action. They are placeholders.

## Checks

```bash
make lint
make test
```

`make test` runs Gradle. The JDBC Testcontainers test runs when Docker is available, which it is in CI.
