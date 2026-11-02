package com.ecoguard.ingestion.support;

import java.nio.file.Files;
import java.nio.file.Path;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;

public final class PostgresFixture {
  private PostgresFixture() {}

  @SuppressWarnings("resource")
  public static PostgreSQLContainer<?> start() {
    Path dockerfile = Path.of("infra/postgres/Dockerfile");
    if (!Files.exists(dockerfile)) {
      dockerfile = Path.of("../../infra/postgres/Dockerfile");
    }
    String image;
    try {
      image =
          new ImageFromDockerfile("ecoguard-postgres:it", false).withDockerfile(dockerfile).get();
    } catch (Exception ex) {
      throw new IllegalStateException("could not build the PostGIS image", ex);
    }
    PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>(
                DockerImageName.parse(image).asCompatibleSubstituteFor("postgres"))
            .withDatabaseName("ecoguard")
            .withUsername("ecoguard")
            .withPassword("ecoguard");
    postgres.start();
    return postgres;
  }
}
