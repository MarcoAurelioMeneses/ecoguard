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

  public record District(
      String code,
      String name,
      String abbreviation,
      Integer subprefectureId,
      Double areaKm2,
      String wkt,
      int sourceSrid,
      Instant sourceUpdatedAt) {

    public static final String OFFICIAL_SOURCE = "geosampa:wfs:geoportal:distrito_municipal";
  }

  public record DistrictEnvironmentDay(
      String districtCode,
      LocalDate date,
      Double temperatureMaxC,
      Double precipitationMm,
      Double pm25Mean,
      Double pm10Mean,
      Double ozoneMean,
      double latitude,
      double longitude,
      Instant extractedAt) {

    public String externalKey() {
      return "open-meteo-district:" + districtCode + ":" + date;
    }
  }

  public record FireHotspot(double latitude, double longitude) {}

  public record FireDay(LocalDate date, List<FireHotspot> hotspots, Instant extractedAt) {}

  public record CensusDistrict(
      String ibgeCode,
      String name,
      Double population,
      Double areaKm2,
      Double occupiedHouseholds,
      Double sewageNetworkHouseholds,
      Instant extractedAt,
      String districtCode) {}

  public record Extraction(
      String source,
      List<RawRecord> records,
      List<InfoDengueAlert> infoDengue,
      List<OpenMeteoDaily> openMeteo,
      List<District> districts,
      List<DistrictEnvironmentDay> districtEnvironment,
      List<FireDay> fires,
      List<CensusDistrict> census) {}

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
