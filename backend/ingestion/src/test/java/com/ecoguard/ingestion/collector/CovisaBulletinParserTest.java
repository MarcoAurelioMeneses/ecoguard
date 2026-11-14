package com.ecoguard.ingestion.collector;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ecoguard.domain.ingestion.CensusNames;
import com.ecoguard.domain.risk.CovisaDengue;
import com.ecoguard.domain.risk.CovisaDengue.Edition;
import com.ecoguard.domain.risk.CovisaDengue.Window;
import com.ecoguard.ingestion.collector.CovisaBulletinParser.Row;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class CovisaBulletinParserTest {
  @Test
  void se43AgainstSe39GatesTheParsedRows() throws Exception {
    Edition tiradentes39 = edition("covisa/boletimarbo_se39_03out25.pdf", "CIDADETIRADENTES");
    Edition tiradentes43 = edition("covisa/boletimarbo_se43_31out25.pdf", "CIDADETIRADENTES");
    Edition liberdade39 = edition("covisa/boletimarbo_se39_03out25.pdf", "LIBERDADE");
    Edition liberdade43 = edition("covisa/boletimarbo_se43_31out25.pdf", "LIBERDADE");
    assertEquals(1429, tiradentes39.cumulativeCases());
    assertEquals(1525, tiradentes43.cumulativeCases());
    assertEquals(203, liberdade39.cumulativeCases());
    assertEquals(206, liberdade43.cumulativeCases());
    assertEquals(0, new BigDecimal("629.1").compareTo(tiradentes43.incidencePer100k()));
    assertEquals(0, new BigDecimal("281.5").compareTo(liberdade43.incidencePer100k()));

    Window tiradentes =
        CovisaDengue.at(List.of(tiradentes39, tiradentes43), LocalDate.parse("2025-10-28"));
    Window liberdade =
        CovisaDengue.at(List.of(liberdade39, liberdade43), LocalDate.parse("2025-10-28"));
    assertEquals(96, tiradentes.cases());
    assertEquals(0, new BigDecimal("39.60").compareTo(tiradentes.incidence4w()));
    assertEquals(4, tiradentes.windowWeeks());
    assertEquals(2, tiradentes.editionsUsed());
    assertEquals(3, liberdade.cases());
    assertEquals(0, new BigDecimal("4.10").compareTo(liberdade.incidence4w()));
    assertEquals("use with source citation; licence not verified", CovisaBulletinParser.licence());
  }

  private static Edition edition(String path, String normalized) throws Exception {
    Edition found = null;
    for (Row row : CovisaBulletinParser.parse(bytes(path))) {
      if (CensusNames.normalize(row.districtName()).equals(normalized)) {
        found = row.edition();
      }
    }
    if (found == null) {
      throw new AssertionError(normalized);
    }
    return found;
  }

  private static byte[] bytes(String path) throws Exception {
    try (InputStream input =
        CovisaBulletinParserTest.class.getClassLoader().getResourceAsStream(path)) {
      return input.readAllBytes();
    }
  }
}
