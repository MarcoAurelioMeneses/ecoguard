# Runbook — ingestion

## What a run does

`run_ingestion` calls the source adapter, upserts `raw.source_payloads`, upserts the staging table, and inserts `ops.job_runs`. A failed extract rolls the data back and still records a failed run.

Natural keys:

| Source | Key |
| --- | --- |
| InfoDengue | geocode, disease, epidemiological week start |
| Open-Meteo | `location_id`, forecast date |
| GeoSampa districts | `district_code` in `marts.districts` |

A second run with the same payload does not write a new raw row. A changed forecast or a revised dengue week updates the existing row.

## Commands

```bash
./backend/gradlew -p backend :ingestion:bootRun --args='--ecoguard.job=infodengue'
./backend/gradlew -p backend :ingestion:bootRun --args='--ecoguard.job=open-meteo'
./backend/gradlew -p backend :ingestion:bootRun --args='--ecoguard.job=geosampa-districts'
./backend/gradlew -p backend :ingestion:bootRun --args='--ecoguard.job=risk-scores'
```

InfoDengue defaults to São Paulo (`3550308`), disease `dengue`, and the previous calendar year through the current one, one HTTP request per year. Open-Meteo defaults to the city reference point `-23.5505, -46.6333`, timezone `America/Sao_Paulo`, seven forecast days, location id `sp-city-reference`. GeoSampa pages the WFS layer `geoportal:distrito_municipal` and writes `marts.districts`. A response that is not 96 features fails the run.

## When it fails

| Symptom | What to check |
| --- | --- |
| Exit code 2 | `ECOGUARD_DATABASE_URL` is unset and `--dry-run` was not passed |
| HTTP 503 from InfoDengue | `ops.job_runs.error`. Retry later. The job is safe to rerun |
| Open-Meteo `error: true` | The adapter raises. No partial staging commit |
| `relation does not exist` | Migrations did not run. See the local-development runbook |
| GeoSampa feature count is not 96 | The job fails and writes a failed `ops.job_runs` row. Do not fill the gap from `design/src/distritos-sp.geojson` |

Do not point a new worker at CETESB or CEMADEN until `docs/data-sources.md` says they are programmatically accessible. Do not load `design/src/distritos-sp.geojson` into `marts.districts`.

## Freshness

`MAX(extracted_at)` on the staging table is the freshness signal. The hour labels in the design mockups are not SLAs.
