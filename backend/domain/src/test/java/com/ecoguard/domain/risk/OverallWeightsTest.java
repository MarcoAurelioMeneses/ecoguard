package com.ecoguard.domain.risk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OverallWeightsTest {
  private static final OverallWeights CONFIRMED =
      new OverallWeights(
          new BigDecimal("0.40"), new BigDecimal("0.35"), new BigDecimal("0.25"), "confirmed");

  @Test
  void combinesConfirmedWeights() {
    assertEquals(
        0,
        new BigDecimal("51.00")
            .compareTo(
                CONFIRMED.combine(
                    new BigDecimal("80"), new BigDecimal("40"), new BigDecimal("20"))));
  }

  @Test
  void missingPillarStaysNull() {
    assertNull(CONFIRMED.combine(new BigDecimal("80"), null, new BigDecimal("20")));
  }

  @Test
  void unconfirmedWeightsCannotPublish() {
    OverallWeights draft =
        new OverallWeights(
            new BigDecimal("0.40"), new BigDecimal("0.35"), new BigDecimal("0.25"), "draft");
    assertThrows(
        IllegalStateException.class,
        () -> draft.combine(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE));
  }
}
