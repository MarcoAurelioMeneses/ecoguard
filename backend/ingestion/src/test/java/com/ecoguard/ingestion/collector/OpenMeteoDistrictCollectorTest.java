package com.ecoguard.ingestion.collector;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ecoguard.domain.port.Ports.DistrictSampler;
import com.ecoguard.domain.port.Ports.DistrictSampler.SamplePoint;
import com.ecoguard.ingestion.config.BackfillProperties;
import com.ecoguard.ingestion.config.IngestionProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.client.RestClient;

class OpenMeteoDistrictCollectorTest {
  @RegisterExtension
  static WireMockExtension wireMock =
      WireMockExtension.newInstance()
          .options(WireMockConfiguration.wireMockConfig().dynamicPort())
          .build();

  @Test
  void readsWeatherAndAirWithoutPuttingTheKeyInTheUrl() {
    wireMock.stubFor(
        get(urlPathEqualTo("/forecast"))
            .willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBody(forecast())));
    wireMock.stubFor(
        get(urlPathEqualTo("/air"))
            .willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBody(air())));
    var extraction = collector("secret-key").extract(Instant.parse("2026-11-03T00:00:00Z"));
    assertEquals(96, extraction.districtEnvironment().size());
    assertNotNull(extraction.districtEnvironment().get(0).temperatureMaxC());
    assertNotNull(extraction.districtEnvironment().get(0).pm25Mean());
    wireMock.verify(
        getRequestedFor(urlPathEqualTo("/forecast"))
            .withHeader("Authorization", equalTo("Bearer secret-key"))
            .withoutQueryParam("apikey"));
  }

  @Test
  void airFailureLeavesNoRowsToStore() {
    wireMock.stubFor(
        get(urlPathEqualTo("/forecast"))
            .willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBody(forecast())));
    wireMock.stubFor(get(urlPathEqualTo("/air")).willReturn(aResponse().withStatus(500)));
    assertThrows(
        RuntimeException.class, () -> collector("").extract(Instant.parse("2026-11-03T00:00:00Z")));
  }

  private static OpenMeteoDistrictCollector collector(String apiKey) {
    List<SamplePoint> points = new ArrayList<>();
    for (int i = 0; i < 96; i++) {
      points.add(new SamplePoint(Integer.toString(i + 1), -23.5, -46.6));
    }
    DistrictSampler sampler = () -> points;
    IngestionProperties ingestion =
        new IngestionProperties(
            "",
            new IngestionProperties.InfoDengue(
                "http://unused", 3550308, "dengue", 1, 53, null, null),
            new IngestionProperties.OpenMeteo(
                wireMock.baseUrl() + "/forecast",
                apiKey,
                -23.55,
                -46.63,
                "America/Sao_Paulo",
                1,
                "sp-city-reference"));
    BackfillProperties backfill =
        new BackfillProperties(
            1, wireMock.baseUrl() + "/air", "http://unused", "http://unused", "http://unused");
    return new OpenMeteoDistrictCollector(
        RestClient.create(), new ObjectMapper(), ingestion, backfill, sampler);
  }

  private static String forecast() {
    String day =
        "{\"daily\":{\"time\":[\"2026-11-02\"],\"temperature_2m_max\":[30.0],\"precipitation_sum\":[1.0]}}";
    return "[" + String.join(",", java.util.Collections.nCopies(8, day)) + "]";
  }

  private static String air() {
    String hour =
        "{\"hourly\":{\"time\":[\"2026-11-02T00:00\"],\"pm2_5\":[12.0],\"pm10\":[20.0],\"ozone\":[40.0]}}";
    return "[" + String.join(",", java.util.Collections.nCopies(8, hour)) + "]";
  }
}
