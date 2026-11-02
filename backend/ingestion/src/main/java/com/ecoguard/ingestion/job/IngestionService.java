package com.ecoguard.ingestion.job;

import com.ecoguard.application.RunIngestion;
import com.ecoguard.domain.ingestion.Records.JobResult;
import com.ecoguard.domain.ingestion.Records.JobRun;
import com.ecoguard.domain.port.Ports.SourcePort;
import com.ecoguard.ingestion.persistence.JdbcIngestionStore;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class IngestionService {
  private final TransactionTemplate transaction;
  private final RunIngestion useCase;
  private final JdbcIngestionStore store;
  private final Map<String, SourcePort> sources;

  public IngestionService(
      PlatformTransactionManager transactions, JdbcIngestionStore store, List<SourcePort> sources) {
    this.transaction = new TransactionTemplate(transactions);
    this.useCase = new RunIngestion();
    this.store = store;
    this.sources =
        sources.stream().collect(Collectors.toMap(SourcePort::sourceName, Function.identity()));
  }

  public JobResult run(String job) {
    SourcePort source = sources.get(job);
    if (source == null) {
      throw new IllegalArgumentException("unknown source: " + job);
    }
    UUID runId = UUID.randomUUID();
    Instant started = Instant.now();
    try {
      return transaction.execute(status -> useCase.execute(source, store, started, runId));
    } catch (RuntimeException ex) {
      transaction.executeWithoutResult(
          status ->
              store.recordRun(
                  new JobRun(
                      runId,
                      source.sourceName(),
                      "failed",
                      started,
                      Instant.now(),
                      0,
                      0,
                      0,
                      ex.getMessage())));
      throw ex;
    }
  }
}
