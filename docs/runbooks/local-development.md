# Runbook — local development

## Install

Requirements: Python 3.12, uv, Node 22, pnpm 10, Docker with Compose.

```bash
cp .env.example .env
uv sync --all-packages --group dev
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

API:

```bash
uv run uvicorn ecoguard.interfaces.http.app:app --app-dir backend/src --reload --port 8000
```

Workers:

```bash
uv run python -m ecoguard.interfaces.cli infodengue
uv run python -m ecoguard.interfaces.cli open-meteo
```

`--dry-run` uses the in-memory store and still calls the public API.

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

`make test` skips the Postgres tests unless `ECOGUARD_DATABASE_URL` is set. CI sets it and fails if the variable is missing.
