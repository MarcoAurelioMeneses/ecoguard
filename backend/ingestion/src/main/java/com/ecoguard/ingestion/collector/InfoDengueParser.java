package com.ecoguard.ingestion.collector;

import com.ecoguard.domain.ingestion.Records.InfoDengueAlert;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;

/** Maps one InfoDengue alertcity object. `data_iniSE` is epoch milliseconds. */
public final class InfoDengueParser {
  private InfoDengueParser() {}

  public static InfoDengueAlert parse(
      Map<String, Object> item, int geocode, String disease, Instant extractedAt) {
    int se = requiredInt(item.get("SE"), "SE");
    int epiYear = Math.floorDiv(se, 100);
    int epiWeek = Math.floorMod(se, 100);
    if (epiWeek < 1 || epiWeek > 53) {
      throw new IllegalArgumentException("SE week out of range: " + se);
    }
    Integer alertLevel = optionalInt(item.get("nivel"));
    if (alertLevel != null && (alertLevel < 1 || alertLevel > 4)) {
      throw new IllegalArgumentException("alert level out of range: " + alertLevel);
    }
    return new InfoDengueAlert(
        geocode,
        disease,
        weekStart(item.get("data_iniSE")),
        epiYear,
        epiWeek,
        se,
        optionalInt(item.get("casos")),
        optionalDouble(item.get("casos_est")),
        optionalInt(item.get("casos_est_min")),
        optionalInt(item.get("casos_est_max")),
        optionalDouble(item.get("p_inc100k")),
        alertLevel,
        optionalDouble(item.get("Rt")),
        optionalDouble(item.get("p_rt1")),
        optionalDouble(item.get("pop")),
        optionalString(item.get("municipio_nome")),
        optionalString(item.get("versao_modelo")),
        optionalLong(item.get("id")),
        extractedAt);
  }

  static LocalDate weekStart(Object value) {
    if (value instanceof String text && text.chars().allMatch(Character::isDigit)) {
      value = Long.parseLong(text);
    }
    if (!(value instanceof Number number) || value instanceof Boolean) {
      throw new IllegalArgumentException("data_iniSE must be epoch milliseconds");
    }
    double raw = number.doubleValue();
    double seconds = raw > 10_000_000_000d ? raw / 1000d : raw;
    return Instant.ofEpochMilli(Math.round(seconds * 1000d)).atZone(ZoneOffset.UTC).toLocalDate();
  }

  private static Integer optionalInt(Object value) {
    if (value == null || "".equals(value)) {
      return null;
    }
    if (value instanceof Boolean) {
      throw new IllegalArgumentException("expected an integer");
    }
    if (value instanceof Number number) {
      return number.intValue();
    }
    return Integer.valueOf(value.toString());
  }

  private static Long optionalLong(Object value) {
    Integer parsed = optionalInt(value);
    return parsed == null ? null : parsed.longValue();
  }

  private static Double optionalDouble(Object value) {
    if (value == null || "".equals(value)) {
      return null;
    }
    if (value instanceof Boolean) {
      throw new IllegalArgumentException("expected a number");
    }
    if (value instanceof Number number) {
      return number.doubleValue();
    }
    return Double.valueOf(value.toString());
  }

  private static String optionalString(Object value) {
    if (value == null) {
      return null;
    }
    String text = value.toString().trim();
    return text.isEmpty() ? null : text;
  }

  private static int requiredInt(Object value, String label) {
    Integer parsed = optionalInt(value);
    if (parsed == null) {
      throw new IllegalArgumentException(label + " is required");
    }
    return parsed;
  }
}
