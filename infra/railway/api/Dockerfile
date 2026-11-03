# Multi-stage build. The runtime image is a JRE 21, not a JDK, and does not run as root.
# Flyway stays enabled: this is the only service that migrates on startup.
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /src
COPY backend /src/backend
COPY db /src/db
WORKDIR /src/backend
RUN ./gradlew :api:bootJar --no-daemon

FROM eclipse-temurin:21-jre-jammy

RUN groupadd --system --gid 10001 ecoguard \
    && useradd --system --uid 10001 --gid ecoguard --home-dir /app --shell /usr/sbin/nologin ecoguard
WORKDIR /app
COPY --from=build --chown=ecoguard:ecoguard /src/backend/api/build/libs/ecoguard-api.jar /app/ecoguard-api.jar
USER ecoguard

EXPOSE 8000
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"
CMD ["java", "-jar", "/app/ecoguard-api.jar"]
