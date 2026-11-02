package com.ecoguard.ingestion.collector;

import com.ecoguard.domain.ingestion.Records.Extraction;
import com.ecoguard.domain.ingestion.Records.InfoDengueAlert;
import com.ecoguard.domain.ingestion.Records.RawRecord;
import com.ecoguard.domain.port.Ports.SourcePort;
import com.ecoguard.ingestion.config.IngestionProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class InfoDengueCollector implements SourcePort {
  private static final Set<String> DISEASES = Set.of("dengue", "chikungunya", "zika");
  private static final String USER_AGENT = "EcoGuard-ingestion/0.1";

  private final RestClient http;
  private final IngestionProperties properties;
  private final ObjectMapper mapper;

  public InfoDengueCollector(RestClient http, IngestionProperties properties, ObjectMapper mapper) {
    this.http = http;
    this.properties = properties;
    this.mapper = mapper;
  }

  @Override
  public String sourceName() {
    return "infodengue";
  }

  @Override
  public Extraction extract(Instant extractedAt) {
    String disease = properties.infodengue().disease();
    if (!DISEASES.contains(disease)) {
      throw new IllegalArgumentException("unsupported InfoDengue disease: " + disease);
    }
    List<InfoDengueAlert> rows = new ArrayList<>();
    for (int year : years()) {
      for (JsonNode item : fetchYear(year)) {
        rows.add(
            InfoDengueParser.parse(
                mapper.convertValue(item, new TypeReference<Map<String, Object>>() {}),
                properties.infodengue().geocode(),
                disease,
                extractedAt));
      }
    }
    Map<String, InfoDengueAlert> deduped = new LinkedHashMap<>();
    for (InfoDengueAlert row : rows) {
      deduped.put(row.externalKey(), row);
    }
    List<InfoDengueAlert> alerts = List.copyOf(deduped.values());
    List<RawRecord> records = new ArrayList<>();
    for (InfoDengueAlert row : alerts) {
      records.add(new RawRecord(sourceName(), row.externalKey(), payload(row), extractedAt));
    }
    return new Extraction(sourceName(), List.copyOf(records), alerts, List.of(), List.of());
  }

  private List<JsonNode> fetchYear(int year) {
    IngestionProperties.InfoDengue source = properties.infodengue();
    String uri =
        UriComponentsBuilder.fromUriString(source.baseUrl())
            .queryParam("geocode", source.geocode())
            .queryParam("disease", source.disease())
            .queryParam("format", "json")
            .queryParam("ew_start", source.weekStart())
            .queryParam("ew_end", source.weekEnd())
            .queryParam("ey_start", year)
            .queryParam("ey_end", year)
            .build(true)
            .toUriString();
    JsonNode body =
        http.get()
            .uri(uri)
            .header("Accept", "application/json")
            .header("User-Agent", USER_AGENT)
            .retrieve()
            .body(JsonNode.class);
    if (body == null) {
      throw new IllegalStateException("InfoDengue returned an empty body for " + year);
    }
    if (body.isObject()) {
      JsonNode message = body.has("error_message") ? body.get("error_message") : body;
      throw new IllegalStateException("InfoDengue error for " + year + ": " + message);
    }
    if (!body.isArray()) {
      throw new IllegalStateException("InfoDengue alertcity must be a JSON list");
    }
    List<JsonNode> items = new ArrayList<>();
    body.forEach(items::add);
    return items;
  }

  private List<Integer> years() {
    IngestionProperties.InfoDengue source = properties.infodengue();
    int current = Year.now().getValue();
    int start = source.yearStart() == null ? current - 1 : source.yearStart();
    int end = source.yearEnd() == null ? current : source.yearEnd();
    if (end < start) {
      throw new IllegalArgumentException("InfoDengue yearEnd is before yearStart");
    }
    List<Integer> years = new ArrayList<>();
    for (int year = start; year <= end; year++) {
      years.add(year);
    }
    return years;
  }

  private static Map<String, Object> payload(InfoDengueAlert row) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("geocode", row.geocode());
    payload.put("disease", row.disease());
    payload.put("epi_week_start", row.epiWeekStart().toString());
    payload.put("se", row.se());
    payload.put("cases_notified", row.casesNotified());
    payload.put("cases_estimated", row.casesEstimated());
    payload.put("alert_level", row.alertLevel());
    payload.put("population", row.population());
    payload.put("municipality_name", row.municipalityName());
    return payload;
  }
}
