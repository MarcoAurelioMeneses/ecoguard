package com.ecoguard.ingestion.collector;

import com.ecoguard.domain.ingestion.Records.Extraction;
import com.ecoguard.domain.ingestion.Records.FireDay;
import com.ecoguard.domain.port.Ports.SourcePort;
import com.ecoguard.ingestion.config.BackfillProperties;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Downloads INPE files first. The database transaction starts only when the rows are stored. */
@Component
public class InpeFireCollector implements SourcePort {
  private static final Pattern FILE = Pattern.compile("focos_diario_br_(\\d{8})\\.csv");
  private static final DateTimeFormatter DAY = DateTimeFormatter.BASIC_ISO_DATE;

  private final RestClient http;
  private final BackfillProperties backfill;

  public InpeFireCollector(RestClient http, BackfillProperties backfill) {
    this.http = http;
    this.backfill = backfill;
  }

  @Override
  public String sourceName() {
    return "inpe-fires";
  }

  @Override
  public Extraction extract(Instant extractedAt) {
    List<String> files =
        InpeFireParser.files(HttpFetch.get(http, backfill.inpeDailyCsv(), String.class));
    int from = Math.max(0, files.size() - backfill.days());
    List<FireDay> days = new ArrayList<>();
    for (String file : files.subList(from, files.size())) {
      Matcher matcher = FILE.matcher(file);
      if (!matcher.matches()) {
        continue;
      }
      LocalDate date = LocalDate.parse(matcher.group(1), DAY);
      String csv = HttpFetch.get(http, backfill.inpeDailyCsv() + file, String.class);
      days.add(new FireDay(date, InpeFireParser.hotspots(csv), extractedAt));
    }
    return new Extraction(
        sourceName(),
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        List.copyOf(days),
        List.of());
  }
}
