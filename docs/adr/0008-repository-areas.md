# ADR 0008 — Top-level areas

- Status: accepted
- Date: 2026-11-02

## Context

The product has an API, workers, a future analyst, a web client, a database, local and future cloud infrastructure, and a security bar that has to be reviewable on its own. Putting all of that under `apps/` hides the boundary between product code, operations, and policy.

## Decision

The repository root is split into areas, each with a README:

- `backend/` — API, ingestion, analyst boundary
- `frontend/` — web client
- `infra/` — container image, compose, Railway notes, CI ownership
- `security/` — threat model, policies, scanner configuration
- `docs/` — architecture, decisions, catalog, runbooks
- `db/` — versioned SQL
- `design/` — design originals, not runtime code

GitHub Actions requires workflow files in `.github/workflows`. Those workflows are part of `infra` even though the platform will not load them from `infra/`.

## Consequences

- A change to a scanner config is reviewed next to the threat model, not next to a React component.
- Deploy configuration cannot grow inside the web or API package unnoticed.
- `design/` stays a reference tree. The running web app copies only the tokens and the logo, and CI checks the copy.
