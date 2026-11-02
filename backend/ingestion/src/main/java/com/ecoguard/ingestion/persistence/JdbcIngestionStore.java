package com.ecoguard.ingestion.persistence;

import com.ecoguard.domain.ingestion.Records.InfoDengueAlert;
import com.ecoguard.domain.ingestion.Records.JobRun;
import com.ecoguard.domain.ingestion.Records.OpenMeteoDaily;
import com.ecoguard.domain.ingestion.Records.RawRecord;
import com.ecoguard.domain.port.Ports.IngestionStore;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
public class JdbcIngestionStore implements IngestionStore {
  private static final String RAW =
      """
      INSERT INTO raw.source_payloads (source, external_key, payload, payload_sha256, extracted_at)
      VALUES (:source, :external_key, CAST(:payload AS jsonb), :payload_sha256, :extracted_at)
      ON CONFLICT (source, external_key) DO UPDATE
      SET payload = EXCLUDED.payload,
          payload_sha256 = EXCLUDED.payload_sha256,
          extracted_at = EXCLUDED.extracted_at,
          loaded_at = now()
      WHERE raw.source_payloads.payload_sha256 IS DISTINCT FROM EXCLUDED.payload_sha256
      """;

  private static final String DENGUE =
      """
      INSERT INTO staging.infodengue_alerts (
        geocode, disease, epi_week_start, epi_year, epi_week, se,
        cases_notified, cases_estimated, cases_estimated_min, cases_estimated_max,
        incidence_per_100k, alert_level, rt, probability_rt_gt1, population,
        municipality_name, model_version, source_record_id, extracted_at)
      VALUES (
        :geocode, :disease, :epi_week_start, :epi_year, :epi_week, :se,
        :cases_notified, :cases_estimated, :cases_estimated_min, :cases_estimated_max,
        :incidence_per_100k, :alert_level, :rt, :probability_rt_gt1, :population,
        :municipality_name, :model_version, :source_record_id, :extracted_at)
      ON CONFLICT (geocode, disease, epi_week_start) DO UPDATE SET
        epi_year = EXCLUDED.epi_year,
        epi_week = EXCLUDED.epi_week,
        se = EXCLUDED.se,
        cases_notified = EXCLUDED.cases_notified,
        cases_estimated = EXCLUDED.cases_estimated,
        cases_estimated_min = EXCLUDED.cases_estimated_min,
        cases_estimated_max = EXCLUDED.cases_estimated_max,
        incidence_per_100k = EXCLUDED.incidence_per_100k,
        alert_level = EXCLUDED.alert_level,
        rt = EXCLUDED.rt,
        probability_rt_gt1 = EXCLUDED.probability_rt_gt1,
        population = EXCLUDED.population,
        municipality_name = EXCLUDED.municipality_name,
        model_version = EXCLUDED.model_version,
        source_record_id = EXCLUDED.source_record_id,
        extracted_at = EXCLUDED.extracted_at,
        loaded_at = now()
      """;

  private static final String WEATHER =
      """
      INSERT INTO staging.open_meteo_daily (
        location_id, forecast_date, requested_latitude, requested_longitude,
        grid_latitude, grid_longitude, elevation_m, timezone, utc_offset_seconds,
        weather_code, temperature_max_c, temperature_min_c, precipitation_sum_mm,
        precipitation_hours, wind_speed_max_kmh, extracted_at, geom)
      VALUES (
        :location_id, :forecast_date, :requested_latitude, :requested_longitude,
        :grid_latitude, :grid_longitude, :elevation_m, :timezone, :utc_offset_seconds,
        :weather_code, :temperature_max_c, :temperature_min_c, :precipitation_sum_mm,
        :precipitation_hours, :wind_speed_max_kmh, :extracted_at,
        ST_SetSRID(ST_MakePoint(:grid_longitude, :grid_latitude), 4326))
      ON CONFLICT (location_id, forecast_date) DO UPDATE SET
        requested_latitude = EXCLUDED.requested_latitude,
        requested_longitude = EXCLUDED.requested_longitude,
        grid_latitude = EXCLUDED.grid_latitude,
        grid_longitude = EXCLUDED.grid_longitude,
        elevation_m = EXCLUDED.elevation_m,
        timezone = EXCLUDED.timezone,
        utc_offset_seconds = EXCLUDED.utc_offset_seconds,
        weather_code = EXCLUDED.weather_code,
        temperature_max_c = EXCLUDED.temperature_max_c,
        temperature_min_c = EXCLUDED.temperature_min_c,
        precipitation_sum_mm = EXCLUDED.precipitation_sum_mm,
        precipitation_hours = EXCLUDED.precipitation_hours,
        wind_speed_max_kmh = EXCLUDED.wind_speed_max_kmh,
        extracted_at = EXCLUDED.extracted_at,
        geom = EXCLUDED.geom,
        loaded_at = now()
      """;

  private static final String RUN =
      """
      INSERT INTO ops.job_runs (
        run_id, source, status, started_at, finished_at,
        extracted_count, raw_upserted_count, staging_upserted_count, error)
      VALUES (
        :run_id, :source, :status, :started_at, :finished_at,
        :extracted_count, :raw_upserted_count, :staging_upserted_count, :error)
      """;

  private final JdbcClient jdbc;

  public JdbcIngestionStore(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public int upsertRaw(List<RawRecord> records) {
    int written = 0;
    for (RawRecord record : records) {
      written +=
          jdbc.sql(RAW)
              .param("source", record.source())
              .param("external_key", record.externalKey())
              .param("payload", PayloadHasher.json(record.payload()))
              .param("payload_sha256", PayloadHasher.sha256(record.payload()))
              .param("extracted_at", record.extractedAt())
              .update();
    }
    return written;
  }

  @Override
  public int upsertInfoDengue(List<InfoDengueAlert> rows) {
    int written = 0;
    for (InfoDengueAlert row : rows) {
      written +=
          jdbc.sql(DENGUE)
              .param("geocode", row.geocode())
              .param("disease", row.disease())
              .param("epi_week_start", row.epiWeekStart())
              .param("epi_year", row.epiYear())
              .param("epi_week", row.epiWeek())
              .param("se", row.se())
              .param("cases_notified", row.casesNotified())
              .param("cases_estimated", row.casesEstimated())
              .param("cases_estimated_min", row.casesEstimatedMin())
              .param("cases_estimated_max", row.casesEstimatedMax())
              .param("incidence_per_100k", row.incidencePer100k())
              .param("alert_level", row.alertLevel())
              .param("rt", row.rt())
              .param("probability_rt_gt1", row.probabilityRtGt1())
              .param("population", row.population())
              .param("municipality_name", row.municipalityName())
              .param("model_version", row.modelVersion())
              .param("source_record_id", row.sourceRecordId())
              .param("extracted_at", row.extractedAt())
              .update();
    }
    return written;
  }

  @Override
  public int upsertOpenMeteo(List<OpenMeteoDaily> rows) {
    int written = 0;
    for (OpenMeteoDaily row : rows) {
      written +=
          jdbc.sql(WEATHER)
              .param("location_id", row.locationId())
              .param("forecast_date", row.forecastDate())
              .param("requested_latitude", row.requestedLatitude())
              .param("requested_longitude", row.requestedLongitude())
              .param("grid_latitude", row.gridLatitude())
              .param("grid_longitude", row.gridLongitude())
              .param("elevation_m", row.elevationMeters())
              .param("timezone", row.timezone())
              .param("utc_offset_seconds", row.utcOffsetSeconds())
              .param("weather_code", row.weatherCode())
              .param("temperature_max_c", row.temperatureMaxC())
              .param("temperature_min_c", row.temperatureMinC())
              .param("precipitation_sum_mm", row.precipitationSumMm())
              .param("precipitation_hours", row.precipitationHours())
              .param("wind_speed_max_kmh", row.windSpeedMaxKmh())
              .param("extracted_at", row.extractedAt())
              .update();
    }
    return written;
  }

  @Override
  public void recordRun(JobRun run) {
    jdbc.sql(RUN)
        .param("run_id", run.runId())
        .param("source", run.source())
        .param("status", run.status())
        .param("started_at", run.startedAt())
        .param("finished_at", run.finishedAt())
        .param("extracted_count", run.extractedCount())
        .param("raw_upserted_count", run.rawUpsertedCount())
        .param("staging_upserted_count", run.stagingUpsertedCount())
        .param("error", run.error())
        .update();
  }
}
