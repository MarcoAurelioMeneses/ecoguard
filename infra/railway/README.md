# Railway — planned, not applied

Do not run `railway up` from this repository. No project, service, or variable is created here.

When a deploy is requested, the intended services are:

| Service | Process | Notes |
| --- | --- | --- |
| `api` | `uvicorn ecoguard.interfaces.http.app:app --app-dir backend/src` | Health check `GET /health` |
| `worker-infodengue` | `python -m ecoguard.interfaces.cli infodengue` | Cron, weekly |
| `worker-open-meteo` | `python -m ecoguard.interfaces.cli open-meteo` | Cron, several times a day |
| `web` | `frontend` Next.js | CSP and analytics host must be set together |

The database stays on Supabase, not on a Railway Postgres, so PostGIS and pgvector stay on the managed instance that matches `db/migrations`. Secrets are Railway variables, the same names as `.env.example`, with real values only in the dashboard.

Open-Meteo’s free tier is non-commercial. A public commercial deploy needs their customer host and a key.
