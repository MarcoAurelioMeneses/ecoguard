package com.ecoguard.ingestion.collector;

import com.ecoguard.domain.risk.CovisaDengue;
import com.ecoguard.domain.risk.CovisaDengue.Edition;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/** Section 4 of a COVISA arbovirus bulletin: cumulative dengue by district (Quadro 10). */
public final class CovisaBulletinParser {
  public record Row(String districtName, Edition edition) {}

  private static final Pattern HEADER =
      Pattern.compile(
          "SEMANA\\s+(\\d+)/(\\d+)\\s*\\((\\d{2}/\\d{2})\\s+A\\s+(\\d{2}/\\d{2})\\)",
          Pattern.CASE_INSENSITIVE);
  private static final Pattern NUMBER = Pattern.compile("\\d+(?:,\\d+)?");
  private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd/MM");

  private CovisaBulletinParser() {}

  public static List<Row> parse(byte[] pdf) {
    String text = text(pdf);
    Matcher header = HEADER.matcher(text);
    if (!header.find()) {
      throw new IllegalArgumentException("COVISA bulletin has no epidemiological week header");
    }
    int week = Integer.parseInt(header.group(1));
    int year = Integer.parseInt(header.group(2));
    LocalDate start = date(header.group(3), year);
    LocalDate end = date(header.group(4), year);
    if (end.isBefore(start)) {
      end = end.plusYears(1);
    }
    int from = indexOf(text, "4 – DENGUE", "4 - DENGUE");
    int to = text.indexOf("Quadro 11", from);
    if (from < 0 || to < from) {
      throw new IllegalArgumentException("COVISA bulletin has no dengue district table");
    }
    List<Row> rows = new ArrayList<>();
    for (String line : text.substring(from, to).split("\\R")) {
      Row row = row(line, year, week, start, end);
      if (row != null) {
        rows.add(row);
      }
    }
    if (rows.isEmpty()) {
      throw new IllegalArgumentException("COVISA dengue table had no district rows");
    }
    return List.copyOf(rows);
  }

  public static String licence() {
    return CovisaDengue.LICENCE;
  }

  private static Row row(String line, int year, int week, LocalDate start, LocalDate end) {
    Matcher numbers = NUMBER.matcher(line);
    List<String> found = new ArrayList<>();
    int first = -1;
    while (numbers.find()) {
      if (first < 0) {
        first = numbers.start();
      }
      found.add(numbers.group());
    }
    if (found.size() < 10 || first < 0) {
      return null;
    }
    String head = line.substring(0, first).trim();
    String name = districtName(head);
    if (name.isBlank()) {
      return null;
    }
    int cases = Integer.parseInt(found.get(found.size() - 2).replace(".", ""));
    BigDecimal incidence = new BigDecimal(found.get(found.size() - 1).replace(',', '.'));
    return new Row(name, new Edition(year, week, start, end, cases, incidence));
  }

  private static String districtName(String head) {
    String[] tokens = head.split("\\s+");
    List<String> upper = new ArrayList<>();
    for (int i = tokens.length - 1; i >= 0; i--) {
      String token = tokens[i];
      if (token.isBlank()
          || !token.equals(token.toUpperCase(Locale.ROOT))
          || !token.chars().anyMatch(Character::isLetter)) {
        break;
      }
      upper.add(0, token);
    }
    String name = String.join(" ", upper);
    int half = name.length() / 2;
    if (name.length() > 3 && name.length() % 2 == 1 && name.charAt(half) == ' ') {
      String left = name.substring(0, half);
      String right = name.substring(half + 1);
      if (left.equals(right)) {
        return left;
      }
    }
    return name;
  }

  private static LocalDate date(String dayMonth, int year) {
    MonthDay monthDay = MonthDay.parse(dayMonth, DAY);
    return monthDay.atYear(year);
  }

  private static int indexOf(String text, String... needles) {
    int found = -1;
    for (String needle : needles) {
      int at = text.indexOf(needle);
      if (at >= 0 && (found < 0 || at < found)) {
        found = at;
      }
    }
    return found;
  }

  private static String text(byte[] pdf) {
    try (PDDocument document = Loader.loadPDF(pdf)) {
      PDFTextStripper stripper = new PDFTextStripper();
      stripper.setSortByPosition(true);
      return stripper.getText(document);
    } catch (IOException ex) {
      throw new IllegalStateException("could not read the COVISA bulletin", ex);
    }
  }
}
