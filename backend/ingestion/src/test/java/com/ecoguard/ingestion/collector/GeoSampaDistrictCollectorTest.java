package com.ecoguard.ingestion.collector;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.client.RestClient;

public class GeoSampaDistrictCollectorTest {
  @RegisterExtension
  static WireMockExtension wireMock =
      WireMockExtension.newInstance()
          .options(WireMockConfiguration.wireMockConfig().dynamicPort())
          .build();

  @Test
  void loadsNinetySixOfficialFeatures() {
    wireMock.stubFor(
        get(urlPathEqualTo("/ows"))
            .withQueryParam("typeNames", equalTo(GeoSampaDistrictParser.TYPE_NAME))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(collection(96))));
    var extraction =
        new GeoSampaDistrictCollector(RestClient.create(), wireMock.baseUrl() + "/ows")
            .extract(Instant.parse("2026-11-02T00:00:00Z"));
    assertEquals(96, extraction.districts().size());
    assertEquals("1", extraction.districts().get(0).code());
    assertEquals(4326, extraction.districts().get(0).sourceSrid());
  }

  @Test
  void rejectsAPartialLayer() {
    wireMock.stubFor(
        get(urlPathEqualTo("/ows"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(collection(2))));
    assertThrows(
        IllegalStateException.class,
        () ->
            new GeoSampaDistrictCollector(RestClient.create(), wireMock.baseUrl() + "/ows")
                .extract(Instant.parse("2026-11-02T00:00:00Z")));
  }

  public static String collection(int count) {
    StringBuilder features = new StringBuilder();
    for (int i = 1; i <= count; i++) {
      if (i > 1) {
        features.append(',');
      }
      double x = -46.8 + (i * 0.001);
      double y = -23.7;
      features
          .append("{\"type\":\"Feature\",\"properties\":{")
          .append("\"cd_distrito_municipal\":\"")
          .append(i)
          .append("\",\"nm_distrito_municipal\":\"District ")
          .append(i)
          .append("\",\"sg_distrito_municipal\":\"D")
          .append(i)
          .append("\",\"cd_identificador_subprefeitura\":1,\"qt_area_quilometro\":1.2,")
          .append("\"dt_atualizacao\":\"2026-10-27T22:04:13.456Z\"},")
          .append("\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[")
          .append(coord(x, y))
          .append(',')
          .append(coord(x + 0.01, y))
          .append(',')
          .append(coord(x + 0.01, y + 0.01))
          .append(',')
          .append(coord(x, y))
          .append("]]}}");
    }
    return "{\"type\":\"FeatureCollection\",\"numberMatched\":"
        + count
        + ",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"urn:ogc:def:crs:EPSG::4326\"}},\"features\":["
        + features
        + "]}";
  }

  private static String coord(double x, double y) {
    return "[" + x + "," + y + "]";
  }
}
