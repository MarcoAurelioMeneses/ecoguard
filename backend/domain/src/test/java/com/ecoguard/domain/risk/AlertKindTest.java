package com.ecoguard.domain.risk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AlertKindTest {
  @Test
  void coldIsPresentAndFloodWasRenamed() {
    assertEquals(AlertKind.FRIO, AlertKind.valueOf("FRIO"));
    assertEquals(AlertKind.HIDROLOGICO, AlertKind.valueOf("HIDROLOGICO"));
    assertThrows(IllegalArgumentException.class, () -> AlertKind.valueOf("ALAGAMENTO"));
  }
}
