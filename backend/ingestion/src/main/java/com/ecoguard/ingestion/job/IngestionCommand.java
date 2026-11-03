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
  private final BackfillJob backfill;
  private final com.ecoguard.ingestion.collector.OpenMeteoDistrictCollector districtWeather;
  private final com.ecoguard.ingestion.collector.InpeFireCollector fires;
  private final com.ecoguard.ingestion.collector.IbgeCensusCollector census;

  public IngestionCommand(
      IngestionService service,
      IngestionProperties properties,
      ScoreJob scores,
      BackfillJob backfill,
      com.ecoguard.ingestion.collector.OpenMeteoDistrictCollector districtWeather,
      com.ecoguard.ingestion.collector.InpeFireCollector fires,
      com.ecoguard.ingestion.collector.IbgeCensusCollector census) {
    this.service = service;
    this.properties = properties;
    this.scores = scores;
    this.backfill = backfill;
    this.districtWeather = districtWeather;
    this.fires = fires;
    this.census = census;
  }

  @Override
  public void run(String... args) {
    if ("backfill".equals(properties.job())) {
      backfill.run();
      return;
    }
    if ("open-meteo-districts".equals(properties.job())) {
      log.info("open-meteo-districts rows={}", districtWeather.extract(java.time.Instant.now()));
      return;
    }
    if ("inpe-fires".equals(properties.job())) {
      log.info("inpe-fires rows={}", fires.extract(java.time.Instant.now()));
      return;
    }
    if ("ibge-census".equals(properties.job())) {
      log.info("ibge-census rows={}", census.extract(java.time.Instant.now()));
      return;
    }
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
