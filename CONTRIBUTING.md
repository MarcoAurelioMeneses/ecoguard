# Contributing

Conventions for EcoGuard. The layout and the reason for it are in `docs/architecture.md`.

## Where a change goes

| Change | Directory |
| --- | --- |
| A score rule, a record, an event name, a port | `backend/domain` |
| A use case | `backend/application` |
| A collector or JDBC upsert | `backend/ingestion` |
| An HTTP route | `backend/api` |
| An analyst tool | `backend/agent` |
| A screen | `frontend/features/<screen>` with `ui`, and `state` or `data` when they exist |
| Shared theme, routes, analytics | `frontend/shared` |
| SQL | `db/migration` as the next Flyway version. Do not edit a migration that has already been applied in a shared database. This repository has not been applied anywhere but a local volume |
| Containers, CI | `infra/` and `.github/workflows` |
| Threats, scanner rules | `security/` |
| A decision that is hard to reverse | `docs/adr` |

`domain` and `application` do not depend on Spring. Collectors, JDBC, and the HTTP API stay in their modules.

## Dependencies

Pass ports into use cases. Do not construct a Postgres connection inside a use case. Do not call PostHog, OpenAI, or Langfuse from a feature component. Go through the analytics tracker or an adapter.

## Data

- Confirmed overall weights are 0.40, 0.35, and 0.25. Change them in `marts.risk_score_config`, not in a second constant in the UI.
- Do not seed mockup numbers.
- Do not load `design/src/distritos-sp.geojson` into PostGIS or the map. It is the 2016 `codigourbano/distritos-sp` extract (`ds_codigo`, `ds_nome`, CRS84), kept so the design scripts can be re-run. Ingestion uses the current GeoSampa WFS layer `geoportal:distrito_municipal`.
- Screen 00 is the web root, `/`.

## Checks

```bash
make lint
make test
```

CI also runs gitleaks, semgrep, Trivy, and CodeQL. Do not commit secrets. The local database password is allowlisted in `security/.gitleaks.toml` and must not be copied into a deployment.

## Language

Code, commits, and documents in this repository are English. The design originals in `design/` stay in the language they were delivered in.
