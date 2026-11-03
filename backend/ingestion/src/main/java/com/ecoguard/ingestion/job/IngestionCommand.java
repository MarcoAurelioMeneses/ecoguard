package com.ecoguard.ingestion.job;

import com.ecoguard.domain.ingestion.Records.JobResult;
import com.ecoguard.ingestion.config.IngestionProperties;
import com.ecoguard.ingestion.score.ScoreJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnExpression("!'${ecoguard.job:}'.isBlank()")
public class IngestionCommand implements CommandLineRunner {
  private static final Logger log = LoggerFactory.getLogger(IngestionCommand.class);

  private final IngestionService service;
  private final IngestionProperties properties;
  private final ScoreJob scores;

  public IngestionCommand(
      IngestionService service, IngestionProperties properties, ScoreJob scores) {
    this.service = service;
    this.properties = properties;
    this.scores = scores;
  }

  @Override
  public void run(String... args) {
    if ("risk-scores".equals(properties.job())) {
      int written = scores.run();
      log.info("risk-scores upserted={}", written);
      return;
    }
    JobResult result = service.run(properties.job());
    log.info(
        "{} extracted={} rawUpserted={} stagingUpserted={} runId={}",
        result.source(),
        result.extracted(),
        result.rawUpserted(),
        result.stagingUpserted(),
        result.runId());
  }
}
