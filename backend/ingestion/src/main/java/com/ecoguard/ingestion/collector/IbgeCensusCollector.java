package com.ecoguard.ingestion.collector;

import com.ecoguard.domain.ingestion.Records.CensusDistrict;
import com.ecoguard.domain.ingestion.Records.Extraction;
import com.ecoguard.domain.port.Ports.SourcePort;
import com.ecoguard.ingestion.config.BackfillProperties;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Downloads the IBGE district zips. Name matching happens when the rows are stored. */
@Component
public class IbgeCensusCollector implements SourcePort {
  private final RestClient http;
  private final BackfillProperties backfill;

  public IbgeCensusCollector(RestClient http, BackfillProperties backfill) {
    this.http = http;
    this.backfill = backfill;
  }

  @Override
  public String sourceName() {
    return "ibge-census";
  }

  @Override
  public Extraction extract(Instant extractedAt) {
    Map<String, CensusDistrict> rows = new LinkedHashMap<>();
    read(backfill.ibgeBasicoUrl(), extractedAt, rows, true);
    read(backfill.ibgeDomicileUrl(), extractedAt, rows, false);
    return new Extraction(
        sourceName(),
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        List.copyOf(rows.values()));
  }

  private void read(
      String url, Instant extractedAt, Map<String, CensusDistrict> rows, boolean basic) {
    for (IbgeCensusParser.Row row : IbgeCensusParser.rows(HttpFetch.get(http, url, byte[].class))) {
      CensusDistrict current = rows.get(row.code());
      rows.put(
          row.code(),
          new CensusDistrict(
              row.code(),
              row.name() == null && current != null ? current.name() : row.name(),
              basic ? row.population() : current == null ? null : current.population(),
              basic ? row.areaKm2() : current == null ? null : current.areaKm2(),
              basic
                  ? row.occupiedHouseholds()
                  : current == null ? null : current.occupiedHouseholds(),
              basic
                  ? current == null ? null : current.sewageNetworkHouseholds()
                  : row.sewageNetworkHouseholds(),
              extractedAt,
              null));
    }
  }
}
