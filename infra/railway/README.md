# Railway

Service definitions for a deploy you create yourself. The runbook is [DEPLOY.md](DEPLOY.md). Nothing in this directory creates a Railway project, service, or variable.

| Path | Service |
| --- | --- |
| `postgres/railway.json` | Postgres 18 + PostGIS + pgvector, image `infra/postgres/Dockerfile`, volume `/var/lib/postgresql` |
| `api/railway.json` | API. The only Flyway runner. `GET /health` |
| `ingestion/railway.json` | Cron backfill and score job. Flyway off. No public port |
| `frontend/railway.json` | Next.js. `GET /health` |

Root directory for every service is the repository root.
