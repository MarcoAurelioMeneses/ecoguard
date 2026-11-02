# ADR 0007 — Langfuse and OpenTelemetry

- Status: accepted
- Date: 2026-11-02

## Context

Two different signals matter. Model calls need prompt, tool, token, and latency traces. Pipelines and the API need vendor-neutral traces, metrics, and logs, plus a way to see that a source stopped updating. The optional status screen was not designed (there is no screen 7) and is out of the UI scope.

## Decision

Use Langfuse for LLM traces and OpenTelemetry for API and worker spans, exported over OTLP. Environment names are reserved in `.env.example`:

- `LANGFUSE_PUBLIC_KEY`, `LANGFUSE_SECRET_KEY`, `LANGFUSE_HOST`
- `OTEL_EXPORTER_OTLP_ENDPOINT`, `OTEL_SERVICE_NAME`

Neither SDK is a dependency yet. What exists now is `ops.job_runs`: each worker run records source, status, timestamps, row counts, and the error string. Freshness checks will query `MAX(extracted_at)` per staging table against the cadence in the data catalog. They will not trust the lag hints printed on the design mockups.

Logs go to stderr. They must not include secrets or full source payloads.

## Consequences

- A later change can attach an OTel exporter without renaming tables.
- There is no dashboard in this repository and no plan to build screen 7 until a design exists.
- Langfuse keys are secrets and follow `SECURITY.md`.
