# EcoGuard

City risk intelligence for São Paulo: environmental, health, and social public data at district grain where the source allows it, and an AI City Analyst that cites sources and dates. This repository is the foundation. The screens are designed and not built. Nothing here is deployed.

## Layout

| Path | What it is |
| --- | --- |
| [backend/](backend/README.md) | Java 21, Spring Boot 3, Gradle. API, collectors, analyst |
| [frontend/](frontend/README.md) | Next.js. `/` is screen 00, What is EcoGuard |
| [infra/](infra/README.md) | Local PostGIS image and compose. Railway is documented, not applied |
| [security/](security/README.md) | STRIDE, ASVS L2 target, LLM risks, LGPD, scanners |
| [docs/](docs/README.md) | Architecture, ADRs, catalog, runbooks, analytics taxonomy |
| [db/](db/README.md) | SQL migrations, roles, agent views |
| [design/](design/README.md) | Design originals, including the 2016 district fixture |

Start with [docs/onboarding.md](docs/onboarding.md). Conventions are in [CONTRIBUTING.md](CONTRIBUTING.md).

## Score

Overall score = `0.40 * environmental + 0.35 * health + 0.25 * social`. Marco confirmed that split. The numbers live in `marts.risk_score_config` (`status = confirmed`) so they can change without a code edit. Indicator sub-weights are still a draft. See [docs/risk-methodology.md](docs/risk-methodology.md).

## Districts

The choropleth source is GeoSampa WFS `geoportal:distrito_municipal` (96 features, EPSG:31983, verified 2026-11-02). `design/src/distritos-sp.geojson` is the 2016 `codigourbano` extract used only to regenerate mockups.

## What is not accessible yet

Detail is in [docs/data-sources.md](docs/data-sources.md).

| Source | Result of the check on 2026-11-02 |
| --- | --- |
| InfoDengue, Open-Meteo | Anonymous JSON works. Workers included |
| GeoSampa districts, INPE 10-minute CSV, IBGE SIDRA table 4709 | Anonymous access works. Not loaded |
| INMET | Station list works. Hourly observations did not |
| CETESB QUALAR | Login-gated. No public API |
| CEMADEN | HTML download form. No anonymous API verified |
| DataSUS SIH | DBC files. The file host timed out. Not a JSON API |

Health series that work are municipal, not district. The methodology says so instead of painting a fake pattern.

## Run locally

Requirements: Java 21, Node 22, pnpm 10, Docker.

```bash
cp .env.example .env
pnpm install
docker compose -f infra/docker-compose.yml up -d --build --wait
./backend/gradlew -p backend :api:bootRun
./backend/gradlew -p backend :ingestion:bootRun --args='--ecoguard.job=infodengue'
./backend/gradlew -p backend :ingestion:bootRun --args='--ecoguard.job=open-meteo'
pnpm --filter @ecoguard/web dev
```

Flyway applies `db/migration` when the API or the ingestion process starts. The ingestion command runs one collector and then the process exits if that is all it was asked to do; leave `ECOGUARD_JOB` unset to keep the process up without a one-shot run.

The database password in compose is for that local container only. More steps are in [docs/runbooks/local-development.md](docs/runbooks/local-development.md).

```bash
make lint
make test
```

## Security

[SECURITY.md](SECURITY.md). Secrets stay in the environment. The agent role is read-only on `agent` views. CI runs gitleaks, semgrep, Trivy, CodeQL, and Dependabot is configured.

## Next milestones

1. Load GeoSampa `distrito_municipal` into `marts.districts`. Do not import the 2016 fixture.
2. Ingest INPE hotspots and IBGE census tracts joined to those districts. Resolve an INMET token before that worker.
3. Decide whether CETESB and CEMADEN credentials will exist. Until then those indicators stay null.
4. Confirm a live DataSUS DBC download, then a municipal respiratory series.
5. Compute scores from `marts.risk_score_config` only. Sub-weights in the methodology are still a draft.
6. Read API for districts, scores, and freshness.
7. Build screens 00–06 from `design/html` on the routes that already exist. No observability page until it is designed.
8. LangGraph analyst on the agent views, with the SQL guard, Langfuse, and OpenTelemetry.
9. Self-hosted PostHog, cookieless or consent-based, wired to the existing event taxonomy. No question text.
10. Replace the in-process rate limit when the API runs as more than one process. Replace `'unsafe-inline'` in the web CSP with nonces.
11. Deploy only when asked.
