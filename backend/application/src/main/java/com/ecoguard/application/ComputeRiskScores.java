package com.ecoguard.application;

import com.ecoguard.domain.risk.OverallWeights;
import com.ecoguard.domain.risk.ScoreInputs.DengueReading;
import com.ecoguard.domain.risk.ScoreInputs.ForecastDay;
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
      String locationId,
      int geocode,
      String disease) {
    OverallWeights weights = readings.weights(configKey);
    List<String> districts = readings.districtCodes();
    List<ForecastDay> forecasts = readings.forecasts(locationId);
    DengueReading dengue = readings.latestDengue(geocode, disease);
    List<PersistedScore> rows = new ArrayList<>();
    for (ForecastDay forecast : forecasts) {
      BigDecimal heat = ScoreRules.heatForecast(forecast.temperatureMaxC());
      BigDecimal rain = ScoreRules.rainForecast(forecast.precipitationMm());
      BigDecimal dengueScore = dengueScore(dengue, forecast);
      DayScore day = ScoreRules.day(weights, heat, rain, dengueScore);
      String grain = grain(day);
      for (String code : districts) {
        rows.add(new PersistedScore(code, forecast.date(), configKey, day, computedAt, grain));
      }
    }
    if (rows.isEmpty()) {
      return 0;
    }
    int written = writer.upsert(rows);
    writer.appendHistory(rows);
    return written;
  }

  private static BigDecimal dengueScore(DengueReading dengue, ForecastDay forecast) {
    if (dengue == null || dengue.weekStart().isAfter(forecast.date())) {
      return null;
    }
    return ScoreRules.dengue(dengue.level(), dengue.incidencePer100k());
  }

  private static String grain(DayScore day) {
    return "environmental=city Open-Meteo forecast, coverage "
        + day.environmental().coverage()
        + "; health=municipal InfoDengue, coverage "
        + day.health().coverage()
        + "; social=no ingested source; missing="
        + String.join(",", day.environmental().missing())
        + ","
        + String.join(",", day.health().missing())
        + ","
        + String.join(",", day.social().missing());
  }
}
