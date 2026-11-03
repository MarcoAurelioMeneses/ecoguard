package com.ecoguard.domain.risk;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Draft indicator rules for the sources this job can read. A missing indicator is listed. A pillar
 * uses only the indicators that are present and records that coverage. Overall stays null unless
 * every pillar is complete.
 */
public final class ScoreRules {
  public static final BigDecimal HEAT_WEIGHT = new BigDecimal("0.25");
  public static final BigDecimal RAIN_WEIGHT = new BigDecimal("0.30");
  public static final BigDecimal DENGUE_WEIGHT = new BigDecimal("0.60");
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

  public static Pillar environmental(BigDecimal heat, BigDecimal rain) {
    List<String> missing = new ArrayList<>(List.of("air_quality", "fire"));
    return partial(
        missing,
        new Term("heat_forecast", HEAT_WEIGHT, heat),
        new Term("rain_forecast", RAIN_WEIGHT, rain));
  }

  public static Pillar health(BigDecimal dengueScore) {
    return partial(
        new ArrayList<>(List.of("respiratory")), new Term("dengue", DENGUE_WEIGHT, dengueScore));
  }

  public static Pillar social() {
    return new Pillar(null, ZERO, false, List.of("income", "vulnerability"));
  }

  public static DayScore day(
      OverallWeights weights, BigDecimal heat, BigDecimal rain, BigDecimal dengueScore) {
    Pillar environmental = environmental(heat, rain);
    Pillar health = health(dengueScore);
    Pillar social = social();
    boolean complete = environmental.complete() && health.complete() && social.complete();
    BigDecimal overall =
        complete ? weights.combine(environmental.score(), health.score(), social.score()) : null;
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

  private static Pillar partial(List<String> missing, Term... terms) {
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
    if (used.signum() == 0) {
      return new Pillar(null, ZERO, false, List.copyOf(missing));
    }
    BigDecimal score = weighted.divide(used, 2, RoundingMode.HALF_UP);
    boolean complete = missing.isEmpty();
    return new Pillar(
        score, used.setScale(2, RoundingMode.HALF_UP), complete, List.copyOf(missing));
  }
}
