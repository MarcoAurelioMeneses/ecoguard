# ADR 0003 — Supabase Postgres with PostGIS and pgvector

- Status: accepted
- Date: 2026-11-02

## Context

The product needs a relational store for indicators, geometries for 96 districts, and vector search over methodology documents. The MVP deployment target is Supabase. This foundation must not create a Supabase project.

District boundaries are geospatial. The official layer verified on 2026-11-02 is GeoSampa WFS `geoportal:distrito_municipal` in EPSG:31983 (SIRGAS 2000 / UTM zone 23S), 96 features, with a sample `dt_atualizacao` of 2026-10-27. MapLibre expects WGS84 longitude/latitude. The design mockups were drawn from a 2016 GeoJSON (`codigourbano/distritos-sp`, properties `ds_codigo` and `ds_nome`). Those polygons are stale and must not be loaded.

Sources also differ in size. InfoDengue and Open-Meteo return small JSON documents. DataSUS ships compressed DBF (DBC) files, IBGE ships GeoPackages, and INPE ships CSV trees. Putting every byte into a jsonb column is a poor fit for those files.

## Decision

Standardise on Postgres with the `postgis` and `vector` extensions. Local development uses the Postgres 18 image with `postgresql-18-postgis-3` and `postgresql-18-pgvector`. The package major must match the server. Schemas:

- `raw` — landed payloads keyed by source and natural key
- `staging` — typed cleaned tables, upserted on the same natural key
- `marts` — curated tables the API and the agent may read
- `ops` — job run records

`marts.districts.geom` is `geometry(MultiPolygon, 4326)`. The load job (not in this PR) transforms from EPSG:31983 and sets `source` to `geosampa:wfs:geoportal:distrito_municipal`. A check constraint rejects any other source label.

`marts.documents.embedding` is `vector(1536)`, the default width of OpenAI `text-embedding-3-small`. No rows are inserted.

JSON APIs write `raw` and `staging` directly. Bulky files will land in Supabase Storage first and then be parsed into the same schemas. Storage is not wired in this PR.

Roles and grants are in the migrations. Passwords are not.

## Consequences

- CI applies the same SQL a local container applies, including both extensions.
- Supabase can run these migrations later because the SQL uses core Postgres, PostGIS, and pgvector only.
- The 2016 GeoJSON cannot be inserted into `marts.districts` without changing the check constraint in a review.
- Embedding width is a commitment. A different model requires a migration.
- An empty `marts.documents` table does not mean retrieval works yet.
