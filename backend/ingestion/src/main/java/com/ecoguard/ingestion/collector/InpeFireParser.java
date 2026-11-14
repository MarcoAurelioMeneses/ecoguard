package com.ecoguard.ingestion.collector;

import com.ecoguard.domain.ingestion.Records.FireHotspot;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Turns the INPE directory listing and daily CSV into São Paulo hotspots. */
public final class InpeFireParser {
  private static final Pattern FILE = Pattern.compile("focos_diario_br_(\\d{8})\\.csv");

  private InpeFireParser() {}

  public static List<String> files(String html) {
    if (html == null || html.isBlank()) {
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

  public static List<FireHotspot> hotspots(String csv) {
    if (csv == null || csv.isBlank()) {
      throw new IllegalStateException("INPE file was empty");
    }
    String[] lines = csv.split("\\R");
    String[] header = lines[0].split(",");
    int lat = index(header, "lat");
    int lon = index(header, "lon");
    int municipality = index(header, "municipio_id");
    List<FireHotspot> points = new ArrayList<>();
    for (int i = 1; i < lines.length; i++) {
      if (lines[i].isBlank()) {
        continue;
      }
      String[] cells = lines[i].split(",");
      if (!"3550308".equals(cells[municipality].trim())) {
        continue;
      }
      points.add(
          new FireHotspot(
              Double.parseDouble(cells[lat].trim()), Double.parseDouble(cells[lon].trim())));
    }
    return points;
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
