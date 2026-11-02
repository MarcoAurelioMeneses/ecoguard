# ADR 0009 — Hexagonal backend

- Status: accepted
- Date: 2026-11-02

## Context

The API, the collectors, and the analyst share records, weights, and the rule that a source fetch must not know how a row is stored. Spring, RestClient, and JDBC are delivery and driver choices. They should be replaceable without rewriting the score rule or the upsert use case.

## Decision

Gradle modules under `backend/`: `domain` (model and ports), `application` (use cases), `ingestion` (collectors and JDBC), `api` (HTTP), and `agent` (Spring AI tools). `domain` and `application` do not depend on Spring.

The ingestion use case depends on `SourcePort` and `IngestionStore`. InfoDengue and Open-Meteo are collectors. Postgres is the JDBC adapter. HTTP is the `api` module. Tests of the use case do not open a socket.

## Consequences

- Adding a source means a new adapter and a branch in the composition root, not a new framework.
- The HTTP process and the cron process import the same package and call different interfaces.
- There is no DI container. Wiring is explicit and short. A container can wait until the graph of dependencies is no longer obvious.
