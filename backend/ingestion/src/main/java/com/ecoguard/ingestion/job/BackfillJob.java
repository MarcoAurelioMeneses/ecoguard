package com.ecoguard.ingestion.job;

import com.ecoguard.ingestion.collector.IbgeCensusCollector;
import com.ecoguard.ingestion.collector.InpeFireCollector;
import com.ecoguard.ingestion.collector.OpenMeteoDistrictCollector;
import com.ecoguard.ingestion.score.ScoreJob;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Loads districts if needed, then the anonymous feeds, then the score. */
@Service
public class BackfillJob {
  private static final Logger log = LoggerFactory.getLogger(BackfillJob.class);

  private final IngestionService ingestion;
  private final OpenMeteoDistrictCollector weather;
  private final InpeFireCollector fires;
  private final IbgeCensusCollector census;
  private final ScoreJob scores;
  private final JobRuns runs;

  public BackfillJob(
      IngestionService ingestion,
      OpenMeteoDistrictCollector weather,
      InpeFireCollector fires,
      IbgeCensusCollector census,
      ScoreJob scores,
      JobRuns runs) {
    this.ingestion = ingestion;
    this.weather = weather;
    this.fires = fires;
    this.census = census;
    this.scores = scores;
    this.runs = runs;
  }

  public void run() {
    runs.record(
        "backfill",
        () -> {
          Instant now = Instant.now();
          log.info("backfill geosampa {}", ingestion.run("geosampa-districts"));
          log.info("backfill infodengue {}", ingestion.run("infodengue"));
          log.info(
              "backfill open-meteo-districts rows={}",
              runs.record("open-meteo-districts", () -> weather.extract(now)));
          log.info(
              "backfill inpe-fires rows={}", runs.record("inpe-fires", () -> fires.extract(now)));
          log.info(
              "backfill ibge-census rows={}",
              runs.record("ibge-census", () -> census.extract(now)));
          log.info("backfill risk-scores upserted={}", runs.record("risk-scores", scores::run));
          return 0;
        });
  }
}
