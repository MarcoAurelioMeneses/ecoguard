package com.ecoguard.ingestion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ecoguard")
public record IngestionProperties(String job, InfoDengue infodengue, OpenMeteo openMeteo) {
  public record InfoDengue(
      String baseUrl,
      int geocode,
      String disease,
      int weekStart,
      int weekEnd,
      Integer yearStart,
      Integer yearEnd) {}

  public record OpenMeteo(
      String baseUrl,
      String apiKey,
      double latitude,
      double longitude,
      String timezone,
      int forecastDays,
      String locationId) {}
}
