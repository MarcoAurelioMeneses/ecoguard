package com.ecoguard.ingestion.job;

import com.ecoguard.ingestion.score.ScoreJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Loads districts if needed, then the anonymous feeds, then the score. */
@Service
public class BackfillJob {
  private static final Logger log = LoggerFactory.getLogger(BackfillJob.class);

  private final IngestionService ingestion;
  private final ScoreJob scores;
  private final JobRuns runs;

  public BackfillJob(IngestionService ingestion, ScoreJob scores, JobRuns runs) {
    this.ingestion = ingestion;
    this.scores = scores;
    this.runs = runs;
  }

  public void run() {
    runs.record(
        "backfill",
        () -> {
          log.info("backfill geosampa {}", ingestion.run("geosampa-districts"));
          log.info("backfill infodengue {}", ingestion.run("infodengue"));
          log.info("backfill open-meteo-districts {}", ingestion.run("open-meteo-districts"));
          log.info("backfill inpe-fires {}", ingestion.run("inpe-fires"));
          log.info("backfill ibge-census {}", ingestion.run("ibge-census"));
          log.info("backfill risk-scores upserted={}", runs.record("risk-scores", scores::run));
          return 0;
        });
  }
}
