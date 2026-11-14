package com.ecoguard.domain.risk;

import com.ecoguard.domain.risk.CovisaDengue.Edition;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class ScoreInputs {
  private ScoreInputs() {}

  public record ForecastDay(
      LocalDate date, BigDecimal temperatureMaxC, BigDecimal precipitationMm) {}

  public record DistrictDay(
      String districtCode,
      LocalDate date,
      BigDecimal temperatureMaxC,
      BigDecimal precipitationMm,
      BigDecimal pm25DailyMean,
      Integer fireCount,
      BigDecimal population,
      BigDecimal areaKm2,
      BigDecimal occupiedHouseholds,
      BigDecimal sewageNetworkHouseholds) {}

  public record DengueReading(LocalDate weekStart, Integer level, BigDecimal incidencePer100k) {}

  public record DistrictCovisa(String districtCode, Edition edition) {}

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

    List<DistrictDay> districtDays();

    /** Every stored epidemiological week, oldest first. A week covers seven days from its start. */
    List<DengueReading> dengueReadings(int geocode, String disease);

    List<DistrictCovisa> covisaEditions();

    OverallWeights weights(String configKey);
  }

  public interface Writer {
    int upsert(List<PersistedScore> scores);

    int appendHistory(List<PersistedScore> scores);
  }
}
