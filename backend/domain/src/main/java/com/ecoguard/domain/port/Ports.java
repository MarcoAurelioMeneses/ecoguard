package com.ecoguard.domain.port;

import com.ecoguard.domain.ingestion.Records;
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

    int upsertDistrictEnvironment(List<Records.DistrictEnvironmentDay> rows);

    int upsertFires(List<Records.FireDay> days);

    int upsertCensus(List<Records.CensusDistrict> rows);

    /** Normalized district name to GeoSampa code. Used to attach census rows. */
    Map<String, String> districtCodesByNormalizedName();

    void recordRun(JobRun run);
  }

  public interface DistrictSampler {
    List<SamplePoint> points();

    record SamplePoint(String code, double latitude, double longitude) {}
  }

  public interface SourcePort {
    String sourceName();

    Extraction extract(Instant extractedAt);
  }

  public interface AnalyticsSink {
    void capture(String event, Map<String, Object> properties);
  }
}
