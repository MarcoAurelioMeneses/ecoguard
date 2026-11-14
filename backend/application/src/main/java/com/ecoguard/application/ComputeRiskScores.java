package com.ecoguard.application;

import com.ecoguard.domain.risk.CovisaDengue;
import com.ecoguard.domain.risk.CovisaDengue.Edition;
import com.ecoguard.domain.risk.CovisaDengue.Window;
import com.ecoguard.domain.risk.OverallWeights;
import com.ecoguard.domain.risk.ScoreInputs.DistrictCovisa;
import com.ecoguard.domain.risk.ScoreInputs.DistrictDay;
import com.ecoguard.domain.risk.ScoreInputs.PersistedScore;
import com.ecoguard.domain.risk.ScoreInputs.Readings;
import com.ecoguard.domain.risk.ScoreInputs.Writer;
import com.ecoguard.domain.risk.ScoreRules;
import com.ecoguard.domain.risk.ScoreRules.DayScore;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** One score row per district and forecast date. The caller owns the transaction. */
public final class ComputeRiskScores {
  public int execute(Readings readings, Writer writer, Instant computedAt, String configKey) {
    OverallWeights weights = readings.weights(configKey);
    Map<String, List<Edition>> covisa = covisaByDistrict(readings.covisaEditions());
    List<PersistedScore> rows = new ArrayList<>();
    for (DistrictDay observation : readings.districtDays()) {
      BigDecimal heat = ScoreRules.heatForecast(observation.temperatureMaxC());
      BigDecimal rain = ScoreRules.rainForecast(observation.precipitationMm());
      BigDecimal air = ScoreRules.airQuality(observation.pm25DailyMean());
      Window dengueWindow =
          CovisaDengue.at(
              covisa.getOrDefault(observation.districtCode(), List.of()), observation.date());
      BigDecimal dengueScore =
          dengueWindow.scored() ? ScoreRules.dengue(null, dengueWindow.incidence4w()) : null;
      BigDecimal density = ScoreRules.densityRisk(observation.population(), observation.areaKm2());
      BigDecimal sanitation =
          ScoreRules.sanitationRisk(
              observation.occupiedHouseholds(), observation.sewageNetworkHouseholds());
      DayScore day = ScoreRules.day(weights, heat, rain, air, dengueScore, density, sanitation);
      rows.add(
          new PersistedScore(
              observation.districtCode(),
              observation.date(),
              configKey,
              day,
              computedAt,
              grain(day, dengueWindow)));
    }
    if (rows.isEmpty()) {
      return 0;
    }
    int written = writer.upsert(rows);
    writer.appendHistory(rows);
    return written;
  }

  private static Map<String, List<Edition>> covisaByDistrict(List<DistrictCovisa> rows) {
    Map<String, List<Edition>> grouped = new HashMap<>();
    for (DistrictCovisa row : rows) {
      grouped.computeIfAbsent(row.districtCode(), ignored -> new ArrayList<>()).add(row.edition());
    }
    return grouped;
  }

  private static String grain(DayScore day, Window dengue) {
    return "environmental=Open-Meteo forecast and PM2.5 at the district point; "
        + "INPE hotspots are informational and stay out of the KPI, coverage "
        + day.environmental().coverage()
        + "; health=COVISA incidence_4w window_weeks="
        + dengue.windowWeeks()
        + " editions_used="
        + dengue.editionsUsed()
        + " revision_flag="
        + dengue.revisionFlag()
        + " cases="
        + dengue.cases()
        + " population_source="
        + CovisaDengue.POPULATION_SOURCE
        + ", coverage "
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
