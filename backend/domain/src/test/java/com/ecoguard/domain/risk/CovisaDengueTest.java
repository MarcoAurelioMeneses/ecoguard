package com.ecoguard.domain.risk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecoguard.domain.risk.CovisaDengue.Edition;
import com.ecoguard.domain.risk.CovisaDengue.Window;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class CovisaDengueTest {
  @Test
  void fourWeekWindowMatchesTheRequestedArithmetic() {
    Window window =
        CovisaDengue.at(
            List.of(edition(2025, 39, 1485, "610.0"), edition(2025, 43, 1525, "629.1")),
            LocalDate.parse("2025-10-28"));
    assertEquals(40, window.cases());
    assertEquals(0, new BigDecimal("16.50").compareTo(window.incidence4w()));
    assertEquals(4, window.windowWeeks());
    assertEquals(2, window.editionsUsed());
    assertEquals(CovisaDengue.POPULATION_SOURCE, CovisaDengue.POPULATION_SOURCE);
    assertEquals(
        0, new BigDecimal("16.50").compareTo(ScoreRules.dengue(null, window.incidence4w())));
  }

  @Test
  void liberdadeOneCaseIs1_36() {
    Window window =
        CovisaDengue.at(
            List.of(edition(2025, 39, 205, "277.0"), edition(2025, 43, 206, "281.5")),
            LocalDate.parse("2025-10-28"));
    assertEquals(1, window.cases());
    assertEquals(0, new BigDecimal("1.37").compareTo(window.incidence4w()));
  }

  @Test
  void missingBaselineUsesTheSameYearWindow() {
    Window window =
        CovisaDengue.at(
            List.of(edition(2025, 41, 1473, "607.7"), edition(2025, 43, 1525, "629.1")),
            LocalDate.parse("2025-10-28"));
    assertEquals(2, window.windowWeeks());
    assertEquals(2, window.editionsUsed());
    assertEquals(52, window.cases());
    assertTrue(window.scored());
  }

  @Test
  void earlyWeeksDoNotSubtractThePreviousYear() {
    Window window =
        CovisaDengue.at(
            List.of(edition(2024, 52, 900, "100.0"), edition(2025, 2, 10, "4.0")),
            LocalDate.parse("2025-01-08"));
    assertEquals(1, window.editionsUsed());
    assertEquals(2, window.windowWeeks());
    assertEquals(10, window.cases());
    assertFalse(window.revisionFlag());
  }

  @Test
  void largeDownwardRevisionDropsTheWindow() {
    Window window =
        CovisaDengue.at(
            List.of(edition(2025, 39, 100, "40.0"), edition(2025, 43, 80, "32.0")),
            LocalDate.parse("2025-10-28"));
    assertTrue(window.revisionFlag());
    assertFalse(window.scored());
    assertNull(window.incidence4w());
  }

  @Test
  void smallDownwardRevisionFloorsAtZero() {
    Window window =
        CovisaDengue.at(
            List.of(edition(2025, 39, 100, "40.0"), edition(2025, 43, 97, "38.8")),
            LocalDate.parse("2025-10-28"));
    assertTrue(window.revisionFlag());
    assertTrue(window.scored());
    assertEquals(0, window.cases());
    assertEquals(0, new BigDecimal("0.00").compareTo(window.incidence4w()));
  }

  @Test
  void incidenceCapsAt100Per100k() {
    Window window =
        CovisaDengue.at(
            List.of(edition(2025, 39, 10, "5.0"), edition(2025, 43, 500, "250.0")),
            LocalDate.parse("2025-10-28"));
    assertEquals(0, new BigDecimal("100.00").compareTo(window.incidence4w()));
  }

  private static Edition edition(int year, int week, int cases, String incidence) {
    LocalDate start =
        LocalDate.parse(year == 2024 ? "2024-12-22" : "2025-01-05").plusWeeks(week - 1L);
    if (week == 39) {
      start = LocalDate.parse("2025-09-28");
    }
    if (week == 41) {
      start = LocalDate.parse("2025-10-12");
    }
    if (week == 43) {
      start = LocalDate.parse("2025-10-26");
    }
    if (week == 2 && year == 2025) {
      start = LocalDate.parse("2025-01-05");
    }
    return new Edition(year, week, start, start.plusDays(6), cases, new BigDecimal(incidence));
  }
}
