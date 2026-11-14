# Backend

Java 21, Spring Boot 3.5, Gradle Kotlin DSL. One hexagonal build, four modules plus the use-case module.

| Module | Role |
| --- | --- |
| `domain` | Records, weights, JTS points, analytics names, ports. No Spring. |
| `application` | `RunIngestion`. No Spring. |
| `ingestion` | InfoDengue and Open-Meteo collectors, JDBC upserts, Flyway, scheduled entrypoint |
| `api` | Spring Web, springdoc, Spring Security, validation, rate limit |
| `agent` | Spring AI tools over `agent` views, `gpt-4o-mini` when `OPENAI_API_KEY` is set |

```bash
./gradlew check
./gradlew :api:bootRun
./gradlew :ingestion:bootRun --args='--ecoguard.job=infodengue'
```

From the repository root, prefix with `./backend/gradlew -p backend`.

ErrorProne is optional: `./gradlew check -Perrorprone=true`. SpotBugs runs on `domain` and `application`. Spotless formats Java. JaCoCo writes reports under each module `build/reports/jacoco`. OWASP Dependency-Check is `dependencyCheckAnalyze` and runs in CI.

Collectors are covered with WireMock. The JDBC upsert test uses Testcontainers and `infra/postgres/Dockerfile` (PostGIS and pgvector). It runs when Docker is available.

The analyst has no free-SQL tool. It connects as `ecoguard_agent` and may only read `agent` views. Document search orders `agent.documents` by pgvector distance. Without `OPENAI_API_KEY` the route returns 503 and the model bean is not created. The model is not called in tests.
