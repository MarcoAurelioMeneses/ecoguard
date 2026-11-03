package com.ecoguard.ingestion.collector;

import com.ecoguard.ingestion.config.BackfillProperties;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipInputStream;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * IBGE Censo 2022 universe aggregates already published at district grain. Income is not in that
 * release and stays missing.
 */
@Service
public class IbgeCensusCollector {
  private final RestClient http;
  private final JdbcClient jdbc;
  private final BackfillProperties backfill;

  public IbgeCensusCollector(RestClient http, JdbcClient jdbc, BackfillProperties backfill) {
    this.http = http;
    this.jdbc = jdbc;
    this.backfill = backfill;
  }

  public int extract(Instant extractedAt) {
    Map<String, String> codes = districtCodesByName();
    Map<String, Census> rows = new HashMap<>();
    read(
        backfill.ibgeBasicoUrl(),
        (code, name, values) -> {
          Census census = rows.computeIfAbsent(code, key -> new Census(code, name));
          census.area = decimal(values.get("AREA_KM2"));
          census.population = decimal(values.get("v0001"));
          census.occupied = decimal(values.get("v0007"));
        });
    read(
        backfill.ibgeDomicileUrl(),
        (code, name, values) -> {
          Census census = rows.computeIfAbsent(code, key -> new Census(code, name));
          census.sewage = decimal(values.get("V00309"));
        });
    int written = 0;
    for (Census census : rows.values()) {
      String district = codes.get(normalize(census.name));
      if (district == null || census.population == null || census.area == null) {
        continue;
      }
      written +=
          jdbc.sql(
                  """
                  INSERT INTO staging.district_census (
                    district_code, ibge_district_code, district_name, population, area_km2,
                    occupied_households, sewage_network_households, reference_year, source, extracted_at)
                  VALUES (
                    :code, :ibge, :name, :population, :area, :occupied, :sewage, 2022,
                    'ibge:censo2022:agregados-por-distrito', :extracted)
                  ON CONFLICT (district_code) DO UPDATE SET
                    population = EXCLUDED.population,
                    area_km2 = EXCLUDED.area_km2,
                    occupied_households = EXCLUDED.occupied_households,
                    sewage_network_households = EXCLUDED.sewage_network_households,
                    extracted_at = EXCLUDED.extracted_at
                  """)
              .param("code", district)
              .param("ibge", census.code)
              .param("name", census.name)
              .param("population", census.population)
              .param("area", census.area)
              .param("occupied", census.occupied)
              .param("sewage", census.sewage)
              .param("extracted", java.sql.Timestamp.from(extractedAt))
              .update();
    }
    if (written != 96) {
      throw new IllegalStateException(
          "IBGE district match wrote " + written + " rows, expected 96");
    }
    return written;
  }

  private Map<String, String> districtCodesByName() {
    Map<String, String> codes = new HashMap<>();
    jdbc.sql("SELECT district_code, name FROM marts.districts")
        .query(
            (rs, row) -> {
              codes.put(normalize(rs.getString("name")), rs.getString("district_code"));
              return null;
            })
        .list();
    return codes;
  }

  private void read(String url, RowConsumer consumer) {
    byte[] zip =
        http.get()
            .uri(url)
            .header("User-Agent", "EcoGuard-ingestion/0.1")
            .retrieve()
            .body(byte[].class);
    if (zip == null) {
      throw new IllegalStateException("IBGE download was empty: " + url);
    }
    try (ZipInputStream input = new ZipInputStream(new java.io.ByteArrayInputStream(zip))) {
      if (input.getNextEntry() == null) {
        throw new IllegalStateException("IBGE zip had no entry: " + url);
      }
      BufferedReader reader =
          new BufferedReader(new InputStreamReader(input, StandardCharsets.ISO_8859_1));
      String header = reader.readLine();
      if (header == null) {
        throw new IllegalStateException("IBGE csv had no header: " + url);
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
        consumer.accept(values.get("CD_DIST"), values.get("NM_DIST"), values);
      }
    } catch (java.io.IOException ex) {
      throw new IllegalStateException("could not read IBGE zip " + url, ex);
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

  static String normalize(String name) {
    String stripped = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    return stripped.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
  }

  @FunctionalInterface
  private interface RowConsumer {
    void accept(String code, String name, Map<String, String> values);
  }

  private static final class Census {
    private final String code;
    private final String name;
    private Double area;
    private Double population;
    private Double occupied;
    private Double sewage;

    private Census(String code, String name) {
      this.code = code;
      this.name = name;
    }
  }
}
