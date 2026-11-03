package com.ecoguard.domain.risk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ScoreRulesTest {
  private static final OverallWeights WEIGHTS =
      new OverallWeights(
          new BigDecimal("0.40"), new BigDecimal("0.35"), new BigDecimal("0.25"), "confirmed");

  @Test
  void heatAndRainFollowTheDocumentedCuts() {
    assertEquals(
        0, new BigDecimal("0.00").compareTo(ScoreRules.heatForecast(new BigDecimal("25"))));
    assertEquals(
        0, new BigDecimal("50.00").compareTo(ScoreRules.heatForecast(new BigDecimal("30"))));
    assertEquals(
        0, new BigDecimal("100.00").compareTo(ScoreRules.heatForecast(new BigDecimal("35"))));
    assertNull(ScoreRules.heatForecast(null));
    assertEquals(
        0, new BigDecimal("50.00").compareTo(ScoreRules.rainForecast(new BigDecimal("40"))));
    assertEquals(
        0, new BigDecimal("100.00").compareTo(ScoreRules.rainForecast(new BigDecimal("80"))));
  }

  @Test
  void dengueUsesLevelThenIncidence() {
    assertEquals(0, new BigDecimal("70.00").compareTo(ScoreRules.dengue(3, null)));
    assertEquals(
        0, new BigDecimal("40.00").compareTo(ScoreRules.dengue(null, new BigDecimal("40"))));
    assertNull(ScoreRules.dengue(null, null));
  }

  @Test
  void publishesOverallWhenEveryPillarMeetsMinimumCoverage() {
    ScoreRules.DayScore day =
        ScoreRules.day(
            WEIGHTS,
            new BigDecimal("100.00"),
            new BigDecimal("0.00"),
            new BigDecimal("50.00"),
            new BigDecimal("0.00"),
            new BigDecimal("70.00"),
            new BigDecimal("80.00"),
            new BigDecimal("20.00"));
    assertEquals(0, new BigDecimal("40.00").compareTo(day.environmental().score()));
    assertEquals(0, new BigDecimal("1.00").compareTo(day.environmental().coverage()));
    assertEquals(0, new BigDecimal("70.00").compareTo(day.health().score()));
    assertEquals(0, new BigDecimal("0.60").compareTo(day.health().coverage()));
    assertTrue(day.health().missing().contains("respiratory_datasus"));
    assertEquals(0, new BigDecimal("50.00").compareTo(day.social().score()));
    assertEquals(0, new BigDecimal("0.50").compareTo(day.social().coverage()));
    assertTrue(day.social().missing().contains("income"));
    assertEquals(0, new BigDecimal("53.00").compareTo(day.overall()));
    assertEquals("moderado", day.riskClass());
  }

  @Test
  void overallStaysNullBelowMinimumCoverage() {
    ScoreRules.DayScore day =
        ScoreRules.day(WEIGHTS, null, null, null, null, new BigDecimal("70"), null, null);
    assertNull(day.environmental().score());
    assertNull(day.overall());
  }

  @Test
  void classCutsMatchTheDesignBands() {
    assertEquals("baixo", ScoreRules.riskClass(new BigDecimal("29.99")));
    assertEquals("moderado", ScoreRules.riskClass(new BigDecimal("30")));
    assertEquals("alto", ScoreRules.riskClass(new BigDecimal("55")));
    assertEquals("critico", ScoreRules.riskClass(new BigDecimal("75")));
    assertEquals("nodata", ScoreRules.riskClass(null));
  }
}
