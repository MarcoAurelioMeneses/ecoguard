# ADR 0006 — OpenAI gpt-4o-mini and a LangGraph analyst

- Status: accepted
- Date: 2026-11-02

## Context

The AI City Analyst answers questions about São Paulo districts. It must call tools, cite the source and the date, and refuse to invent measurements. The MVP model is OpenAI `gpt-4o-mini`. The orchestration library is LangGraph, with tools:

- text-to-SQL restricted to an allowlist of marts relations
- `get_risk(district, date)`
- `search_docs` over the methodology corpus in `marts.documents`

The database role `ecoguard_agent` can `SELECT` from `marts` only. That is necessary and not sufficient: the SQL tool still needs an allowlist, a statement timeout, and a single-statement guard.

## Decision

Adopt that design and do not implement the agent in this PR. No OpenAI client, LangGraph graph, or prompt file is shipped, so there is nothing to call by accident. `OPENAI_API_KEY` is reserved in `.env.example` and left empty.

When the agent is built it will:

- read scores from `marts`, including the weight row, rather than from the prompt
- cite the publisher and `extracted_at` or `published_on` on every numeric claim
- treat InfoDengue and SIH as municipal, not district, unless a later mart says otherwise
- ignore mockup copy in `design/` as evidence

## Consequences

- The foundation review is about data contracts, not prompt behavior.
- Model choice can be revisited without a migration, except the embedding width locked in ADR 0003.
- Prompt-injection handling is specified in `SECURITY.md` and implemented with the agent, not before there is a prompt.
