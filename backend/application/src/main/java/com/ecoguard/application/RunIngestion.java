package com.ecoguard.application;

import com.ecoguard.domain.ingestion.CensusNames;
import com.ecoguard.domain.ingestion.Records.CensusDistrict;
import com.ecoguard.domain.ingestion.Records.Extraction;
import com.ecoguard.domain.ingestion.Records.JobResult;
import com.ecoguard.domain.ingestion.Records.JobRun;
import com.ecoguard.domain.port.Ports.IngestionStore;
import com.ecoguard.domain.port.Ports.SourcePort;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Persist an extraction. HTTP belongs to the caller, outside the transaction. */
public final class RunIngestion {
  public JobResult execute(SourcePort source, IngestionStore store, Instant startedAt, UUID runId) {
    return persist(source.extract(startedAt), store, startedAt, runId);
  }

  public JobResult persist(
      Extraction extraction, IngestionStore store, Instant startedAt, UUID runId) {
    int raw = store.upsertRaw(extraction.records());
    int staging = store.upsertInfoDengue(extraction.infoDengue());
    staging += store.upsertOpenMeteo(extraction.openMeteo());
    staging += store.upsertDistricts(extraction.districts());
    staging += store.upsertDistrictEnvironment(extraction.districtEnvironment());
    staging += store.upsertFires(extraction.fires());
    staging += store.upsertCensus(matchCensus(extraction.census(), store));
    int extracted = extraction.records().size() + extraction.districts().size();
    store.recordRun(
        new JobRun(
            runId,
            extraction.source(),
            "succeeded",
            startedAt,
            Instant.now(),
            extracted,
            raw,
            staging,
            null));
    return new JobResult(extraction.source(), runId, extracted, raw, staging);
  }

  private static List<CensusDistrict> matchCensus(List<CensusDistrict> rows, IngestionStore store) {
    if (rows.isEmpty()) {
      return List.of();
    }
    Map<String, String> codes = store.districtCodesByNormalizedName();
    List<CensusDistrict> matched = new ArrayList<>();
    for (CensusDistrict row : rows) {
      String code = codes.get(CensusNames.normalize(row.name()));
      if (code != null && row.population() != null && row.areaKm2() != null) {
        matched.add(
            new CensusDistrict(
                row.ibgeCode(),
                row.name(),
                row.population(),
                row.areaKm2(),
                row.occupiedHouseholds(),
                row.sewageNetworkHouseholds(),
                row.extractedAt(),
                code));
      }
    }
    if (matched.size() != 96) {
      throw new IllegalStateException(
          "IBGE district match wrote " + matched.size() + " rows, expected 96");
    }
    return matched;
  }
}
