# OWASP LLM Top 10 — analyst

The analyst is not implemented. These are the controls that are already in place, and the ones that have to land in the same change as the graph. The ports are `ecoguard.agent`.

| Risk | Control |
| --- | --- |
| LLM01 Prompt injection | Retrieved bulletins and rows are data. The system instruction says so. Tool output is not executed as a new instruction. A user cannot expand the tool list. |
| LLM02 Sensitive information disclosure | The model is not given secrets, raw payloads, or connection strings. Analytics must not receive the question text. |
| LLM03 Supply chain | The model name is an explicit dependency (`gpt-4o-mini`), pinned in configuration, not inferred from a webpage. |
| LLM04 Data and model poisoning | The RAG corpus is loaded by the ingest role into `marts.documents`, not by the agent. Documents have a source and a date. |
| LLM05 Improper output handling | Answers are rendered as text. HTML from the model is not injected. Charts are built from tool JSON, not from a string the model asks us to `eval`. |
| LLM06 Excessive agency | Tools are the three ports: risk lookup, allowlisted SQL, document search. No email, no writes, no arbitrary HTTP. |
| LLM07 System prompt leakage | The prompt is not a secret. It also does not contain credentials. Leakage of the prompt is not a vulnerability by itself. |
| LLM08 Vector and embedding weaknesses | Embeddings live in `marts.documents`. The agent reads through a view that should expose title, body, source, and date, not an unfiltered operator table. The view is added when documents exist. |
| LLM09 Misinformation | Numbers must come from a tool result that includes the publisher and the date. The model does not fill a null pillar. |
| LLM10 Unbounded consumption | `gpt-4o-mini`, a max token limit, a timeout, and the API rate limit. Langfuse records usage when it is wired. |

SQL guardrails, in addition to the prompt:

1. The connection uses `ecoguard_agent`.
2. That role can read `agent.*` views only. Grants are in `db/migrations/0010_agent_views.sql`.
3. The tool, when written, allows one statement, must start with `SELECT`, and rejects comments, semicolons, and any schema other than `agent`.
4. A statement timeout is set on the connection.

Output handling: the HTTP API returns JSON. The web app will not pass model text to `dangerouslySetInnerHTML`.
