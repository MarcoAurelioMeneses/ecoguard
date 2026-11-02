# CI

GitHub Actions loads workflow files only from `.github/workflows`. Those files are part of infrastructure. They are not under `infra/` because the platform would ignore them there.

| Workflow | What it runs |
| --- | --- |
| `ci.yml` | Gradle check (JUnit, Spotless, Checkstyle, SpotBugs, JaCoCo), OWASP Dependency-Check, web lint and build, gitleaks, semgrep, Trivy |
| `codeql.yml` | CodeQL for Python and JavaScript |

Dependabot is `.github/dependabot.yml`. Scanner rules are under `security/`.
