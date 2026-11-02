# Threat model (STRIDE)

Scope: the foundation as it will run when the API, workers, database, web app, and analyst exist. Items marked "now" have a control in this repository. Items marked "later" are required before that part is exposed. There is no production deployment.

Trust boundaries: the browser, the public API, the worker process, the database roles, the model provider, and the publisher networks.

| Threat | Property | Where | What we do |
| --- | --- | --- | --- |
| Spoofing | Authentication | Public API | The MVP has no accounts. There is no session to steal. A future login is out of scope and would need its own review. |
| Spoofing | Worker identity | Database | Workers are meant to use `ecoguard_ingest`, not the migration superuser. The local compose user is a development convenience and is documented as local-only. |
| Tampering | Source payloads | Ingestion | Upserts are keyed. Raw payloads are not writable by the API or the agent. |
| Tampering | District geometry | `marts.districts` | A check constraint allows only the GeoSampa source label. The 2016 GeoJSON cannot be inserted under that rule. |
| Tampering | Analyst SQL | `agent` schema | The role has no privilege on base tables. The future SQL tool must still be a single `SELECT` against those views, with a statement timeout. The tool is not built yet. |
| Tampering | Supply chain | Dependencies | Dependabot, plus CI scans. Lockfiles are committed. |
| Repudiation | Pipeline runs | `ops.job_runs` | Each run records status, counts, and the error string. There are no user actions to repudiate yet. |
| Information disclosure | Secrets | Repo and logs | Secrets are env vars. Gitleaks runs in CI. Logs must not include payloads or connection strings. |
| Information disclosure | Raw rows | Agent | `ecoguard_agent` cannot read `raw` or `staging`. |
| Information disclosure | Analytics | Browser | The tracker refuses personal-data keys and does not transmit. See `docs/analytics.md`. |
| Information disclosure | Model provider | Analyst, later | Send the question and the tool result, not secrets and not raw payloads. |
| Denial of service | API | HTTP | In-process rate limit on routes other than health and docs. A shared store is a later replacement if more than one API process runs. |
| Denial of service | Publisher | Workers | Timeouts on HTTP. Jobs are safe to rerun. No unbounded fan-out in the two reference jobs. |
| Elevation of privilege | SQL injection | Analyst, later | Read-only role, views as the allowlist, application guard still required. |
| Elevation of privilege | Prompt injection | Analyst, later | Retrieved text is data. See `llm-top10.md`. |
| Elevation of privilege | XSS | Web | Security headers and a CSP. React escapes text. Mockup HTML is not served. |

Abuse of the free Open-Meteo tier for a commercial launch is a licensing issue, not an implementation bug. The catalog states the limit.
