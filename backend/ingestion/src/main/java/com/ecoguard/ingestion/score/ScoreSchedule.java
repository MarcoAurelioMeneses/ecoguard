package com.ecoguard.ingestion.score;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "ecoguard.schedule", name = "cron")
public class ScoreSchedule {
  private final com.ecoguard.ingestion.job.BackfillJob backfill;

  public ScoreSchedule(com.ecoguard.ingestion.job.BackfillJob backfill) {
    this.backfill = backfill;
  }

  /** Runs the same collectors as a one-shot backfill, for the configured day window. */
  @Scheduled(cron = "${ecoguard.schedule.cron}", zone = "America/Sao_Paulo")
  public void nightly() {
    backfill.run();
  }
}
