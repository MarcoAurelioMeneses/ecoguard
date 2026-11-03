package com.ecoguard.domain.risk;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Draft indicator rules for the sources this job can read. A missing indicator is listed. A pillar
 * uses only the indicators that are present, records that coverage, and renormalizes the weights
 * that are present once coverage reaches {@link #MINIMUM_COVERAGE}. Overall is published when
 * every pillar meets that minimum.
 */
public final class ScoreRules {
  public static final BigDecimal MINIMUM_COVERAGE = new BigDecimal("0.50");
  public static final BigDecimal HEAT_WEIGHT = new BigDecimal("0.25");
  public static final BigDecimal RAIN_WEIGHT = new BigDecimal("0.30");
  public static final BigDecimal AIR_WEIGHT = new BigDecimal("0.30");
  public static final BigDecimal FIRE_WEIGHT = new BigDecimal("0.15");
  public static final BigDecimal DENGUE_WEIGHT = new BigDecimal("0.60");
  public static final BigDecimal RESPIRATORY_WEIGHT = new BigDecimal("0.40");
  public static final BigDecimal DENSITY_WEIGHT = new BigDecimal("0.25");
  public static final BigDecimal SANITATION_WEIGHT = new BigDecimal("0.25");
  public static final BigDecimal INCOME_WEIGHT = new BigDecimal("0.50");
  private static final BigDecimal HUNDRED = new BigDecimal("100");
  private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

  private ScoreRules() {}

  public record Pillar(
      BigDecimal score, BigDecimal coverage, boolean complete, List<String> missing) {}

  public record DayScore(
      Pillar environmental,
      Pillar health,
      Pillar social,
      BigDecimal overall,
      BigDecimal heat,
      BigDecimal rain,
      BigDecimal dengue,
      String riskClass) {}

  public static BigDecimal heatForecast(BigDecimal temperatureMaxC) {
    if (temperatureMaxC == null) {
      return null;
    }
    if (temperatureMaxC.compareTo(new BigDecimal("25")) <= 0) {
      return ZERO;
    }
    if (temperatureMaxC.compareTo(new BigDecimal("35")) >= 0) {
      return HUNDRED.setScale(2, RoundingMode.HALF_UP);
    }
    return temperatureMaxC
        .subtract(new BigDecimal("25"))
        .multiply(BigDecimal.TEN)
        .setScale(2, RoundingMode.HALF_UP);
  }

  public static BigDecimal rainForecast(BigDecimal precipitationMm) {
    if (precipitationMm == null) {
      return null;
    }
    if (precipitationMm.signum() <= 0) {
      return ZERO;
    }
    if (precipitationMm.compareTo(new BigDecimal("80")) >= 0) {
      return HUNDRED.setScale(2, RoundingMode.HALF_UP);
    }
    return precipitationMm.multiply(HUNDRED).divide(new BigDecimal("80"), 2, RoundingMode.HALF_UP);
  }

  /** Level 1→15, 2→40, 3→70, 4→100. Without a level, incidence reaches 100 at 100 per 100,000. */
  public static BigDecimal dengue(Integer level, BigDecimal incidencePer100k) {
    if (level != null) {
      return switch (level) {
        case 1 -> new BigDecimal("15.00");
        case 2 -> new BigDecimal("40.00");
        case 3 -> new BigDecimal("70.00");
        case 4 -> HUNDRED.setScale(2, RoundingMode.HALF_UP);
        default -> null;
      };
    }
    if (incidencePer100k == null) {
      return null;
    }
    if (incidencePer100k.signum() <= 0) {
      return ZERO;
    }
    if (incidencePer100k.compareTo(HUNDRED) >= 0) {
      return HUNDRED.setScale(2, RoundingMode.HALF_UP);
    }
    return incidencePer100k.setScale(2, RoundingMode.HALF_UP);
  }

  /** 0 at or below 5 µg/m³ daily mean PM2.5, 100 at or above 50. */
  public static BigDecimal airQuality(BigDecimal pm25DailyMean) {
    if (pm25DailyMean == null) {
      return null;
    }
    if (pm25DailyMean.compareTo(new BigDecimal("5")) <= 0) {
      return ZERO;
    }
    if (pm25DailyMean.compareTo(new BigDecimal("50")) >= 0) {
      return HUNDRED.setScale(2, RoundingMode.HALF_UP);
    }
    return pm25DailyMean
        .subtract(new BigDecimal("5"))
        .multiply(HUNDRED)
        .divide(new BigDecimal("45"), 2, RoundingMode.HALF_UP);
  }

  /** Null when the day's file was not read. Zero hotspots is a real zero, not missing data. */
  public static BigDecimal fire(Integer hotspotCount) {
    if (hotspotCount == null) {
      return null;
    }
    return hotspotCount > 0 ? HUNDRED.setScale(2, RoundingMode.HALF_UP) : ZERO;
  }

  /** Higher density is higher vulnerability. 0 at or below 2,000/km², 100 at or above 20,000. */
  public static BigDecimal densityRisk(BigDecimal population, BigDecimal areaKm2) {
    if (population == null || areaKm2 == null || areaKm2.signum() == 0) {
      return null;
    }
    BigDecimal density = population.divide(areaKm2, 2, RoundingMode.HALF_UP);
    if (density.compareTo(new BigDecimal("2000")) <= 0) {
      return ZERO;
    }
    if (density.compareTo(new BigDecimal("20000")) >= 0) {
      return HUNDRED.setScale(2, RoundingMode.HALF_UP);
    }
    return density
        .subtract(new BigDecimal("2000"))
        .multiply(HUNDRED)
        .divide(new BigDecimal("18000"), 2, RoundingMode.HALF_UP);
  }

  /** Share of occupied households not on the public sewer network, as a 0–100 risk. */
  public static BigDecimal sanitationRisk(BigDecimal occupiedHouseholds, BigDecimal onNetwork) {
    if (occupiedHouseholds == null || onNetwork == null || occupiedHouseholds.signum() == 0) {
      return null;
    }
    BigDecimal without = occupiedHouseholds.subtract(onNetwork).max(BigDecimal.ZERO);
    return without.multiply(HUNDRED).divide(occupiedHouseholds, 2, RoundingMode.HALF_UP);
  }

  public static Pillar environmental(
      BigDecimal heat, BigDecimal rain, BigDecimal air, BigDecimal fireScore) {
    return partial(
        BigDecimal.ONE,
        new Term("heat_forecast", HEAT_WEIGHT, heat),
        new Term("rain_forecast", RAIN_WEIGHT, rain),
        new Term("air_quality", AIR_WEIGHT, air),
        new Term("fire", FIRE_WEIGHT, fireScore));
  }

  public static Pillar health(BigDecimal dengueScore) {
    return partial(
        BigDecimal.ONE,
        new Term("dengue", DENGUE_WEIGHT, dengueScore),
        new Term("respiratory_datasus", RESPIRATORY_WEIGHT, null));
  }

  public static Pillar social(BigDecimal density, BigDecimal sanitation) {
    return partial(
        BigDecimal.ONE,
        new Term("population_density", DENSITY_WEIGHT, density),
        new Term("sanitation", SANITATION_WEIGHT, sanitation),
        new Term("income", INCOME_WEIGHT, null));
  }

  public static DayScore day(
      OverallWeights weights,
      BigDecimal heat,
      BigDecimal rain,
      BigDecimal air,
      BigDecimal fireScore,
      BigDecimal dengueScore,
      BigDecimal density,
      BigDecimal sanitation) {
    Pillar environmental = environmental(heat, rain, air, fireScore);
    Pillar health = health(dengueScore);
    Pillar social = social(density, sanitation);
    BigDecimal overall =
        environmental.score() == null || health.score() == null || social.score() == null
            ? null
            : weights.combine(environmental.score(), health.score(), social.score());
    return new DayScore(
        environmental, health, social, overall, heat, rain, dengueScore, riskClass(overall));
  }

  public static String riskClass(BigDecimal score) {
    if (score == null) {
      return "nodata";
    }
    if (score.compareTo(new BigDecimal("30")) < 0) {
      return "baixo";
    }
    if (score.compareTo(new BigDecimal("55")) < 0) {
      return "moderado";
    }
    if (score.compareTo(new BigDecimal("75")) < 0) {
      return "alto";
    }
    return "critico";
  }

  public static String layerClass(DayScore score, String layer) {
    Pillar pillar =
        switch (layer) {
          case "environmental" -> score.environmental();
          case "health" -> score.health();
          case "social" -> score.social();
          default -> null;
        };
    if (pillar == null) {
      return riskClass(score.overall());
    }
    return riskClass(pillar.score());
  }

  private record Term(String id, BigDecimal weight, BigDecimal value) {}

  private static Pillar partial(BigDecimal totalWeight, Term... terms) {
    List<String> missing = new ArrayList<>();
    BigDecimal weighted = BigDecimal.ZERO;
    BigDecimal used = BigDecimal.ZERO;
    for (Term term : terms) {
      if (term.value() == null) {
        missing.add(term.id());
        continue;
      }
      weighted = weighted.add(term.weight().multiply(term.value()));
      used = used.add(term.weight());
    }
    BigDecimal coverage =
        totalWeight.signum() == 0 ? ZERO : used.divide(totalWeight, 2, RoundingMode.HALF_UP);
    if (used.signum() == 0 || coverage.compareTo(MINIMUM_COVERAGE) < 0) {
      return new Pillar(null, coverage, false, List.copyOf(missing));
    }
    BigDecimal score = weighted.divide(used, 2, RoundingMode.HALF_UP);
    return new Pillar(score, coverage, missing.isEmpty(), List.copyOf(missing));
  }
}
