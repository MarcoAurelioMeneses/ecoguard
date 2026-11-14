package com.ecoguard.domain.risk;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Four-week dengue incidence from COVISA cumulative district bulletins (section 4, Quadro 10).
 * Year-to-date incidence in the bulletin is context. The score uses {@code incidence_4w}.
 */
public final class CovisaDengue {
  public static final String POPULATION_SOURCE = "covisa_implied";
  public static final String LICENCE = "use with source citation; licence not verified";
  private static final BigDecimal HUNDRED = new BigDecimal("100");
  private static final BigDecimal HUNDRED_THOUSAND = new BigDecimal("100000");
  private static final BigDecimal FOUR = new BigDecimal("4");
  private static final BigDecimal TEN_PERCENT = new BigDecimal("0.10");

  private CovisaDengue() {}

  public record Edition(
      int year,
      int epiWeek,
      LocalDate weekStart,
      LocalDate weekEnd,
      int cumulativeCases,
      BigDecimal incidencePer100k) {}

  public record Window(
      int cases,
      BigDecimal incidence4w,
      int windowWeeks,
      int editionsUsed,
      boolean revisionFlag,
      boolean scored,
      BigDecimal impliedPopulation,
      int yearToDateCases) {

    static Window missing() {
      return new Window(0, null, 0, 0, false, false, null, 0);
    }
  }

  public static Window at(List<Edition> editions, LocalDate scoreDate) {
    Edition current = editionValidAt(editions, scoreDate);
    if (current == null || current.cumulativeCases() <= 0 || current.incidencePer100k() == null) {
      return Window.missing();
    }
    if (current.incidencePer100k().signum() <= 0) {
      return Window.missing();
    }
    Edition baseline = baseline(editions, current);
    int startCases = 0;
    int weeks = current.epiWeek();
    int editionsUsed = 1;
    if (baseline != null) {
      startCases = baseline.cumulativeCases();
      weeks = current.epiWeek() - baseline.epiWeek();
      editionsUsed = 2;
    }
    if (weeks < 1) {
      weeks = 1;
    }
    int signed = current.cumulativeCases() - startCases;
    boolean revision = signed < 0;
    int magnitude = Math.abs(signed);
    if (revision
        && magnitude >= 5
        && BigDecimal.valueOf(magnitude)
                .compareTo(BigDecimal.valueOf(startCases).multiply(TEN_PERCENT))
            >= 0) {
      return new Window(
          0,
          null,
          weeks,
          editionsUsed,
          true,
          false,
          impliedPopulation(current),
          current.cumulativeCases());
    }
    int cases = Math.max(0, signed);
    BigDecimal population = impliedPopulation(current);
    BigDecimal incidence =
        BigDecimal.valueOf(cases)
            .multiply(HUNDRED_THOUSAND)
            .multiply(FOUR)
            .divide(population.multiply(BigDecimal.valueOf(weeks)), 2, RoundingMode.HALF_UP);
    if (incidence.compareTo(HUNDRED) > 0) {
      incidence = HUNDRED.setScale(2, RoundingMode.HALF_UP);
    }
    return new Window(
        cases,
        incidence,
        weeks,
        editionsUsed,
        revision,
        true,
        population,
        current.cumulativeCases());
  }

  private static BigDecimal impliedPopulation(Edition current) {
    return BigDecimal.valueOf(current.cumulativeCases())
        .multiply(HUNDRED_THOUSAND)
        .divide(current.incidencePer100k(), 2, RoundingMode.HALF_UP);
  }

  private static Edition baseline(List<Edition> editions, Edition current) {
    if (current.epiWeek() < 4) {
      return null;
    }
    Edition exact = find(editions, current.year(), current.epiWeek() - 4);
    if (exact != null) {
      return exact;
    }
    Edition earlier = null;
    for (Edition edition : editions) {
      if (edition.year() != current.year() || edition.epiWeek() >= current.epiWeek()) {
        continue;
      }
      if (earlier == null || edition.epiWeek() > earlier.epiWeek()) {
        earlier = edition;
      }
    }
    return earlier;
  }

  private static Edition find(List<Edition> editions, int year, int week) {
    Edition found = null;
    for (Edition edition : editions) {
      if (edition.year() == year && edition.epiWeek() == week) {
        found = edition;
      }
    }
    return found;
  }

  static Edition editionValidAt(List<Edition> editions, LocalDate scoreDate) {
    Edition containing = null;
    Edition started = null;
    for (Edition edition : editions) {
      if (edition.weekStart() == null || edition.weekStart().isAfter(scoreDate)) {
        continue;
      }
      if (started == null || edition.weekStart().isAfter(started.weekStart())) {
        started = edition;
      }
      boolean open = edition.weekEnd() == null || !scoreDate.isAfter(edition.weekEnd());
      if (open && (containing == null || edition.weekStart().isAfter(containing.weekStart()))) {
        containing = edition;
      }
    }
    return containing != null ? containing : started;
  }
}
