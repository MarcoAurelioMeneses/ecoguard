package com.ecoguard.domain.port;

import com.ecoguard.domain.ingestion.Records.District;
import com.ecoguard.domain.ingestion.Records.Extraction;
import com.ecoguard.domain.ingestion.Records.InfoDengueAlert;
import com.ecoguard.domain.ingestion.Records.JobRun;
import com.ecoguard.domain.ingestion.Records.OpenMeteoDaily;
import com.ecoguard.domain.ingestion.Records.RawRecord;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class Ports {
  private Ports() {}

  public interface IngestionStore {
    int upsertRaw(List<RawRecord> records);

    int upsertInfoDengue(List<InfoDengueAlert> rows);

    int upsertOpenMeteo(List<OpenMeteoDaily> rows);

    int upsertDistricts(List<District> districts);

    void recordRun(JobRun run);
  }

  public interface SourcePort {
    String sourceName();

    Extraction extract(Instant extractedAt);
  }

  public interface AnalyticsSink {
    void capture(String event, Map<String, Object> properties);
  }
}
