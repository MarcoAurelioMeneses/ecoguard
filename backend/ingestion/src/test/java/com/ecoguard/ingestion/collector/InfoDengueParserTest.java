package com.ecoguard.ingestion.collector;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class InfoDengueParserTest {
  @Test
  void acceptsEpochMillisecondsAndStringPopulation() {
    var row =
        InfoDengueParser.parse(
            Map.of(
                "data_iniSE",
                1760227200000L,
                "SE",
                202542,
                "casos",
                3,
                "casos_est",
                4.0,
                "nivel",
                1,
                "pop",
                "1000",
                "municipio_nome",
                "São Paulo"),
            3550308,
            "dengue",
            Instant.parse("2026-11-02T00:00:00Z"));
    assertEquals("2025-10-12", row.epiWeekStart().toString());
    assertEquals(2025, row.epiYear());
    assertEquals(42, row.epiWeek());
    assertEquals(1000.0, row.population());
    assertEquals("infodengue:3550308:dengue:202542", row.externalKey());
  }
}
