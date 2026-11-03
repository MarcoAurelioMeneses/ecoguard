package com.ecoguard.application;

import com.ecoguard.domain.risk.OverallWeights;
import com.ecoguard.domain.risk.ScoreInputs.DengueReading;
import com.ecoguard.domain.risk.ScoreInputs.DistrictDay;
import com.ecoguard.domain.risk.ScoreInputs.PersistedScore;
import com.ecoguard.domain.risk.ScoreInputs.Readings;
import com.ecoguard.domain.risk.ScoreInputs.Writer;
import com.ecoguard.domain.risk.ScoreRules;
import com.ecoguard.domain.risk.ScoreRules.DayScore;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** One score row per district and forecast date. The caller owns the transaction. */
public final class ComputeRiskScores {
  public int execute(
      Readings readings,
      Writer writer,
      Instant computedAt,
      String configKey,
      int geocode,
      String disease) {
    OverallWeights weights = readings.weights(configKey);
    DengueReading dengue = readings.latestDengue(geocode, disease);
    List<PersistedScore> rows = new ArrayList<>();
    for (DistrictDay observation : readings.districtDays()) {
      BigDecimal heat = ScoreRules.heatForecast(observation.temperatureMaxC());
      BigDecimal rain = ScoreRules.rainForecast(observation.precipitationMm());
      BigDecimal air = ScoreRules.airQuality(observation.pm25DailyMean());
      BigDecimal fire = ScoreRules.fire(observation.fireCount());
      BigDecimal dengueScore = dengueScore(dengue, observation.date());
      BigDecimal density = ScoreRules.densityRisk(observation.population(), observation.areaKm2());
      BigDecimal sanitation =
          ScoreRules.sanitationRisk(
              observation.occupiedHouseholds(), observation.sewageNetworkHouseholds());
      DayScore day =
          ScoreRules.day(weights, heat, rain, air, fire, dengueScore, density, sanitation);
      rows.add(
          new PersistedScore(
              observation.districtCode(),
              observation.date(),
              configKey,
              day,
              computedAt,
              grain(day)));
    }
    if (rows.isEmpty()) {
      return 0;
    }
    int written = writer.upsert(rows);
    writer.appendHistory(rows);
    return written;
  }

  private static BigDecimal dengueScore(DengueReading dengue, java.time.LocalDate date) {
    if (dengue == null || dengue.weekStart().isAfter(date)) {
      return null;
    }
    return ScoreRules.dengue(dengue.level(), dengue.incidencePer100k());
  }

  private static String grain(DayScore day) {
    return "environmental=Open-Meteo forecast and PM2.5 at the district point, INPE hotspots inside the polygon, coverage "
        + day.environmental().coverage()
        + "; health=municipal InfoDengue, coverage "
        + day.health().coverage()
        + "; social=IBGE Censo 2022 district density and sewer network, coverage "
        + day.social().coverage()
        + "; missing="
        + String.join(",", day.environmental().missing())
        + ","
        + String.join(",", day.health().missing())
        + ","
        + String.join(",", day.social().missing());
  }
}
