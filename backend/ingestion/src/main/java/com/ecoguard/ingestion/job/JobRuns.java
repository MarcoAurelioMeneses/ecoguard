package com.ecoguard.ingestion.job;

import com.ecoguard.domain.ingestion.Records.JobRun;
import com.ecoguard.ingestion.persistence.JdbcIngestionStore;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Writes one ops.job_runs row after the work transaction has committed or rolled back. */
@Service
public class JobRuns {
  private final TransactionTemplate transaction;
  private final JdbcIngestionStore store;

  public JobRuns(PlatformTransactionManager transactions, JdbcIngestionStore store) {
    this.transaction = new TransactionTemplate(transactions);
    this.store = store;
  }

  public int record(String source, Supplier<Integer> work) {
    UUID runId = UUID.randomUUID();
    Instant started = Instant.now();
    try {
      int rows = work.get();
      write(runId, source, "succeeded", started, rows, null);
      return rows;
    } catch (RuntimeException ex) {
      write(runId, source, "failed", started, 0, ex.getMessage());
      throw ex;
    }
  }

  private void write(
      UUID runId, String source, String status, Instant started, int rows, String error) {
    transaction.executeWithoutResult(
        ignored ->
            store.recordRun(
                new JobRun(
                    runId, source, status, started, Instant.now(), rows, rows, rows, error)));
  }
}
