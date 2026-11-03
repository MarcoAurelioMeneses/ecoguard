package com.ecoguard.domain.risk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
  void partialPillarsDoNotPublishAnOverall() {
    ScoreRules.DayScore day =
        ScoreRules.day(
            WEIGHTS,
            ScoreRules.heatForecast(new BigDecimal("35")),
            ScoreRules.rainForecast(BigDecimal.ZERO),
            new BigDecimal("70.00"));
    assertEquals(0, new BigDecimal("45.45").compareTo(day.environmental().score()));
    assertEquals(0, new BigDecimal("0.55").compareTo(day.environmental().coverage()));
    assertFalse(day.environmental().complete());
    assertTrue(day.environmental().missing().contains("air_quality"));
    assertEquals(0, new BigDecimal("70.00").compareTo(day.health().score()));
    assertFalse(day.health().complete());
    assertNull(day.social().score());
    assertNull(day.overall());
    assertEquals("nodata", day.riskClass());
    assertEquals("moderado", ScoreRules.layerClass(day, "environmental"));
    assertEquals("alto", ScoreRules.layerClass(day, "health"));
    assertEquals("nodata", ScoreRules.layerClass(day, "social"));
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
