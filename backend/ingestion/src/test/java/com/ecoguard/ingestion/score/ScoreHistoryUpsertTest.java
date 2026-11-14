package com.ecoguard.ingestion.score;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ecoguard.domain.risk.ScoreInputs.PersistedScore;
import com.ecoguard.domain.risk.ScoreRules;
import com.ecoguard.domain.risk.ScoreRules.DayScore;
import com.ecoguard.domain.risk.ScoreRules.Pillar;
import com.ecoguard.ingestion.support.PostgresFixture;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
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

class ScoreHistoryUpsertTest {
  static JdbcScoreStore store;
  static JdbcTemplate jdbc;

  @BeforeAll
  static void migrate() {
    Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable());
    PostgreSQLContainer<?> postgres = PostgresFixture.start();
    DataSource dataSource =
        new DriverManagerDataSource(
            postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
    Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate();
    jdbc = new JdbcTemplate(dataSource);
    store = new JdbcScoreStore(JdbcClient.create(dataSource));
  }

  @Test
  void recomputingADistrictDayUpdatesHistoryInsteadOfInserting() {
    store.appendHistory(List.of(score(new BigDecimal("10.00"))));
    store.appendHistory(List.of(score(new BigDecimal("20.00"))));
    assertEquals(
        1,
        jdbc.queryForObject(
            """
            SELECT count(*) FROM marts.district_risk_score_history
            WHERE district_code = '1' AND score_date = DATE '2026-11-02'
            """,
            Integer.class));
    assertEquals(
        0,
        new BigDecimal("20.00")
            .compareTo(
                jdbc.queryForObject(
                    """
                    SELECT overall_score FROM marts.district_risk_score_history
                    WHERE district_code = '1' AND score_date = DATE '2026-11-02'
                    """,
                    BigDecimal.class)));
  }

  private static PersistedScore score(BigDecimal overall) {
    Pillar environmental =
        new Pillar(new BigDecimal("10.00"), new BigDecimal("1.000"), true, List.of());
    Pillar health =
        new Pillar(
            new BigDecimal("70.00"),
            new BigDecimal("0.600"),
            false,
            List.of("respiratory_datasus"));
    Pillar social =
        new Pillar(new BigDecimal("20.00"), new BigDecimal("0.500"), false, List.of("income"));
    DayScore day =
        new DayScore(
            environmental,
            health,
            social,
            overall,
            new BigDecimal("0.00"),
            new BigDecimal("0.00"),
            new BigDecimal("70.00"),
            ScoreRules.riskClass(overall));
    return new PersistedScore(
        "1",
        LocalDate.parse("2026-11-02"),
        "default",
        day,
        Instant.parse("2026-11-03T00:00:00Z"),
        "test");
  }
}
