package com.ecoguard.ingestion.collector;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class InpeFireParserTest {
  @Test
  void keepsSaoPauloHotspotsFromTheDailyCsv() {
    String csv =
        """
        lat,lon,municipio_id
        -23.5,-46.6,3550308
        -10.0,-40.0,3550309
        """;
    assertEquals(1, InpeFireParser.hotspots(csv).size());
    assertEquals(
        "focos_diario_br_20261102.csv",
        InpeFireParser.files("<a href=\"focos_diario_br_20261102.csv\">x</a>").get(0));
  }
}
