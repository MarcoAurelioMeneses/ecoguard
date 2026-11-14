package com.ecoguard.ingestion.collector;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ecoguard.ingestion.config.BackfillProperties;
import com.ecoguard.ingestion.config.IngestionProperties;
import com.ecoguard.ingestion.support.PostgresFixture;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.time.Instant;
import java.util.List;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.client.RestClient;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

class OpenMeteoDistrictCollectorTest {
  @RegisterExtension
  static WireMockExtension wireMock =
      WireMockExtension.newInstance()
          .options(WireMockConfiguration.wireMockConfig().dynamicPort())
          .build();

  static JdbcTemplate jdbc;
  static OpenMeteoDistrictCollector collector;

  @BeforeAll
  static void migrate() {
    Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable());
    PostgreSQLContainer<?> postgres = PostgresFixture.start();
    DataSource dataSource =
        new DriverManagerDataSource(
            postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
    Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate();
    jdbc = new JdbcTemplate(dataSource);
    jdbc.update(
        """
        INSERT INTO marts.districts (district_code, name, geom, source)
        VALUES (
          '1', 'One',
          ST_Multi(ST_GeomFromText('POLYGON((-46.7 -23.6,-46.6 -23.6,-46.6 -23.5,-46.7 -23.5,-46.7 -23.6))', 4326)),
          'geosampa:wfs:geoportal:distrito_municipal'),
        (
          '2', 'Two',
          ST_Multi(ST_GeomFromText('POLYGON((-46.5 -23.6,-46.4 -23.6,-46.4 -23.5,-46.5 -23.5,-46.5 -23.6))', 4326)),
          'geosampa:wfs:geoportal:distrito_municipal')
        """);
    IngestionProperties ingestion =
        new IngestionProperties(
            "",
            new IngestionProperties.InfoDengue(
                "http://unused", 3550308, "dengue", 1, 53, null, null),
            new IngestionProperties.OpenMeteo(
                wireMock.baseUrl() + "/forecast",
                "",
                -23.55,
                -46.63,
                "America/Sao_Paulo",
                1,
                "sp-city-reference"));
    BackfillProperties backfill =
        new BackfillProperties(
            1, wireMock.baseUrl() + "/air", "http://unused", "http://unused", "http://unused");
    collector =
        new OpenMeteoDistrictCollector(
            RestClient.create(),
            JdbcClient.create(dataSource),
            new ObjectMapper(),
            ingestion,
            backfill,
            new DataSourceTransactionManager(dataSource));
  }

  @Test
  void airQualityFailureRollsBackTheWeatherWrites() {
    wireMock.stubFor(
        get(urlPathEqualTo("/forecast"))
            .willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBody(FORECAST)));
    wireMock.stubFor(
        get(urlPathEqualTo("/air")).willReturn(aResponse().withStatus(500).withBody("no")));
    assertThrows(
        RuntimeException.class,
        () ->
            collector.ingest(
                List.of(
                    new OpenMeteoDistrictCollector.Point("1", -23.55, -46.63),
                    new OpenMeteoDistrictCollector.Point("2", -23.56, -46.64)),
                Instant.parse("2026-11-03T00:00:00Z")));
    assertEquals(
        0,
        jdbc.queryForObject(
            "SELECT count(*) FROM staging.district_environment_daily", Integer.class));
  }

  private static final String FORECAST =
      """
      [
        {"daily":{"time":["2026-11-02"],"temperature_2m_max":[30.0],"precipitation_sum":[1.0]}},
        {"daily":{"time":["2026-11-02"],"temperature_2m_max":[31.0],"precipitation_sum":[0.0]}}
      ]
      """;
}
