# Security

EcoGuard reads public statistics and, later, answers questions about them. Secrets stay in the environment or a secret manager. They are not committed. The local Postgres password in `infra/docker-compose.yml` is for that container only.

## Roles

| Role | Login | Privilege |
| --- | --- | --- |
| `ecoguard_ingest` | `NOLOGIN` until a password is set outside git | Write raw, staging, marts, ops |
| `ecoguard_api` | `NOLOGIN` | Read staging and marts |
| `ecoguard_agent` | `NOLOGIN` | Read `agent` views only |

The agent views are the SQL allowlist at the database. The application still has to restrict statements when the analyst is built. Details are in `security/llm-top10.md`.

## This foundation

- API rate limit, security headers, and a strict content security policy on the JSON API
- Web security headers. The web CSP still allows inline scripts because of the theme boot snippet. That gap is written down in `security/asvs.md`
- Input parsers reject malformed publisher payloads
- Analytics refuses personal-data property names and does not send events
- CI runs Dependabot configuration, CodeQL, semgrep, gitleaks, and Trivy

## Reporting

See `security/policies/disclosure.md`. Report issues privately to Marco. Do not file an exploit in a public issue.

## More

- `security/threat-model.md` — STRIDE
- `security/asvs.md` — ASVS level 2 target
- `security/lgpd.md` — personal data
- `security/README.md` — index
