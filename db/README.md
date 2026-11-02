# Database

Ordered Flyway SQL in `migration/` (`V1__...` through `V11__...`). A new change is the next version. The API and the ingestion process apply them on startup. `scripts/apply-migrations.sh` applies the same files with `psql` when you want the schema before either process. The baseline creates:

- extensions `postgis` and `vector`
- schemas `raw`, `staging`, `marts`, `ops`, and `agent`
- roles `ecoguard_ingest`, `ecoguard_api`, `ecoguard_agent`, all `NOLOGIN`
- InfoDengue and Open-Meteo staging tables
- empty `marts.districts` with a check that the source is the GeoSampa WFS layer
- the confirmed weight row 0.40 / 0.35 / 0.25
- `agent` views that are the only objects the analyst role can read

`marts.districts` starts empty. Do not load `design/src/distritos-sp.geojson`. That file is the 2016 `codigourbano/distritos-sp` extract (properties `ds_codigo` and `ds_nome`, CRS84). The load job, when it exists, reads GeoSampa `geoportal:distrito_municipal` (EPSG:31983, 96 features, verified 2026-11-02) and stores EPSG:4326.

Apply to an existing database:

```bash
export ECOGUARD_DATABASE_URL=postgresql://ecoguard:ecoguard@localhost:5432/ecoguard
../../scripts/apply-migrations.sh
```

From the repository root, `./scripts/apply-migrations.sh`.
