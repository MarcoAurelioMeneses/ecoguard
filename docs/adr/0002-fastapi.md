# ADR 0002 — FastAPI for the HTTP API

- Status: accepted
- Date: 2026-11-02

## Context

The API must be typed, publish an OpenAPI document, and later host a LangGraph analyst next to ordinary JSON endpoints (district scores, freshness, alerts). Readers are unauthenticated in the MVP. Writes from the public internet are out of scope.

## Decision

Use FastAPI on Python 3.12 with Pydantic response models. Settings come from `pydantic-settings` and the `ECOGUARD_` environment prefix. This foundation exposes:

- `GET /health` — process liveness
- `GET /` — pointers to health and the OpenAPI document
- `/openapi.json` and `/docs` — generated schema

The app is created in `create_app()` so tests do not depend on a developer `.env`.

## Consequences

- OpenAPI stays aligned with the Python types instead of a hand-written spec.
- The analyst can live in the same process later and share settings, logging, and the read-only database role.
- Rate limiting, CORS, and auth are not pretending to exist. They will be added when a browser client on another origin calls the API. Permissive CORS is intentionally absent.
- The web placeholder does not call the API yet.
