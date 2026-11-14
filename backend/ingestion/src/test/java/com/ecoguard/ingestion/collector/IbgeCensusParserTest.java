package com.ecoguard.ingestion.collector;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;

class IbgeCensusParserTest {
  @Test
  void readsSaoPauloDistrictRowsFromTheZip() throws Exception {
    String csv =
        """
        CD_MUN;CD_DIST;NM_DIST;AREA_KM2;v0001;v0007
        3550308;355030801;Água Rasa;7,17;1000;400
        3500000;350000001;Outra;1,0;10;4
        """;
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
      zip.putNextEntry(new ZipEntry("distritos.csv"));
      zip.write(csv.getBytes(StandardCharsets.ISO_8859_1));
      zip.closeEntry();
    }
    var rows = IbgeCensusParser.rows(bytes.toByteArray());
    assertEquals(1, rows.size());
    assertEquals("Água Rasa", rows.get(0).name());
    assertEquals(7.17, rows.get(0).areaKm2());
  }
}
