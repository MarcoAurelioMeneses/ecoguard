# ADR 0011 — Analyst with typed tools

- Status: accepted
- Date: 2026-11-14
- Supersedes: [ADR 0006](0006-openai-langgraph-agent.md)

## Context

ADR 0006 left the analyst unimplemented and sketched a LangGraph loop with free text-to-SQL, `get_risk`, and `search_docs`, on `gpt-4o-mini`. That sketch is not the design.

The F0 security fix removes the free SQL tool. The analyst uses a dedicated login, `ecoguard_agent_login`, which is a member only of `ecoguard_agent`. The login has `default_transaction_read_only=on`, `statement_timeout=3s`, and `search_path=agent`. The API reaches it through a dedicated `DataSource`. There is no `SET ROLE` on the owner connection.

The whole analyst budget is about $5. On 2026-11-14 Marco decided not to create a dedicated OpenAI project for now, so `ops.llm_budget` is the only cap.

`gpt-4o-mini`, the model named in ADR 0006, shuts down on 2027-01-14.

## Decision

The analyst is a server-side loop of typed, read-only tools. This ADR does not implement it.

### Modules

- `domain.analyst` holds `SourceRef`, `Citation`, `AnalystAnswer`, `AnswerMode`, and `DegradedReason`.
- `application.analyst` is pure Java: `AskAnalyst`, `AnalystModeResolver`, `DeterministicAnalyst`, `IntentParser`, `DistrictNameResolver`, `AnswerValidator`, and `CostEstimator`.
- The `agent` module holds only adapters.
- ArchUnit: Spring AI only in `agent`; `@Tool` only in `agent.tools`; `api.web` does not depend on `agent`.

### Endpoints

- `POST /v1/analyst/questions`
- `POST /v1/analyst/questions/stream` (SSE)
- OpenAPI operation ids: `askAnalyst` and `askAnalystStream`
- `400` and `429` are `ProblemDetail`

### Tools

Eight typed, read-only tools. They read `agent.*` views and those views have no geometry. A tool result is capped at 2,000 tokens.

| Tool | Bounds |
| --- | --- |
| `getDistrictRisk(district, date?)` | |
| `rankDistricts(pillar, order, limit, date?)` | `limit` at most 10 |
| `compareDistricts(districts, date?)` | at most 5 districts |
| `getDistrictHistory(district, from, to, indicators)` | window at most 92 days |
| `getCitySummary(date?)` | |
| `listActiveAlerts(district?, severity?, type?)` | at most 20 |
| `searchMethodology(query, k)` | query at most 300 characters, `k` at most 4 |
| `getRiskConfig()` | |

Views that do not exist yet stay behind feature flags, default off: `agent.district_indicators`, `agent.city_summary`, `agent.alerts`, `agent.sources`, and `agent.methodology_chunks`. `agent.district_risk_scores` must expose coverage, `grain_note`, and `computed_at` before the score tools rely on it.

### Retrieval

The corpus is repository docs only, indexed by the `methodology-index` job. Chunks follow H2 and H3 headings, about 500 tokens, with about 60 tokens of overlap, embedded with `text-embedding-3-small`. Only changed content is re-embedded. Rows live in `marts.methodology_chunks` with an HNSW index and are read through `agent.methodology_chunks`. Without an API key, search falls back to Portuguese full-text search. If `PgVectorStore` cannot read a view, the adapter is JDBC.

### Citations and validation

The server assigns refs `c1`, `c2`, and so on. The model only writes `[cN]`. The server builds `citations[]` from those refs. A figure at municipal grain gets an automatic caveat.

`AnswerValidator` requires every number in the answer to appear in this question's tool results, and every `[cN]` to exist. The answer has no URLs outside citations, no HTML, and no images. On failure the response is the deterministic answer with `degradedReason=validation_failed`. A tolerance is allowed so a formatting difference is not a false failure.

### Limits per question

At most 4 model calls and 6 tool executions, in a manual loop. `max_tokens` is 600. The question is at most 500 characters. Each model call times out at 20 seconds, and the question times out at 30 seconds.

### Modes

In F0, a missing key returns 503.

In F2, a missing key, an exhausted budget, an LLM error, or a validation failure all return 200 with `mode=degraded`, a template answer, and the same tools and citations. 503 is only for a tool failure or a database failure.

### Budget

| Pool | Amount |
| --- | --- |
| Production | $4 |
| Daily cap | $0.20 |
| Eval | $0.50 |
| Reserve | $0.50 |

Before each model call the API role writes an atomic worst-case reservation in `ops.llm_budget`, then settles it from `ops.llm_usage`. Alert at 80 percent. Responses are cached for 1 hour.

`ecoguard.analyst.model` defaults to `gpt-6-luna`. Prices are properties, `ecoguard.analyst.pricing.<model>.{input,cached-input,output}`, in USD per 1,000,000 tokens, so a price change does not require a code change.

A typical question is about 4,400 input tokens and 430 output tokens, about $0.00066, which is about 6,100 questions on $4. The worst case inside the caps is about 19,400 input tokens and 900 output tokens, about $0.0024.

### Observability

Langfuse receives traces through OTLP. Attributes: `mode`, `degraded_reason`, `iterations`, `tool_calls`, `cost_usd`, `validation_passed`, `cache_hit`. Prompt content is traced only when `ECOGUARD_TRACE_CONTENT=true`. PII is masked. The client IP is not traced.

### Rate limit

`/v1/analyst/**` allows 5 requests per minute and 30 per day per IP, 1 concurrent request, and 300 per day globally. The real client IP comes from `forward-headers-strategy=framework`.

### Alternatives rejected

Text-to-SQL with an allowlist, an unverified free LLM, and a LangGraph or Python analyst.

### Risks

| Risk | Mitigation |
| --- | --- |
| Model tool behaviour | A golden set before the model is enabled |
| Price changes | Pricing properties |
| Validator false negatives | Tolerance |
| Injection through alert titles | Data-only delimiters |
| Budget abuse | Reservation, daily cap, and the analyst rate limit |
| `PgVectorStore` rejects a view | JDBC adapter |
| `agent.*` views arrive late | Feature flags, default off |

### Tests

Unit tests cover district typos, including Sapopema to Sapopemba and Agua rasa to Água Rasa. Testcontainers covers the role escape and the budget race. A golden set of about 20 cases lives at `agent/src/test/resources/evals/golden-set.pt-BR.json` and includes prompt injection. Every CI run executes it in degraded mode. LLM mode is a manual job.

### F2 acceptance

1. The 503 transition above, with tests.
2. 100 percent of golden answers pass the validator.
3. Injection cases stay contained.
4. A question never makes more than 4 model calls.
5. Role and budget tests stay green under concurrency.
6. A Langfuse trace carries the cost of the question, and the analyst rate limit returns 429.
7. ArchUnit, OpenAPI, `docs/api.md`, and `SECURITY.md` are updated in the same change.

## Consequences

- ADR 0006 is historical. Its free-SQL tool, its `gpt-4o-mini` default, and its LangGraph choice are not the design.
- Nothing in the agent module changes with this record. Until F2, a missing key still returns 503.
- The views named above are added by their own migrations. This ADR does not create them.
