package com.ecoguard.application;

import com.ecoguard.domain.ingestion.Records.Extraction;
import com.ecoguard.domain.ingestion.Records.JobResult;
import com.ecoguard.domain.ingestion.Records.JobRun;
import com.ecoguard.domain.port.Ports.IngestionStore;
import com.ecoguard.domain.port.Ports.SourcePort;
import java.time.Instant;
import java.util.UUID;

/** Extract, then raw, then staging. The caller owns the transaction. */
public final class RunIngestion {
  public JobResult execute(SourcePort source, IngestionStore store, Instant startedAt, UUID runId) {
    Extraction extraction = source.extract(startedAt);
    int raw = store.upsertRaw(extraction.records());
    int staging = store.upsertInfoDengue(extraction.infoDengue());
    staging += store.upsertOpenMeteo(extraction.openMeteo());
    staging += store.upsertDistricts(extraction.districts());
    int extracted = extraction.records().size() + extraction.districts().size();
    store.recordRun(
        new JobRun(
            runId,
            source.sourceName(),
            "succeeded",
            startedAt,
            Instant.now(),
            extracted,
            raw,
            staging,
            null));
    return new JobResult(source.sourceName(), runId, extracted, raw, staging);
  }
}
