# ADR 0010 — Product analytics with PostHog

- Status: accepted
- Date: 2026-11-02

## Context

We need to know which screens, map layers, districts, and analyst questions people use, without building a profile of a person. The audience includes residents in Brazil. LGPD applies. The MVP has no accounts.

## Decision

Use PostHog, self-hosted, as the product analytics store when it is deployed. Until then the frontend and the backend share an event taxonomy (`docs/analytics.md`) and a tracker that validates names and refuses personal-data keys. The tracker does not set a cookie and does not send a request.

When PostHog is switched on:

- cookieless mode, or a consent banner before any cookie
- no user id, no IP stored as a property, no question text
- the PostHog host is our deployment, not a surprise third party
- the web CSP `connect-src` is updated in the same change

OpenTelemetry remains the trace, metric, and log path for the API and the workers. Langfuse remains the path for model calls. They are not substitutes for product analytics, and product analytics is not a place to put prompts.

## Consequences

- Screen placeholders already emit `screen_viewed` into the in-memory tracker, so the call sites exist before a vendor does.
- A future SDK addition that posts raw questions will fail the banned-key check if it goes through `track`.
- Self-hosting PostHog is an infrastructure milestone. It is not in `docker-compose.yml`.
