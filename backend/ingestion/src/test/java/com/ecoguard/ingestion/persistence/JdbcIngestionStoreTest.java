package com.ecoguard.ingestion.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ecoguard.domain.ingestion.Records.InfoDengueAlert;
import com.ecoguard.domain.ingestion.Records.RawRecord;
import com.ecoguard.ingestion.support.PostgresFixture;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
class JdbcIngestionStoreTest {
  static PostgreSQLContainer<?> postgres;
  static JdbcIngestionStore store;
  static JdbcTemplate jdbc;

  @BeforeAll
  static void migrate() {
    Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable());
    postgres = PostgresFixture.start();
    DataSource dataSource =
        new DriverManagerDataSource(
            postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
    Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate();
    jdbc = new JdbcTemplate(dataSource);
    store = new JdbcIngestionStore(JdbcClient.create(dataSource));
  }

  @Test
  void upsertsTheSameWeekInPlace() {
    InfoDengueAlert first = alert(3);
    InfoDengueAlert second = alert(9);
    store.upsertRaw(List.of(raw(first)));
    store.upsertInfoDengue(List.of(first));
    store.upsertRaw(List.of(raw(second)));
    store.upsertInfoDengue(List.of(second));
    Integer count =
        jdbc.queryForObject(
            "SELECT count(*) FROM staging.infodengue_alerts WHERE geocode = 3550308",
            Integer.class);
    Integer cases =
        jdbc.queryForObject(
            "SELECT cases_notified FROM staging.infodengue_alerts WHERE geocode = 3550308",
            Integer.class);
    assertEquals(1, count);
    assertEquals(9, cases);
  }

  private static InfoDengueAlert alert(int cases) {
    return new InfoDengueAlert(
        3550308,
        "dengue",
        LocalDate.parse("2025-10-12"),
        2025,
        42,
        202542,
        cases,
        (double) cases,
        cases,
        cases,
        1.0,
        2,
        1.0,
        0.5,
        1000.0,
        "São Paulo",
        "test",
        99L,
        Instant.parse("2026-11-02T00:00:00Z"));
  }

  private static RawRecord raw(InfoDengueAlert row) {
    return new RawRecord(
        "infodengue",
        row.externalKey(),
        Map.of("se", row.se(), "cases", row.casesNotified()),
        row.extractedAt());
  }
}
