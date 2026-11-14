# Infrastructure

Local runtime only. This directory does not create Railway or Supabase resources.

| Path | Purpose |
| --- | --- |
| `postgres/Dockerfile` | Postgres 18 (Debian bookworm) with `postgresql-18-postgis-3` and `postgresql-18-pgvector`. The 16 packages do not serve `CREATE EXTENSION` on this server. The compose volume is `/var/lib/postgresql` |
| `docker-compose.yml` | Local database. Password `ecoguard` is local-only |
| `railway/DEPLOY.md` | Railway runbook: four services, private network, variables. Not applied from this repo |
| `ci/README.md` | Why workflows live in `.github/workflows` |

```bash
docker compose -f infra/docker-compose.yml up -d --build --wait
```

Migrations are mounted from `db/migrations` into the Postgres entrypoint. They run when the volume is created.
