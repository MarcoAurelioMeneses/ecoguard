package com.ecoguard.ingestion.collector;

import com.ecoguard.domain.ingestion.Records.District;
import com.ecoguard.domain.ingestion.Records.Extraction;
import com.ecoguard.domain.port.Ports.SourcePort;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class GeoSampaDistrictCollector implements SourcePort {
  static final String SOURCE = "geosampa-districts";
  private static final int PAGE_SIZE = 100;

  private final RestClient http;
  private final String baseUrl;

  public GeoSampaDistrictCollector(
      RestClient http,
      @Value(
              "${ecoguard.geosampa.base-url:https://wfs.geosampa.prefeitura.sp.gov.br/geoserver/geoportal/ows}")
          String baseUrl) {
    this.http = http;
    this.baseUrl = baseUrl;
  }

  @Override
  public String sourceName() {
    return SOURCE;
  }

  @Override
  public Extraction extract(Instant extractedAt) {
    List<District> districts = new ArrayList<>();
    int start = 0;
    int matched = Integer.MAX_VALUE;
    while (start < matched && start < PAGE_SIZE * 5) {
      JsonNode page = fetch(start);
      matched = page.path("numberMatched").asInt(page.path("totalFeatures").asInt(matched));
      List<District> batch = GeoSampaDistrictParser.parse(page);
      if (batch.isEmpty()) {
        break;
      }
      districts.addAll(batch);
      start += batch.size();
      if (batch.size() < PAGE_SIZE) {
        break;
      }
    }
    if (districts.size() != 96) {
      throw new IllegalStateException(
          "GeoSampa distrito_municipal returned " + districts.size() + " features, expected 96");
    }
    return new Extraction(sourceName(), List.of(), List.of(), List.of(), List.copyOf(districts));
  }

  private JsonNode fetch(int startIndex) {
    String uri =
        UriComponentsBuilder.fromUriString(baseUrl)
            .queryParam("service", "WFS")
            .queryParam("version", "2.0.0")
            .queryParam("request", "GetFeature")
            .queryParam("typeNames", GeoSampaDistrictParser.TYPE_NAME)
            .queryParam("outputFormat", "application/json")
            .queryParam("srsName", "EPSG:4326")
            .queryParam("count", PAGE_SIZE)
            .queryParam("startIndex", startIndex)
            .build(true)
            .toUriString();
    JsonNode body =
        http.get()
            .uri(uri)
            .header("Accept", "application/json")
            .header("User-Agent", "EcoGuard-ingestion/0.1")
            .retrieve()
            .body(JsonNode.class);
    if (body == null || !body.path("features").isArray()) {
      throw new IllegalStateException("GeoSampa WFS did not return a FeatureCollection");
    }
    return body;
  }
}
