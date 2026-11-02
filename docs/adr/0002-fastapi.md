# ADR 0002 — Spring Boot for the HTTP API

- Status: accepted
- Date: 2026-11-02
- Supersedes the earlier FastAPI choice. The backend is Java.

## Context

The API must be typed, publish an OpenAPI document, and later host the city analyst next to ordinary JSON endpoints. Readers are unauthenticated in the MVP. Writes from the public internet are out of scope.

## Decision

Use Java 21 and Spring Boot 3 on the `api` module. springdoc publishes `/openapi.json` and `/docs`. Spring Security sets headers and a content security policy and permits the public reads. Bean validation covers the analyst question body. Settings come from the environment.

This foundation exposes `GET /health`, `GET /`, and `POST /analyst/questions`. The analyst answers only when `OPENAI_API_KEY` is set. Otherwise the route returns 503.

## Consequences

- OpenAPI stays aligned with the controller types.
- The analyst module is on the API classpath and uses the same security filter chain.
- A replicated API still needs a shared rate limiter. The current limiter is in-process.
