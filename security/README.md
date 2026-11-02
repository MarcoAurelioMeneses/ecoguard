# Security

This directory is the security baseline for the foundation. It is not a certification.

| Document | What it decides |
| --- | --- |
| [threat-model.md](threat-model.md) | STRIDE for the system as designed |
| [asvs.md](asvs.md) | OWASP ASVS level 2 as the target, and what this PR already does |
| [llm-top10.md](llm-top10.md) | OWASP LLM risks for the analyst, including the controls that exist before the model is wired |
| [lgpd.md](lgpd.md) | Personal data, analytics, and public statistics |
| [policies/disclosure.md](policies/disclosure.md) | How to report a vulnerability |

The disclosure copy also lives at the repository root in `SECURITY.md`, because that is the path people and scanners look for.

Scanner configuration:

| Control | Config | Where it runs |
| --- | --- | --- |
| Dependency updates | `.github/dependabot.yml` | GitHub Dependabot |
| SAST | `semgrep.yml` and CodeQL | `.github/workflows` |
| Secret scanning | `.gitleaks.toml` | gitleaks in CI |
| Container and config scan | `trivy.yaml` | Trivy in CI |

Secrets are environment variables. Roles are `NOLOGIN` until a password is set outside git. The analyst role can `SELECT` from the `agent` views only.
