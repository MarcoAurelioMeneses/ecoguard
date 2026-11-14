# ADR 0011 — Analyst with typed tools

- Status: accepted
- Date: 2026-11-14
- Supersedes: [ADR 0006](0006-openai-langgraph-agent.md)

## Context

ADR 0006 left the analyst unimplemented and sketched a LangGraph loop with free text-to-SQL, `get_risk`, and `search_docs`. Free SQL is not an acceptable tool. The analyst login can read `agent.*` views only, and a model must not be able to send a statement.

`gpt-4o-mini` shuts down on 14 January 2027. The replacement named by OpenAI is `gpt-6-luna`. The price row for that model is recorded in ADR 0012 when that record is present. This ADR does not depend on it.

The product needs an answer the server can check: citation markers come from the server, and every number in the answer has to appear in the cited tool result. A question also needs a hard stop on model calls, tool executions, and daily spend.

This is the decision record. Vision's longer draft replaces the narrative in place when it is final. The decisions below are the ones already accepted.

## Decision

The analyst is one HTTP endpoint, `POST /v1/analyst/questions`. It is not implemented in this change.

Tools are typed functions with a fixed argument schema. There is no free-SQL tool and no path that accepts a statement from the model.

One question may start at most 4 model calls and 6 tool executions. The fifth model call and the seventh tool execution are refused.

Citations are server-generated `[cN]` markers. The model does not choose the numbers. Before the response is sent, the server checks that every numeric claim in the answer appears in the tool result attached to its marker.

A daily cost cap, in USD, bounds model spend. Usage is priced with the published Standard rates for the configured model. When the day's total reaches the cap, the endpoint does not start another model call.

The model id is the property `ecoguard.analyst.model`. The default is `gpt-6-luna`.

Degraded mode, in F2, answers with a template and HTTP 200 when the model is not configured. HTTP 503 is reserved for a tool failure or a database failure. Until F2, a missing API key still returns 503. A tool or database failure returns 503 in both phases.

These tools read `agent.*` views that do not exist yet, and they start disabled behind feature flags that default to off:

- `district_indicators`
- `city_summary`
- `alerts`
- `sources`
- `methodology_chunks`

A flag stays off until its view exists. Enabling the flag is a later change, not part of this record.

## Consequences

- ADR 0006 is historical. Its free-SQL tool, its `gpt-4o-mini` default, and its LangGraph choice are not the design.
- Nothing in the agent module changes with this ADR. The current missing-key behavior remains 503 until F2.
- The dollar amount of the daily cap is configuration, not a figure in this record.
- Views the flags wait on are added by their own migrations. This ADR does not create them.
