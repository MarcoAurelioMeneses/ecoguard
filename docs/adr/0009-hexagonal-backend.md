# ADR 0009 — Hexagonal backend

- Status: accepted
- Date: 2026-11-02

## Context

The API, the workers, and the future analyst share records, weights, and the rule that a source fetch must not know how a row is stored. FastAPI, httpx, and psycopg are delivery and driver choices. They should be replaceable without rewriting the score rule or the upsert use case.

## Decision

One package, `ecoguard`, with `domain`, `application`, `ports`, `adapters`, `interfaces`, and a `composition` root. The analyst lives in `ecoguard.agent` as ports only until a graph is built.

The ingestion use case depends on `SourcePort` and `IngestionStore`. InfoDengue and Open-Meteo are adapters. Postgres and the in-memory store are adapters. CLI and HTTP are interfaces. Tests of the use case do not open a socket and do not import FastAPI.

A unit test rejects framework imports inside `domain`, `application`, and `ports`.

## Consequences

- Adding a source means a new adapter and a branch in the composition root, not a new framework.
- The HTTP process and the cron process import the same package and call different interfaces.
- There is no DI container. Wiring is explicit and short. A container can wait until the graph of dependencies is no longer obvious.
