package com.ecoguard.domain.risk;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class ScoreInputs {
  private ScoreInputs() {}

  public record ForecastDay(
      LocalDate date, BigDecimal temperatureMaxC, BigDecimal precipitationMm) {}

  public record DengueReading(LocalDate weekStart, Integer level, BigDecimal incidencePer100k) {}

  public record PersistedScore(
      String districtCode,
      LocalDate scoreDate,
      String configKey,
      ScoreRules.DayScore score,
      Instant computedAt,
      String grainNote) {}

  public interface Readings {
    List<String> districtCodes();

    List<ForecastDay> forecasts(String locationId);

    DengueReading latestDengue(int geocode, String disease);

    OverallWeights weights(String configKey);
  }

  public interface Writer {
    int upsert(List<PersistedScore> scores);

    int appendHistory(List<PersistedScore> scores);
  }
}
