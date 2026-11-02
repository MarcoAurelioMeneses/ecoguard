# Railway — planned, not applied

Do not run `railway up` from this repository. No project, service, or variable is created here.

When a deploy is requested, the intended services are:

| Service | Process | Notes |
| --- | --- | --- |
| `api` | `java -jar ecoguard-api.jar` | Health check `GET /health` |
| `worker-infodengue` | `java -jar ecoguard-ingestion.jar --ecoguard.job=infodengue` | Cron, weekly |
| `worker-open-meteo` | `java -jar ecoguard-ingestion.jar --ecoguard.job=open-meteo` | Cron, several times a day |
| `web` | `frontend` Next.js | CSP and analytics host must be set together |

The database stays on Supabase, not on a Railway Postgres, so PostGIS and pgvector stay on the managed instance that matches `db/migration`. Secrets are Railway variables, the same names as `.env.example`, with real values only in the dashboard.

Open-Meteo’s free tier is non-commercial. A public commercial deploy needs their customer host and a key.
