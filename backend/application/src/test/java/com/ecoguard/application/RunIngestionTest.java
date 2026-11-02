package com.ecoguard.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ecoguard.domain.ingestion.Records.Extraction;
import com.ecoguard.domain.ingestion.Records.InfoDengueAlert;
import com.ecoguard.domain.ingestion.Records.JobRun;
import com.ecoguard.domain.ingestion.Records.OpenMeteoDaily;
import com.ecoguard.domain.ingestion.Records.RawRecord;
import com.ecoguard.domain.port.Ports.IngestionStore;
import com.ecoguard.domain.port.Ports.SourcePort;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RunIngestionTest {
  @Test
  void writesRawThenStaging() {
    InfoDengueAlert row =
        new InfoDengueAlert(
            3550308,
            "dengue",
            LocalDate.parse("2025-10-12"),
            2025,
            42,
            202542,
            3,
            4.0,
            3,
            5,
            1.25,
            1,
            1.1,
            0.5,
            1000.0,
            "São Paulo",
            "test",
            99L,
            Instant.parse("2026-11-02T00:00:00Z"));
    RawRecord raw =
        new RawRecord("infodengue", row.externalKey(), Map.of("se", 202542), row.extractedAt());
    SourcePort source =
        new SourcePort() {
          @Override
          public String sourceName() {
            return "infodengue";
          }

          @Override
          public Extraction extract(Instant extractedAt) {
            return new Extraction("infodengue", List.of(raw), List.of(row), List.of());
          }
        };
    MemoryStore store = new MemoryStore();
    var result =
        new RunIngestion()
            .execute(source, store, Instant.parse("2026-11-02T00:00:00Z"), UUID.randomUUID());
    assertEquals(1, result.extracted());
    assertEquals(1, store.infoDengue.size());
    assertEquals("succeeded", store.runs.get(0).status());
  }

  private static final class MemoryStore implements IngestionStore {
    private final List<InfoDengueAlert> infoDengue = new ArrayList<>();
    private final List<JobRun> runs = new ArrayList<>();

    @Override
    public int upsertRaw(List<RawRecord> records) {
      return records.size();
    }

    @Override
    public int upsertInfoDengue(List<InfoDengueAlert> rows) {
      infoDengue.addAll(rows);
      return rows.size();
    }

    @Override
    public int upsertOpenMeteo(List<OpenMeteoDaily> rows) {
      return rows.size();
    }

    @Override
    public void recordRun(JobRun run) {
      runs.add(run);
    }
  }
}
