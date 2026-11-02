package com.ecoguard.domain.ingestion;

import com.ecoguard.domain.geo.Wgs84Point;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Records {
  private Records() {}

  public record RawRecord(
      String source, String externalKey, Map<String, Object> payload, Instant extractedAt) {}

  public record InfoDengueAlert(
      int geocode,
      String disease,
      LocalDate epiWeekStart,
      int epiYear,
      int epiWeek,
      int se,
      Integer casesNotified,
      Double casesEstimated,
      Integer casesEstimatedMin,
      Integer casesEstimatedMax,
      Double incidencePer100k,
      Integer alertLevel,
      Double rt,
      Double probabilityRtGt1,
      Double population,
      String municipalityName,
      String modelVersion,
      Long sourceRecordId,
      Instant extractedAt) {

    public String externalKey() {
      return "infodengue:" + geocode + ":" + disease + ":" + se;
    }
  }

  public record OpenMeteoDaily(
      String locationId,
      LocalDate forecastDate,
      double requestedLatitude,
      double requestedLongitude,
      double gridLatitude,
      double gridLongitude,
      Double elevationMeters,
      String timezone,
      int utcOffsetSeconds,
      Integer weatherCode,
      Double temperatureMaxC,
      Double temperatureMinC,
      Double precipitationSumMm,
      Double precipitationHours,
      Double windSpeedMaxKmh,
      Instant extractedAt) {

    public String externalKey() {
      return "open-meteo:" + locationId + ":" + forecastDate;
    }

    public Wgs84Point gridPoint() {
      return new Wgs84Point(gridLatitude, gridLongitude);
    }
  }

  public record Extraction(
      String source,
      List<RawRecord> records,
      List<InfoDengueAlert> infoDengue,
      List<OpenMeteoDaily> openMeteo) {}

  public record JobResult(
      String source, UUID runId, int extracted, int rawUpserted, int stagingUpserted) {}

  public record JobRun(
      UUID runId,
      String source,
      String status,
      Instant startedAt,
      Instant finishedAt,
      int extractedCount,
      int rawUpsertedCount,
      int stagingUpsertedCount,
      String error) {}
}
