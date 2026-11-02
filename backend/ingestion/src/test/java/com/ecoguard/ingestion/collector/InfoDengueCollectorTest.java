package com.ecoguard.ingestion.collector;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ecoguard.domain.ingestion.Records.Extraction;
import com.ecoguard.ingestion.config.IngestionProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.client.RestClient;

class InfoDengueCollectorTest {
  @RegisterExtension
  static WireMockExtension wireMock =
      WireMockExtension.newInstance()
          .options(WireMockConfiguration.wireMockConfig().dynamicPort())
          .build();

  @Test
  void requestsOneYearAndParsesTheWeek() {
    wireMock.stubFor(
        get(urlPathEqualTo("/alertcity"))
            .withQueryParam("geocode", equalTo("3550308"))
            .withQueryParam("disease", equalTo("dengue"))
            .withQueryParam("ey_start", equalTo("2025"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(BODY)));
    Extraction extraction = collector().extract(Instant.parse("2026-11-02T00:00:00Z"));
    assertEquals(1, extraction.infoDengue().size());
    assertEquals(3, extraction.infoDengue().get(0).casesNotified());
    wireMock.verify(getRequestedFor(urlPathEqualTo("/alertcity")));
  }

  @Test
  void rejectsAnErrorObject() {
    wireMock.stubFor(
        get(urlPathEqualTo("/alertcity"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"error_message\":\"epidate sent is empty.\"}")));
    assertThrows(
        IllegalStateException.class,
        () -> collector().extract(Instant.parse("2026-11-02T00:00:00Z")));
  }

  private InfoDengueCollector collector() {
    IngestionProperties properties =
        new IngestionProperties(
            "",
            new IngestionProperties.InfoDengue(
                wireMock.baseUrl() + "/alertcity", 3550308, "dengue", 42, 42, 2025, 2025),
            new IngestionProperties.OpenMeteo(
                "http://unused", "", -23.55, -46.63, "America/Sao_Paulo", 7, "sp-city-reference"));
    return new InfoDengueCollector(RestClient.create(), properties, new ObjectMapper());
  }

  private static final String BODY =
      """
      [{"data_iniSE":1760227200000,"SE":202542,"casos":3,"casos_est":4.0,"nivel":1,"pop":"1000","municipio_nome":"São Paulo"}]
      """;
}
