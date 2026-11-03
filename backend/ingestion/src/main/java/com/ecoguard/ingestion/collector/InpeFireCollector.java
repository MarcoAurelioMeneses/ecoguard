package com.ecoguard.ingestion.collector;

import com.ecoguard.ingestion.config.BackfillProperties;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

/** Counts INPE daily hotspots that fall inside each district. */
@Service
public class InpeFireCollector {
  private static final Pattern FILE = Pattern.compile("focos_diario_br_(\\d{8})\\.csv");
  private static final DateTimeFormatter DAY = DateTimeFormatter.BASIC_ISO_DATE;

  private final RestClient http;
  private final JdbcClient jdbc;
  private final BackfillProperties backfill;

  public InpeFireCollector(RestClient http, JdbcClient jdbc, BackfillProperties backfill) {
    this.http = http;
    this.jdbc = jdbc;
    this.backfill = backfill;
  }

  @Transactional
  public int extract(Instant extractedAt) {
    List<String> files = listedFiles();
    int from = Math.max(0, files.size() - backfill.days());
    int written = 0;
    for (String file : files.subList(from, files.size())) {
      Matcher matcher = FILE.matcher(file);
      if (!matcher.matches()) {
        continue;
      }
      LocalDate date = LocalDate.parse(matcher.group(1), DAY);
      List<double[]> points = hotspotsInSaoPaulo(file);
      written += store(date, points, extractedAt);
    }
    return written;
  }

  private List<String> listedFiles() {
    String html =
        http.get()
            .uri(backfill.inpeDailyCsv())
            .header("User-Agent", "EcoGuard-ingestion/0.1")
            .retrieve()
            .body(String.class);
    if (html == null) {
      throw new IllegalStateException("INPE daily index was empty");
    }
    Matcher matcher = FILE.matcher(html);
    List<String> files = new ArrayList<>();
    while (matcher.find()) {
      files.add(matcher.group());
    }
    files.sort(String::compareTo);
    if (files.isEmpty()) {
      throw new IllegalStateException("INPE daily index listed no CSV files");
    }
    return files;
  }

  private List<double[]> hotspotsInSaoPaulo(String file) {
    String csv =
        http.get()
            .uri(backfill.inpeDailyCsv() + file)
            .header("User-Agent", "EcoGuard-ingestion/0.1")
            .retrieve()
            .body(String.class);
    if (csv == null) {
      throw new IllegalStateException("INPE file was empty: " + file);
    }
    String[] lines = csv.split("\\R");
    String[] header = lines[0].split(",");
    int lat = index(header, "lat");
    int lon = index(header, "lon");
    int municipality = index(header, "municipio_id");
    List<double[]> points = new ArrayList<>();
    for (int i = 1; i < lines.length; i++) {
      if (lines[i].isBlank()) {
        continue;
      }
      String[] cells = lines[i].split(",");
      if (cells.length <= municipality) {
        continue;
      }
      if (!"3550308".equals(cells[municipality].trim())) {
        continue;
      }
      points.add(
          new double[] {
            Double.parseDouble(cells[lat].trim()), Double.parseDouble(cells[lon].trim())
          });
    }
    return points;
  }

  private int store(LocalDate date, List<double[]> points, Instant extractedAt) {
    jdbc.sql(
            "CREATE TEMP TABLE IF NOT EXISTS fire_points (lat double precision, lon double precision)")
        .update();
    jdbc.sql("TRUNCATE fire_points").update();
    for (double[] point : points) {
      jdbc.sql("INSERT INTO fire_points (lat, lon) VALUES (:lat, :lon)")
          .param("lat", point[0])
          .param("lon", point[1])
          .update();
    }
    return jdbc.sql(
            """
            INSERT INTO staging.district_fire_daily (
              district_code, observation_date, hotspot_count, source, extracted_at)
            SELECT d.district_code, :date, count(p.lat)::int,
                   'inpe:csv:diario:Brasil', :extracted
            FROM marts.districts d
            LEFT JOIN fire_points p
              ON ST_Within(ST_SetSRID(ST_MakePoint(p.lon, p.lat), 4326), d.geom)
            GROUP BY d.district_code
            ON CONFLICT (district_code, observation_date) DO UPDATE SET
              hotspot_count = EXCLUDED.hotspot_count,
              extracted_at = EXCLUDED.extracted_at
            """)
        .param("date", java.sql.Date.valueOf(date))
        .param("extracted", java.sql.Timestamp.from(extractedAt))
        .update();
  }

  private static int index(String[] header, String name) {
    for (int i = 0; i < header.length; i++) {
      if (name.equals(header[i].trim())) {
        return i;
      }
    }
    throw new IllegalStateException("INPE CSV is missing column " + name);
  }
}
