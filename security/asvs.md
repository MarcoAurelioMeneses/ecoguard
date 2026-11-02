# OWASP ASVS Level 2 — target

ASVS 5.0 level 2 is the target for the deployed product. This foundation meets the items that already have a surface, and records the rest as work before launch. This is not an attestation.

| Area | Now | Before a public launch |
| --- | --- | --- |
| Architecture | Documented context, containers, and roles | Review when the analyst and auth, if any, are added |
| Authentication | No accounts, so no session | Only if accounts are added, which is outside the MVP |
| Session | None | Same |
| Access control | Database roles and grants, agent views | Enforce the SQL allowlist in the tool, statement timeout |
| Validation | Pydantic settings, typed parsers, analytics key denylist | Validate every new query parameter and path parameter the same way |
| Cryptography | TLS is the publisher and browser default. No custom crypto | TLS on the deployed hosts, secrets in a manager |
| Error handling | Failures become a job row or an HTTP error. No stack traces configured | Keep debug off in production |
| Data protection | No personal data stored. LGPD note written | Retention for analytics and logs |
| Communication | Security headers on the API and the web app | Tighten CSP `unsafe-inline` with nonces. The inline boot script and Next.js need it today |
| Malicious code | Dependabot, CodeQL, semgrep, gitleaks, Trivy | Review the scanner findings, do not silence them without a written exception |
| Configuration | `.env.example` has empty secrets. Local DB password is named as local-only | Separate production config. No compose password |
| Rate limiting | Per-process window on the API | A shared limiter when the API is replicated |

The web CSP allows `'unsafe-inline'` for scripts and styles because the theme boot script and Next.js inject inline tags. That is a known gap against a strict CSP. It is called out here so it is not mistaken for a finished control.
