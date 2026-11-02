package com.ecoguard.ingestion.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ecoguard.domain.ingestion.Records.District;
import com.ecoguard.ingestion.collector.GeoSampaDistrictCollectorTest;
import com.ecoguard.ingestion.collector.GeoSampaDistrictParser;
import com.ecoguard.ingestion.support.PostgresFixture;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
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

class DistrictUpsertTest {
  static JdbcIngestionStore store;
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
    store = new JdbcIngestionStore(JdbcClient.create(dataSource));
  }

  @Test
  void upsertIsIdempotentAndStoresWgs84() throws Exception {
    var first =
        GeoSampaDistrictParser.parse(
            new ObjectMapper().readTree(GeoSampaDistrictCollectorTest.collection(2)));
    var renamed =
        first.stream()
            .map(
                district ->
                    new District(
                        district.code(),
                        district.name() + " updated",
                        district.abbreviation(),
                        district.subprefectureId(),
                        district.areaKm2(),
                        district.wkt(),
                        district.sourceSrid(),
                        Instant.parse("2026-10-28T00:00:00Z")))
            .toList();
    assertEquals(2, store.upsertDistricts(first));
    assertEquals(2, store.upsertDistricts(renamed));
    assertEquals(2, jdbc.queryForObject("SELECT count(*) FROM marts.districts", Integer.class));
    assertEquals(
        4326,
        jdbc.queryForObject("SELECT ST_SRID(geom) FROM marts.districts LIMIT 1", Integer.class));
    assertEquals(
        "District 1 updated",
        jdbc.queryForObject(
            "SELECT name FROM marts.districts WHERE district_code = '1'", String.class));
  }
}
