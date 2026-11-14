# Deploy EcoGuard on Railway

This runbook prepares four services. It does not create them. Create the project and services in the Railway dashboard, from the repository, with the settings below. Do not run `railway up` from a checkout until those services exist and you intend to deploy.

The database is the custom Postgres 18 image in `infra/postgres` (PostGIS and pgvector). Do not add Railway's managed Postgres. That image does not match the extensions in `db/migration`.

## Services

Every service uses the repository root as its root directory. The config-file path is absolute inside the repo. A narrower root directory breaks the Docker build, because the API and ingestion images copy `backend/` and `db/`, and the frontend image copies the pnpm workspace.

| Service | Config file | Image | Listens | Public |
| --- | --- | --- | --- | --- |
| `postgres` | `infra/railway/postgres/railway.json` | `infra/postgres/Dockerfile` | 5432 | No |
| `api` | `infra/railway/api/railway.json` | `infra/railway/api/Dockerfile` | `$PORT` | Yes |
| `ingestion` | `infra/railway/ingestion/railway.json` | `infra/railway/ingestion/Dockerfile` | none (cron exits) | No |
| `frontend` | `infra/railway/frontend/railway.json` | `infra/railway/frontend/Dockerfile` | `$PORT` | Yes |

Create them in that order. The API is the only process that runs Flyway. Ingestion refuses to migrate (`--spring.flyway.enabled=false` in its start command and image `CMD`).

## Private network

Railway gives every service a private DNS name. Leave Postgres and ingestion without a public domain and without a public TCP proxy. The browser cannot resolve `railway.internal`, so the frontend's public origin calls the API's public origin. Ingestion talks only to Postgres.

| From | To | Address |
| --- | --- | --- |
| `api`, `ingestion` | `postgres` | `${{postgres.RAILWAY_PRIVATE_DOMAIN}}` port **5432** |
| browser | `api` | `https://${{api.RAILWAY_PUBLIC_DOMAIN}}` |
| browser | `frontend` | `https://${{frontend.RAILWAY_PUBLIC_DOMAIN}}` |

Generate the public domains for `api` and `frontend` before the first frontend build. `NEXT_PUBLIC_API_BASE_URL` is baked into the client bundle at build time.

## Variables

Set these in the service variables UI. Do not commit values. References such as `${{postgres.POSTGRES_PASSWORD}}` are Railway variable references, not literals.

### `postgres`

| Variable | Value | Secret |
| --- | --- | --- |
| `POSTGRES_USER` | `ecoguard` | No |
| `POSTGRES_PASSWORD` | a password you generate in the dashboard | Yes |
| `POSTGRES_DB` | `ecoguard` | No |

Attach a volume mounted at `/var/lib/postgresql`. Postgres 18 keeps its data under `/var/lib/postgresql/18/docker` inside that mount. `requiredMountPath` in the config file makes a deploy without this volume fail. The image healthcheck is `pg_isready`. Railway's HTTP healthcheck does not apply to this service.

The entrypoint starts as root only to chown that volume, then the server runs as the `postgres` user.

### `api`

Flyway runs on startup because `spring.flyway.enabled` stays at its default, `true`. Do not set `SPRING_FLYWAY_ENABLED=false` here, and do not add a pre-deploy migrate command. One migrator is enough.

| Variable | Value | Secret |
| --- | --- | --- |
| `ECOGUARD_JDBC_URL` | `jdbc:postgresql://${{postgres.RAILWAY_PRIVATE_DOMAIN}}:5432/${{postgres.POSTGRES_DB}}` | No |
| `ECOGUARD_DB_USER` | `${{postgres.POSTGRES_USER}}` | No |
| `ECOGUARD_DB_PASSWORD` | `${{postgres.POSTGRES_PASSWORD}}` | Yes |
| `ECOGUARD_ENVIRONMENT` | `production` | No |
| `ECOGUARD_WEB_ORIGIN` | `https://${{frontend.RAILWAY_PUBLIC_DOMAIN}}` | No |
| `OPENAI_API_KEY` | empty until the analyst should answer; empty returns 503 for that route | Yes |
| `ECOGUARD_OPEN_METEO_API_KEY` | empty on the free non-commercial tier | Yes |
| `OTEL_EXPORT_ENABLED` | `false` unless a collector is configured | No |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | collector URL, only when export is enabled | No |

Railway sets `PORT`. The API binds to it. The healthcheck is `GET /health` and allows 180 seconds for the JVM and Flyway.

### `ingestion`

Cron is Railway's `0 9 * * *` (09:00 UTC, 06:00 in `America/Sao_Paulo`). The process runs `backfill` (GeoSampa districts, InfoDengue, district weather and air quality, INPE fires, IBGE census, then scores) and must exit. Leave `ECOGUARD_SCHEDULE_CRON` unset. Setting it starts a second, in-process scheduler and the JVM will not exit, so the next Railway run is skipped.

There is no HTTP healthcheck. A zero exit is the healthy run. `ON_FAILURE` retries a non-zero exit up to 3 times.

| Variable | Value | Secret |
| --- | --- | --- |
| `ECOGUARD_JDBC_URL` | same reference as the API | No |
| `ECOGUARD_DB_USER` | `${{postgres.POSTGRES_USER}}` | No |
| `ECOGUARD_DB_PASSWORD` | `${{postgres.POSTGRES_PASSWORD}}` | Yes |
| `SPRING_FLYWAY_ENABLED` | `false` | No |
| `ECOGUARD_BACKFILL_DAYS` | `30` (maximum 92) | No |
| `ECOGUARD_OPEN_METEO_API_KEY` | empty on the free tier | Yes |

Do not give this service a public domain.

### `frontend`

| Variable | Value | Secret |
| --- | --- | --- |
| `NEXT_PUBLIC_API_BASE_URL` | `https://${{api.RAILWAY_PUBLIC_DOMAIN}}` | No |
| `NEXT_PUBLIC_POSTHOG_KEY` | empty, or both this and the host | Yes |
| `NEXT_PUBLIC_POSTHOG_HOST` | empty, or both this and the key | No |

Set the public API URL before the first build, then rebuild after any change to a `NEXT_PUBLIC_` variable. The healthcheck is `GET /health`.

## Bring-up

1. Deploy `postgres`. Wait until `pg_isready` would succeed (the log line `database system is ready to accept connections`).
2. Deploy `api`. Wait until `GET /health` on its public URL returns `"status": "ok"`. That deploy created the schema.
3. Deploy `ingestion`. Trigger the cron once from the service (Run now) so the map has rows before 09:00 UTC. Confirm the logs end with the score upsert and the process exits 0.
4. Deploy `frontend`. Open the public site, then the map.

Changing `db/migration` redeploys the API through its watch pattern. Ingestion picks up the new schema on its next run. It does not migrate.

## Local images

From the repository root, the same Dockerfiles build without Railway:

```bash
docker build -f infra/postgres/Dockerfile infra/postgres
docker build -f infra/railway/api/Dockerfile .
docker build -f infra/railway/ingestion/Dockerfile .
docker build -f infra/railway/frontend/Dockerfile --build-arg NEXT_PUBLIC_API_BASE_URL=http://localhost:8000 .
```

The local database for development remains `infra/docker-compose.yml`. Its password is local-only and is not the Railway password.

## Commercial weather

Open-Meteo's free host is non-commercial and the data is CC BY 4.0. A commercial Railway project needs their customer host and `ECOGUARD_OPEN_METEO_API_KEY` on both `api` (unused by the score job) and `ingestion`. See `docs/data-sources.md`.
