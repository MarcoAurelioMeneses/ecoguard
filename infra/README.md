# Infrastructure

Local runtime only. This directory does not create Railway or Supabase resources.

| Path | Purpose |
| --- | --- |
| `postgres/Dockerfile` | Postgres 16 (Debian bookworm) with PostGIS and pgvector. The server process runs as `postgres` |
| `docker-compose.yml` | Local database. Password `ecoguard` is local-only |
| `railway/README.md` | Planned services, not applied |
| `ci/README.md` | Why workflows live in `.github/workflows` |

```bash
docker compose -f infra/docker-compose.yml up -d --build --wait
```

Migrations are mounted from `db/migrations` into the Postgres entrypoint. They run when the volume is created.
