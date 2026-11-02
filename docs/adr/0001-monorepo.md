# ADR 0001 — Monorepo for API, workers, web, and SQL

- Status: accepted
- Date: 2026-11-02

## Context

EcoGuard ships one product with four kinds of code that must stay in step: a typed HTTP API, ingestion jobs, a web client, and the database contract those processes share. District codes, score weights, and source names will be referenced from more than one of them.

## Decision

Keep a single git repository.

- `backend/` — Java modules, managed with Gradle
- `frontend/` — Next.js application, managed with pnpm
- `db/migrations` — ordered SQL, applied by Postgres on first container start and by `scripts/apply-migrations.sh`
- `infra/`, `security/`, `docs/`, and `design/` — operations, security, architecture, and the design source of truth

Java uses Gradle under `backend/`. JavaScript uses a pnpm workspace that contains `frontend`. Design files stay in `design/` and are copied into the web app only when the UI must serve them (`tokens.css`, logo SVGs). A CI diff keeps those copies identical. Directory names were refined in ADR 0008. The monorepo decision is unchanged.

## Consequences

- One CI workflow typechecks both stacks.
- A schema change and the worker that writes it land in the same review.
- The repository holds design PNG and HTML originals. They are reference material, not the running app.
- Publishing independent packages is unnecessary at this size. If a package needs its own release later, the workspace layout still allows it.
