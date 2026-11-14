package com.ecoguard.ingestion.collector;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

/** Reads one IBGE district-aggregate zip. */
public final class IbgeCensusParser {
  private IbgeCensusParser() {}

  public record Row(
      String code,
      String name,
      Double population,
      Double areaKm2,
      Double occupiedHouseholds,
      Double sewageNetworkHouseholds) {}

  public static List<Row> rows(byte[] zip) {
    if (zip == null || zip.length == 0) {
      throw new IllegalStateException("IBGE download was empty");
    }
    List<Row> rows = new ArrayList<>();
    try (ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(zip))) {
      if (input.getNextEntry() == null) {
        throw new IllegalStateException("IBGE zip had no entry");
      }
      BufferedReader reader =
          new BufferedReader(new InputStreamReader(input, StandardCharsets.ISO_8859_1));
      String header = reader.readLine();
      if (header == null) {
        throw new IllegalStateException("IBGE csv had no header");
      }
      String[] columns = split(header);
      String line;
      while ((line = reader.readLine()) != null) {
        String[] cells = split(line);
        Map<String, String> values = new HashMap<>();
        for (int i = 0; i < columns.length && i < cells.length; i++) {
          values.put(columns[i], cells[i]);
        }
        String district = values.get("CD_DIST");
        boolean saoPaulo =
            "3550308".equals(values.get("CD_MUN"))
                || (district != null && district.startsWith("3550308"));
        if (!saoPaulo) {
          continue;
        }
        rows.add(
            new Row(
                district,
                values.get("NM_DIST"),
                decimal(values.get("v0001")),
                decimal(values.get("AREA_KM2")),
                decimal(values.get("v0007")),
                decimal(values.get("V00309"))));
      }
      return rows;
    } catch (IOException ex) {
      throw new IllegalStateException("could not read IBGE zip", ex);
    }
  }

  private static String[] split(String line) {
    String[] cells = line.split(";", -1);
    for (int i = 0; i < cells.length; i++) {
      cells[i] = cells[i].replace("\"", "").trim();
    }
    return cells;
  }

  private static Double decimal(String raw) {
    if (raw == null || raw.isBlank() || "X".equalsIgnoreCase(raw) || "...".equals(raw)) {
      return null;
    }
    return Double.valueOf(raw.replace(".", "").replace(",", "."));
  }
}
