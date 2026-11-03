package com.ecoguard.ingestion.collector;

import com.ecoguard.ingestion.config.BackfillProperties;
import com.ecoguard.ingestion.config.IngestionProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

/** Samples Open-Meteo forecast and air quality at each district point-on-surface. */
@Service
public class OpenMeteoDistrictCollector {
  private static final String DAILY = "temperature_2m_max,precipitation_sum";
  private static final String HOURLY = "pm2_5,pm10,ozone";
  private static final int BATCH = 8;

  private final RestClient http;
  private final JdbcClient jdbc;
  private final ObjectMapper mapper;
  private final IngestionProperties ingestion;
  private final BackfillProperties backfill;

  public OpenMeteoDistrictCollector(
      RestClient http,
      JdbcClient jdbc,
      ObjectMapper mapper,
      IngestionProperties ingestion,
      BackfillProperties backfill) {
    this.http = http;
    this.jdbc = jdbc;
    this.mapper = mapper;
    this.ingestion = ingestion;
    this.backfill = backfill;
  }

  public int extract(Instant extractedAt) {
    List<Point> points =
        jdbc.sql(
                """
                SELECT district_code,
                       ST_Y(ST_PointOnSurface(geom)) AS lat,
                       ST_X(ST_PointOnSurface(geom)) AS lon
                FROM marts.districts
                ORDER BY district_code
                """)
            .query(
                (rs, row) ->
                    new Point(
                        rs.getString("district_code"), rs.getDouble("lat"), rs.getDouble("lon")))
            .list();
    if (points.size() != 96) {
      throw new IllegalStateException(
          "need 96 districts before Open-Meteo sampling, found " + points.size());
    }
    int written = 0;
    for (int i = 0; i < points.size(); i += BATCH) {
      List<Point> batch = points.subList(i, Math.min(points.size(), i + BATCH));
      written += writeWeather(batch, extractedAt);
      written += writeAir(batch, extractedAt);
    }
    return written;
  }

  private int writeWeather(List<Point> batch, Instant extractedAt) {
    JsonNode body = get(ingestion.openMeteo().baseUrl(), batch, "daily", DAILY);
    ArrayNode rows = asArray(body);
    int written = 0;
    for (int i = 0; i < batch.size(); i++) {
      JsonNode daily = rows.get(i).path("daily");
      JsonNode dates = daily.path("time");
      JsonNode temps = daily.path("temperature_2m_max");
      JsonNode rain = daily.path("precipitation_sum");
      for (int d = 0; d < dates.size(); d++) {
        written +=
            upsert(
                batch.get(i),
                LocalDate.parse(dates.get(d).asText()),
                number(temps, d),
                number(rain, d),
                null,
                null,
                null,
                extractedAt);
      }
    }
    return written;
  }

  private int writeAir(List<Point> batch, Instant extractedAt) {
    JsonNode body = get(backfill.airQualityBaseUrl(), batch, "hourly", HOURLY);
    ArrayNode rows = asArray(body);
    int written = 0;
    for (int i = 0; i < batch.size(); i++) {
      JsonNode hourly = rows.get(i).path("hourly");
      written += upsertDailyMeans(batch.get(i), hourly, extractedAt);
    }
    return written;
  }

  private int upsertDailyMeans(Point point, JsonNode hourly, Instant extractedAt) {
    JsonNode times = hourly.path("time");
    JsonNode pm25 = hourly.path("pm2_5");
    JsonNode pm10 = hourly.path("pm10");
    JsonNode ozone = hourly.path("ozone");
    java.util.Map<LocalDate, double[]> sums = new java.util.LinkedHashMap<>();
    for (int i = 0; i < times.size(); i++) {
      LocalDate date = LocalDateTime.parse(times.get(i).asText()).toLocalDate();
      double[] acc = sums.computeIfAbsent(date, key -> new double[6]);
      add(acc, 0, pm25, i);
      add(acc, 1, pm10, i);
      add(acc, 2, ozone, i);
    }
    int written = 0;
    for (var entry : sums.entrySet()) {
      double[] acc = entry.getValue();
      written +=
          upsert(
              point,
              entry.getKey(),
              null,
              null,
              mean(acc, 0),
              mean(acc, 1),
              mean(acc, 2),
              extractedAt);
    }
    return written;
  }

  private JsonNode get(String base, List<Point> batch, String key, String value) {
    String lats =
        batch.stream()
            .map(point -> Double.toString(point.lat()))
            .reduce((a, b) -> a + "," + b)
            .orElse("");
    String lons =
        batch.stream()
            .map(point -> Double.toString(point.lon()))
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
    if (ingestion.openMeteo().apiKey() != null && !ingestion.openMeteo().apiKey().isBlank()) {
      builder.queryParam("apikey", ingestion.openMeteo().apiKey());
    }
    JsonNode body =
        http.get()
            .uri(builder.build(true).toUri())
            .header("Accept", "application/json")
            .header("User-Agent", "EcoGuard-ingestion/0.1")
            .retrieve()
            .body(JsonNode.class);
    if (body != null && body.path("error").asBoolean(false)) {
      throw new IllegalStateException("Open-Meteo error: " + body.path("reason").asText());
    }
    return body;
  }

  private int upsert(
      Point point,
      LocalDate date,
      Double temperature,
      Double rain,
      Double pm25,
      Double pm10,
      Double ozone,
      Instant extractedAt) {
    return jdbc.sql(
            """
            INSERT INTO staging.district_environment_daily (
              district_code, observation_date, temperature_max_c, precipitation_mm,
              pm2_5_mean, pm10_mean, ozone_mean, requested_latitude, requested_longitude,
              source, extracted_at)
            VALUES (
              :code, :date, :temp, :rain, :pm25, :pm10, :ozone, :lat, :lon,
              'open-meteo:forecast+air-quality', :extracted)
            ON CONFLICT (district_code, observation_date) DO UPDATE SET
              temperature_max_c = COALESCE(EXCLUDED.temperature_max_c, staging.district_environment_daily.temperature_max_c),
              precipitation_mm = COALESCE(EXCLUDED.precipitation_mm, staging.district_environment_daily.precipitation_mm),
              pm2_5_mean = COALESCE(EXCLUDED.pm2_5_mean, staging.district_environment_daily.pm2_5_mean),
              pm10_mean = COALESCE(EXCLUDED.pm10_mean, staging.district_environment_daily.pm10_mean),
              ozone_mean = COALESCE(EXCLUDED.ozone_mean, staging.district_environment_daily.ozone_mean),
              extracted_at = EXCLUDED.extracted_at
            """)
        .param("code", point.code())
        .param("date", java.sql.Date.valueOf(date))
        .param("temp", temperature)
        .param("rain", rain)
        .param("pm25", pm25)
        .param("pm10", pm10)
        .param("ozone", ozone)
        .param("lat", point.lat())
        .param("lon", point.lon())
        .param("extracted", java.sql.Timestamp.from(extractedAt))
        .update();
  }

  private ArrayNode asArray(JsonNode body) {
    if (body == null) {
      throw new IllegalStateException("Open-Meteo returned an empty body");
    }
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

  private record Point(String code, double lat, double lon) {}
}
