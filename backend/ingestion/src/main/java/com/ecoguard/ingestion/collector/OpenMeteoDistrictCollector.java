package com.ecoguard.ingestion.collector;

import com.ecoguard.domain.ingestion.Records.DistrictEnvironmentDay;
import com.ecoguard.domain.ingestion.Records.Extraction;
import com.ecoguard.domain.ingestion.Records.RawRecord;
import com.ecoguard.domain.port.Ports.DistrictSampler;
import com.ecoguard.domain.port.Ports.DistrictSampler.SamplePoint;
import com.ecoguard.domain.port.Ports.SourcePort;
import com.ecoguard.ingestion.config.BackfillProperties;
import com.ecoguard.ingestion.config.IngestionProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

/** HTTP and parsing only. Persistence goes through RunIngestion. */
@Component
public class OpenMeteoDistrictCollector implements SourcePort {
  private static final String DAILY = "temperature_2m_max,precipitation_sum";
  private static final String HOURLY = "pm2_5,pm10,ozone";
  private static final int BATCH = 8;

  private final RestClient http;
  private final ObjectMapper mapper;
  private final IngestionProperties ingestion;
  private final BackfillProperties backfill;
  private final DistrictSampler sampler;

  public OpenMeteoDistrictCollector(
      RestClient http,
      ObjectMapper mapper,
      IngestionProperties ingestion,
      BackfillProperties backfill,
      DistrictSampler sampler) {
    this.http = http;
    this.mapper = mapper;
    this.ingestion = ingestion;
    this.backfill = backfill;
    this.sampler = sampler;
  }

  @Override
  public String sourceName() {
    return "open-meteo-districts";
  }

  @Override
  public Extraction extract(Instant extractedAt) {
    List<SamplePoint> points = sampler.points();
    if (points.size() != 96) {
      throw new IllegalStateException(
          "need 96 districts before Open-Meteo sampling, found " + points.size());
    }
    Map<String, DistrictEnvironmentDay> rows = new LinkedHashMap<>();
    for (int i = 0; i < points.size(); i += BATCH) {
      List<SamplePoint> batch = points.subList(i, Math.min(points.size(), i + BATCH));
      mergeWeather(rows, batch, extractedAt);
      mergeAir(rows, batch, extractedAt);
    }
    List<RawRecord> raw = new ArrayList<>();
    for (DistrictEnvironmentDay row : rows.values()) {
      raw.add(
          new RawRecord(
              sourceName(),
              row.externalKey(),
              Map.of("district", row.districtCode(), "date", row.date().toString()),
              extractedAt));
    }
    return new Extraction(
        sourceName(),
        List.copyOf(raw),
        List.of(),
        List.of(),
        List.of(),
        List.copyOf(rows.values()),
        List.of(),
        List.of());
  }

  private void mergeWeather(
      Map<String, DistrictEnvironmentDay> rows, List<SamplePoint> batch, Instant extractedAt) {
    ArrayNode body = asArray(get(ingestion.openMeteo().baseUrl(), batch, "daily", DAILY));
    for (int i = 0; i < batch.size(); i++) {
      JsonNode daily = body.get(i).path("daily");
      JsonNode dates = daily.path("time");
      for (int d = 0; d < dates.size(); d++) {
        SamplePoint point = batch.get(i);
        LocalDate date = LocalDate.parse(dates.get(d).asText());
        String key = point.code() + ":" + date;
        DistrictEnvironmentDay current = rows.get(key);
        rows.put(
            key,
            new DistrictEnvironmentDay(
                point.code(),
                date,
                number(daily.path("temperature_2m_max"), d),
                number(daily.path("precipitation_sum"), d),
                current == null ? null : current.pm25Mean(),
                current == null ? null : current.pm10Mean(),
                current == null ? null : current.ozoneMean(),
                point.latitude(),
                point.longitude(),
                extractedAt));
      }
    }
  }

  private void mergeAir(
      Map<String, DistrictEnvironmentDay> rows, List<SamplePoint> batch, Instant extractedAt) {
    ArrayNode body = asArray(get(backfill.airQualityBaseUrl(), batch, "hourly", HOURLY));
    for (int i = 0; i < batch.size(); i++) {
      JsonNode hourly = body.get(i).path("hourly");
      Map<LocalDate, double[]> sums = new LinkedHashMap<>();
      JsonNode times = hourly.path("time");
      for (int hour = 0; hour < times.size(); hour++) {
        LocalDate date = LocalDateTime.parse(times.get(hour).asText()).toLocalDate();
        double[] acc = sums.computeIfAbsent(date, key -> new double[6]);
        add(acc, 0, hourly.path("pm2_5"), hour);
        add(acc, 1, hourly.path("pm10"), hour);
        add(acc, 2, hourly.path("ozone"), hour);
      }
      SamplePoint point = batch.get(i);
      for (var entry : sums.entrySet()) {
        String key = point.code() + ":" + entry.getKey();
        DistrictEnvironmentDay current = rows.get(key);
        double[] acc = entry.getValue();
        rows.put(
            key,
            new DistrictEnvironmentDay(
                point.code(),
                entry.getKey(),
                current == null ? null : current.temperatureMaxC(),
                current == null ? null : current.precipitationMm(),
                mean(acc, 0),
                mean(acc, 1),
                mean(acc, 2),
                point.latitude(),
                point.longitude(),
                extractedAt));
      }
    }
  }

  private JsonNode get(String base, List<SamplePoint> batch, String key, String value) {
    String lats =
        batch.stream()
            .map(point -> Double.toString(point.latitude()))
            .reduce((a, b) -> a + "," + b)
            .orElse("");
    String lons =
        batch.stream()
            .map(point -> Double.toString(point.longitude()))
            .reduce((a, b) -> a + "," + b)
            .orElse("");
    UriComponentsBuilder builder =
        UriComponentsBuilder.fromUriString(base)
            .queryParam("latitude", lats)
            .queryParam("longitude", lons)
            .queryParam(key, value)
            .queryParam("timezone", ingestion.openMeteo().timezone())
            .queryParam("past_days", backfill.days())
            .queryParam("forecast_days", 1);
    java.util.Map<String, String> headers = new java.util.HashMap<>();
    if (ingestion.openMeteo().apiKey() != null && !ingestion.openMeteo().apiKey().isBlank()) {
      headers.put("Authorization", "Bearer " + ingestion.openMeteo().apiKey());
    }
    return HttpFetch.get(http, builder.build(true).toUri(), JsonNode.class, headers);
  }

  private ArrayNode asArray(JsonNode body) {
    if (body.isArray()) {
      return (ArrayNode) body;
    }
    ArrayNode array = mapper.createArrayNode();
    array.add(body);
    return array;
  }

  private static Double number(JsonNode values, int index) {
    JsonNode node = values.get(index);
    return node == null || node.isNull() ? null : node.asDouble();
  }

  private static void add(double[] acc, int metric, JsonNode values, int index) {
    Double value = number(values, index);
    if (value == null) {
      return;
    }
    acc[metric] += value;
    acc[metric + 3] += 1;
  }

  private static Double mean(double[] acc, int metric) {
    return acc[metric + 3] == 0 ? null : acc[metric] / acc[metric + 3];
  }
}
