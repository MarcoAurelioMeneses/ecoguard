# Onboarding

Read these in order:

1. [README](../README.md) — what the system is and how to run it
2. [architecture.md](architecture.md) — context, containers, and where code goes
3. [data-sources.md](data-sources.md) — what can actually be fetched
4. [risk-methodology.md](risk-methodology.md) — the confirmed weights and the draft indicators
5. [../security/README.md](../security/README.md) — the threat model and the scanners
6. [CONTRIBUTING.md](../CONTRIBUTING.md) — how to change the code

Local setup is the runbook [runbooks/local-development.md](runbooks/local-development.md). Ingestion is [runbooks/ingestion.md](runbooks/ingestion.md).

You do not need a Supabase project, a Railway account, an OpenAI key, or a PostHog project to run the foundation. Do not create those resources unless someone asks.

The design package in `design/` is the visual reference. Screen 00 is the product entry and the web route `/`. The file `design/src/distritos-sp.geojson` is a 2016 fixture for regenerating mockups. The map's source of truth is the live GeoSampa WFS layer documented in the catalog.
