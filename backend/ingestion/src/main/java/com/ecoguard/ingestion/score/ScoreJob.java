package com.ecoguard.ingestion.score;

import com.ecoguard.application.ComputeRiskScores;
import com.ecoguard.ingestion.config.IngestionProperties;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class ScoreJob {
  private final TransactionTemplate transaction;
  private final JdbcScoreStore store;
  private final IngestionProperties properties;
  private final ComputeRiskScores useCase = new ComputeRiskScores();

  public ScoreJob(
      PlatformTransactionManager transactions,
      JdbcScoreStore store,
      IngestionProperties properties) {
    this.transaction = new TransactionTemplate(transactions);
    this.store = store;
    this.properties = properties;
  }

  public int run() {
    Integer written =
        transaction.execute(
            status ->
                useCase.execute(
                    store,
                    store,
                    Instant.now(),
                    "default",
                    properties.openMeteo().locationId(),
                    properties.infodengue().geocode(),
                    properties.infodengue().disease()));
    return written == null ? 0 : written;
  }
}
