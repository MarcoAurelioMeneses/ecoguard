package com.ecoguard.ingestion.collector;

import com.ecoguard.domain.ingestion.Records.Extraction;
import com.ecoguard.domain.ingestion.Records.OpenMeteoDaily;
import com.ecoguard.domain.ingestion.Records.RawRecord;
import com.ecoguard.domain.port.Ports.SourcePort;
import com.ecoguard.ingestion.config.IngestionProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OpenMeteoCollector implements SourcePort {
  private static final String USER_AGENT = "EcoGuard-ingestion/0.1";
  private static final String DAILY =
      "weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum,precipitation_hours,wind_speed_10m_max";

  private final RestClient http;
  private final IngestionProperties properties;
  private final ObjectMapper mapper;

  public OpenMeteoCollector(RestClient http, IngestionProperties properties, ObjectMapper mapper) {
    this.http = http;
    this.properties = properties;
    this.mapper = mapper;
  }

  @Override
  public String sourceName() {
    return "open-meteo";
  }

  @Override
  public Extraction extract(Instant extractedAt) {
    IngestionProperties.OpenMeteo source = properties.openMeteo();
    JsonNode body = fetch(source);
    if (body != null && body.path("error").asBoolean(false)) {
      throw new IllegalStateException("Open-Meteo error: " + body.path("reason").asText());
    }
    Map<String, Object> document = mapper.convertValue(body, new TypeReference<>() {});
    List<OpenMeteoDaily> rows =
        OpenMeteoParser.parse(
            document, source.locationId(), source.latitude(), source.longitude(), extractedAt);
    List<RawRecord> records = new ArrayList<>();
    for (OpenMeteoDaily row : rows) {
      records.add(new RawRecord(sourceName(), row.externalKey(), payload(row), extractedAt));
    }
    return new Extraction(sourceName(), List.copyOf(records), List.of(), rows);
  }

  private JsonNode fetch(IngestionProperties.OpenMeteo source) {
    UriComponentsBuilder builder =
        UriComponentsBuilder.fromUriString(source.baseUrl())
            .queryParam("latitude", source.latitude())
            .queryParam("longitude", source.longitude())
            .queryParam("daily", DAILY)
            .queryParam("timezone", source.timezone())
            .queryParam("forecast_days", source.forecastDays());
    if (source.apiKey() != null && !source.apiKey().isBlank()) {
      builder.queryParam("apikey", source.apiKey());
    }
    return http.get()
        .uri(builder.build(true).toUri())
        .header("Accept", "application/json")
        .header("User-Agent", USER_AGENT)
        .retrieve()
        .body(JsonNode.class);
  }

  private static Map<String, Object> payload(OpenMeteoDaily row) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("location_id", row.locationId());
    payload.put("forecast_date", row.forecastDate().toString());
    payload.put("requested_latitude", row.requestedLatitude());
    payload.put("requested_longitude", row.requestedLongitude());
    payload.put("grid_latitude", row.gridLatitude());
    payload.put("grid_longitude", row.gridLongitude());
    payload.put("temperature_max_c", row.temperatureMaxC());
    payload.put("precipitation_sum_mm", row.precipitationSumMm());
    return payload;
  }
}
