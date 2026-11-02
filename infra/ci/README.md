# CI

GitHub Actions loads workflow files only from `.github/workflows`. Those files are part of infrastructure. They are not under `infra/` because the platform would ignore them there.

| Workflow | What it runs |
| --- | --- |
| `ci.yml` | ruff, mypy, pytest against PostGIS, web lint and build, gitleaks, semgrep, Trivy |
| `codeql.yml` | CodeQL for Python and JavaScript |

Dependabot is `.github/dependabot.yml`. Scanner rules are under `security/`.
