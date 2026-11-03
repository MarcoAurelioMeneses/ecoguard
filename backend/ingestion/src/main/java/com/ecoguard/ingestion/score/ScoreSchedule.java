package com.ecoguard.ingestion.score;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "ecoguard.score", name = "cron")
public class ScoreSchedule {
  private final ScoreJob job;

  public ScoreSchedule(ScoreJob job) {
    this.job = job;
  }

  @Scheduled(cron = "${ecoguard.score.cron}", zone = "America/Sao_Paulo")
  public void nightly() {
    job.run();
  }
}
