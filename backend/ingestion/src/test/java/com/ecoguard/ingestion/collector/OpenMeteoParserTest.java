package com.ecoguard.ingestion.collector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OpenMeteoParserTest {
  @Test
  void keepsRequestedPointAndGridPoint() {
    var rows =
        OpenMeteoParser.parse(
            body(List.of(0.0, 12.5)),
            "sp-city-reference",
            -23.5505,
            -46.6333,
            Instant.parse("2026-11-02T00:00:00Z"));
    assertEquals(2, rows.size());
    assertEquals(-23.5505, rows.get(0).requestedLatitude());
    assertEquals(-23.51, rows.get(0).gridLatitude());
    assertEquals(28.0, rows.get(0).temperatureMaxC());
    assertEquals(4326, rows.get(0).gridPoint().toPoint().getSRID());
  }

  @Test
  void rejectsMismatchedColumns() {
    Map<String, Object> broken = new java.util.LinkedHashMap<>(body(List.of(0.0)));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            OpenMeteoParser.parse(
                broken,
                "sp-city-reference",
                -23.55,
                -46.63,
                Instant.parse("2026-11-02T00:00:00Z")));
  }

  private static Map<String, Object> body(List<Double> rain) {
    return Map.of(
        "latitude",
        -23.51,
        "longitude",
        -46.61,
        "elevation",
        760.0,
        "utc_offset_seconds",
        -10800,
        "timezone",
        "America/Sao_Paulo",
        "daily",
        Map.of(
            "time",
            List.of("2026-11-02", "2026-11-03"),
            "weather_code",
            List.of(1, 61),
            "temperature_2m_max",
            List.of(28.0, 22.5),
            "temperature_2m_min",
            List.of(18.0, 17.0),
            "precipitation_sum",
            rain,
            "precipitation_hours",
            List.of(0.0, 4.0),
            "wind_speed_10m_max",
            List.of(10.0, 18.0)));
  }
}
