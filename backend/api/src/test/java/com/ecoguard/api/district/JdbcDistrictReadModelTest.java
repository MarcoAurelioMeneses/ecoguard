package com.ecoguard.api.district;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecoguard.application.DistrictReadModel.CityScores;
import com.ecoguard.application.DistrictReadModel.DistrictView;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers(disabledWithoutDocker = true)
class JdbcDistrictReadModelTest {
  static JdbcDistrictReadModel model;
  static JdbcTemplate jdbc;

  @BeforeAll
  static void migrate() {
    Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable());
    PostgreSQLContainer<?> postgres = start();
    DataSource dataSource =
        new DriverManagerDataSource(
            postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
    Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate();
    jdbc = new JdbcTemplate(dataSource);
    model = new JdbcDistrictReadModel(JdbcClient.create(dataSource));
    jdbc.update(
        """
        INSERT INTO marts.districts (district_code, name, geom, source)
        VALUES
          ('01', 'Alpha', ST_Multi(ST_GeomFromText('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))', 4326)),
           'geosampa:wfs:geoportal:distrito_municipal'),
          ('02', 'Zulu', ST_Multi(ST_GeomFromText('POLYGON((2 0, 2 1, 3 1, 3 0, 2 0))', 4326)),
           'geosampa:wfs:geoportal:distrito_municipal')
        """);
  }

  @Test
  void latestScoreStopsAtTodayAndTheCityIsTheMean() {
    LocalDate today = LocalDate.now();
    insert("01", today, 10, 20, 40, 20);
    insert("02", today, 50, 60, 80, 60);
    insert("01", today.plusDays(1), 90, 90, 90, 90);

    DistrictView alpha = model.find("01").orElseThrow();
    assertEquals(today, alpha.score().scoreDate());
    assertEquals(0, new java.math.BigDecimal("10.00").compareTo(alpha.score().environmental()));

    CityScores city = model.cityScores();
    assertNotNull(city.latest());
    assertEquals(today, city.latest().scoreDate());
    assertEquals(0, new java.math.BigDecimal("30.00").compareTo(city.latest().environmental()));
    assertEquals("moderado", city.latest().overallClass());
    assertEquals("city mean of district scores", city.latest().grainNote());
    assertTrue(city.series().stream().allMatch(point -> !point.scoreDate().isAfter(today)));
    assertEquals(1, city.series().size());

    Integer coverageColumns =
        jdbc.queryForObject(
            """
            SELECT count(*) FROM information_schema.columns
            WHERE table_schema = 'agent'
              AND table_name = 'district_risk_scores'
              AND column_name IN (
                'environmental_coverage', 'health_coverage', 'social_coverage')
            """,
            Integer.class);
    assertEquals(3, coverageColumns);
    assertFalse(
        jdbc.queryForList(
                "SELECT environmental_coverage FROM agent.district_risk_scores WHERE district_code = '01'")
            .isEmpty());
  }

  private static void insert(
      String code, LocalDate date, int environmental, int health, int social, int overall) {
    jdbc.update(
        """
        INSERT INTO marts.district_risk_scores (
          district_code, score_date, config_key,
          environmental_score, health_score, social_score, overall_score,
          environmental_coverage, health_coverage, social_coverage,
          environmental_complete, health_complete, social_complete,
          grain_note, computed_at)
        VALUES (?, ?, 'default', ?, ?, ?, ?, 1, 1, 1, true, true, true, 'test', now())
        """,
        code,
        date,
        environmental,
        health,
        social,
        overall);
  }

  @SuppressWarnings("resource")
  private static PostgreSQLContainer<?> start() {
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
