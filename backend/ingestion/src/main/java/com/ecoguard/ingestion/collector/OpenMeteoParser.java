package com.ecoguard.ingestion.collector;

import com.ecoguard.domain.ingestion.Records.OpenMeteoDaily;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class OpenMeteoParser {
  private OpenMeteoParser() {}

  @SuppressWarnings("unchecked")
  public static List<OpenMeteoDaily> parse(
      Map<String, Object> body,
      String locationId,
      double requestedLatitude,
      double requestedLongitude,
      Instant extractedAt) {
    Object dailyValue = body.get("daily");
    if (!(dailyValue instanceof Map<?, ?> dailyRaw)) {
      throw new IllegalArgumentException("Open-Meteo daily must be an object");
    }
    Map<String, Object> daily = (Map<String, Object>) dailyRaw;
    List<String> times = stringList(daily.get("time"), "daily.time");
    List<Double> weather = numberList(daily.get("weather_code"), "weather_code", times.size());
    List<Double> max =
        numberList(daily.get("temperature_2m_max"), "temperature_2m_max", times.size());
    List<Double> min =
        numberList(daily.get("temperature_2m_min"), "temperature_2m_min", times.size());
    List<Double> rain =
        numberList(daily.get("precipitation_sum"), "precipitation_sum", times.size());
    List<Double> hours =
        numberList(daily.get("precipitation_hours"), "precipitation_hours", times.size());
    List<Double> wind =
        numberList(daily.get("wind_speed_10m_max"), "wind_speed_10m_max", times.size());
    Object timezone = body.get("timezone");
    if (!(timezone instanceof String zone) || zone.isBlank()) {
      throw new IllegalArgumentException("Open-Meteo timezone is required");
    }
    int offset = requiredInt(body.get("utc_offset_seconds"), "utc_offset_seconds");
    double gridLat = requiredDouble(body.get("latitude"), "latitude");
    double gridLon = requiredDouble(body.get("longitude"), "longitude");
    Double elevation = optionalDouble(body.get("elevation"));
    List<OpenMeteoDaily> rows = new ArrayList<>();
    for (int i = 0; i < times.size(); i++) {
      rows.add(
          new OpenMeteoDaily(
              locationId,
              LocalDate.parse(times.get(i)),
              requestedLatitude,
              requestedLongitude,
              gridLat,
              gridLon,
              elevation,
              zone,
              offset,
              integerAt(weather, i),
              max.get(i),
              min.get(i),
              rain.get(i),
              hours.get(i),
              wind.get(i),
              extractedAt));
    }
    return List.copyOf(rows);
  }

  private static List<String> stringList(Object value, String label) {
    if (!(value instanceof List<?> list) || list.isEmpty()) {
      throw new IllegalArgumentException(label + " must be a non-empty list");
    }
    List<String> result = new ArrayList<>();
    for (Object item : list) {
      if (!(item instanceof String text)) {
        throw new IllegalArgumentException(label + " entries must be strings");
      }
      result.add(text);
    }
    return result;
  }

  private static List<Double> numberList(Object value, String label, int size) {
    if (!(value instanceof List<?> list)) {
      throw new IllegalArgumentException(label + " must be a list");
    }
    if (list.size() != size) {
      throw new IllegalArgumentException(
          label + " length " + list.size() + " does not match time length " + size);
    }
    List<Double> result = new ArrayList<>();
    for (Object item : list) {
      result.add(optionalDouble(item));
    }
    return result;
  }

  private static Integer integerAt(List<Double> values, int index) {
    Double value = values.get(index);
    if (value == null) {
      return null;
    }
    if (value != Math.rint(value)) {
      throw new IllegalArgumentException("weather_code must be an integer, got " + value);
    }
    return value.intValue();
  }

  private static Double optionalDouble(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Number number) {
      return number.doubleValue();
    }
    throw new IllegalArgumentException("expected a number");
  }

  private static double requiredDouble(Object value, String label) {
    Double parsed = optionalDouble(value);
    if (parsed == null) {
      throw new IllegalArgumentException(label + " is required");
    }
    return parsed;
  }

  private static int requiredInt(Object value, String label) {
    Double parsed = optionalDouble(value);
    if (parsed == null) {
      throw new IllegalArgumentException(label + " is required");
    }
    return parsed.intValue();
  }
}
