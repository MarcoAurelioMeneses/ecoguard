# Multi-stage build. The runtime image is a JRE 21 and does not run as root.
# Flyway is forced off. The API service is the only migrator.
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /src
COPY backend /src/backend
COPY db /src/db
WORKDIR /src/backend
RUN ./gradlew :ingestion:bootJar --no-daemon

FROM eclipse-temurin:21-jre-jammy

RUN groupadd --system --gid 10001 ecoguard \
    && useradd --system --uid 10001 --gid ecoguard --home-dir /app --shell /usr/sbin/nologin ecoguard
WORKDIR /app
COPY --from=build --chown=ecoguard:ecoguard /src/backend/ingestion/build/libs/ecoguard-ingestion.jar /app/ecoguard-ingestion.jar
USER ecoguard

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"
CMD ["java", "-jar", "/app/ecoguard-ingestion.jar", "--ecoguard.job=backfill", "--spring.main.web-application-type=none", "--spring.flyway.enabled=false"]
