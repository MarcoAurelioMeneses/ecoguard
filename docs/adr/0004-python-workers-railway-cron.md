# ADR 0004 — Python workers invoked by cron

- Status: accepted
- Date: 2026-11-02

## Context

Each publisher has its own cadence, from 10-minute fire detections to a decennial census. The MVP does not need a stream processor or a resident queue. Jobs must be testable without the network and safe to run twice.

The planned host is Railway cron. This PR does not create a Railway project or a schedule.

## Decision

Ingestion is the `run_ingestion` use case in `backend/`. A source adapter extracts. The use case always does three steps:

1. `extract` — HTTP (or, later, a file download) into in-memory records
2. `load_raw` — upsert `raw.source_payloads` on `(source, external_key)`
3. `load_staging` — upsert the typed staging table on its natural key

A run record is committed to `ops.job_runs` after a successful commit, or after a rollback if the write fails. Extract failures never leave a partial batch committed.

The reference jobs are InfoDengue (dengue, municipality of São Paulo, epidemiological weeks) and Open-Meteo (daily forecast for one city reference coordinate). Both are idempotent: a second run with the same keys updates rows in place.

Jobs are a CLI (`python -m ecoguard.interfaces.cli infodengue`). Cron will call that command. `--dry-run` executes extract and the upserts against an in-memory store so a developer can see counts without a database.

HTTP is `httpx`. Tests use `httpx.MockTransport`. Live calls are not part of the test suite.

## Consequences

- A new source is an adapter that returns domain records. The use case owns run accounting and the upserts.
- Railway cron is a thin scheduler. The job does not import a Railway SDK.
- Long historical backfills are a loop of the same upsert, not a second code path.
- In-process pandas or polars transforms can sit between extract and staging later without changing the schema boundary.
- There is no queue, retry broker, or distributed lock yet. Cron intervals must be longer than a normal run, and the upsert makes overlap safe for these two sources.
