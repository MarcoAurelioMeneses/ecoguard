"""Postgres upserts for raw, staging, and ops. Natural keys match the migrations."""

from datetime import datetime
from typing import Any

import psycopg
from psycopg.types.json import Jsonb

from ecoguard.adapters.hashing import payload_sha256
from ecoguard.domain.records import InfoDengueAlert, OpenMeteoDaily, RawRecord

_RAW_UPSERT = """
INSERT INTO raw.source_payloads (
  source, external_key, payload, payload_sha256, extracted_at
)
VALUES (%(source)s, %(external_key)s, %(payload)s, %(payload_sha256)s, %(extracted_at)s)
ON CONFLICT (source, external_key) DO UPDATE
SET payload = EXCLUDED.payload,
    payload_sha256 = EXCLUDED.payload_sha256,
    extracted_at = EXCLUDED.extracted_at,
    loaded_at = now()
WHERE raw.source_payloads.payload_sha256 IS DISTINCT FROM EXCLUDED.payload_sha256
"""

_INFODENGUE_UPSERT = """
INSERT INTO staging.infodengue_alerts (
  geocode, disease, epi_week_start, epi_year, epi_week, se,
  cases_notified, cases_estimated, cases_estimated_min, cases_estimated_max,
  incidence_per_100k, alert_level, rt, probability_rt_gt1, population,
  municipality_name, model_version, source_record_id, extracted_at
)
VALUES (
  %(geocode)s, %(disease)s, %(epi_week_start)s, %(epi_year)s, %(epi_week)s, %(se)s,
  %(cases_notified)s, %(cases_estimated)s, %(cases_estimated_min)s, %(cases_estimated_max)s,
  %(incidence_per_100k)s, %(alert_level)s, %(rt)s, %(probability_rt_gt1)s, %(population)s,
  %(municipality_name)s, %(model_version)s, %(source_record_id)s, %(extracted_at)s
)
ON CONFLICT (geocode, disease, epi_week_start) DO UPDATE
SET epi_year = EXCLUDED.epi_year,
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
"""

_OPEN_METEO_UPSERT = """
INSERT INTO staging.open_meteo_daily (
  location_id, forecast_date, requested_latitude, requested_longitude,
  grid_latitude, grid_longitude, elevation_m, timezone, utc_offset_seconds,
  weather_code, temperature_max_c, temperature_min_c, precipitation_sum_mm,
  precipitation_hours, wind_speed_max_kmh, extracted_at
)
VALUES (
  %(location_id)s, %(forecast_date)s, %(requested_latitude)s, %(requested_longitude)s,
  %(grid_latitude)s, %(grid_longitude)s, %(elevation_m)s, %(timezone)s, %(utc_offset_seconds)s,
  %(weather_code)s, %(temperature_max_c)s, %(temperature_min_c)s, %(precipitation_sum_mm)s,
  %(precipitation_hours)s, %(wind_speed_max_kmh)s, %(extracted_at)s
)
ON CONFLICT (location_id, forecast_date) DO UPDATE
SET requested_latitude = EXCLUDED.requested_latitude,
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
    loaded_at = now()
"""

_INSERT_RUN = """
INSERT INTO ops.job_runs (
  run_id, source, status, started_at, finished_at,
  extracted_count, raw_upserted_count, staging_upserted_count, error
)
VALUES (
  %(run_id)s, %(source)s, %(status)s, %(started_at)s, %(finished_at)s,
  %(extracted_count)s, %(raw_upserted_count)s, %(staging_upserted_count)s, %(error)s
)
"""


class PostgresIngestionStore:
    def __init__(self, connection: psycopg.Connection[tuple[Any, ...]]) -> None:
        self._connection = connection

    @classmethod
    def connect(cls, database_url: str) -> "PostgresIngestionStore":
        connection: psycopg.Connection[tuple[Any, ...]] = psycopg.connect(database_url)
        return cls(connection)

    def close(self) -> None:
        self._connection.close()

    def upsert_raw(self, records: tuple[RawRecord, ...]) -> int:
        written = 0
        with self._connection.cursor() as cursor:
            for record in records:
                cursor.execute(
                    _RAW_UPSERT,
                    {
                        "source": record.source,
                        "external_key": record.external_key,
                        "payload": Jsonb(record.payload),
                        "payload_sha256": payload_sha256(record.payload),
                        "extracted_at": record.extracted_at,
                    },
                )
                written += cursor.rowcount
        return written

    def upsert_infodengue(self, rows: tuple[InfoDengueAlert, ...]) -> int:
        written = 0
        with self._connection.cursor() as cursor:
            for row in rows:
                cursor.execute(_INFODENGUE_UPSERT, _infodengue_params(row))
                written += cursor.rowcount
        return written

    def upsert_open_meteo(self, rows: tuple[OpenMeteoDaily, ...]) -> int:
        written = 0
        with self._connection.cursor() as cursor:
            for row in rows:
                cursor.execute(_OPEN_METEO_UPSERT, _open_meteo_params(row))
                written += cursor.rowcount
        return written

    def record_run(
        self,
        *,
        run_id: str,
        source: str,
        status: str,
        started_at: datetime,
        finished_at: datetime,
        extracted_count: int,
        raw_upserted_count: int,
        staging_upserted_count: int,
        error: str | None,
    ) -> None:
        with self._connection.cursor() as cursor:
            cursor.execute(
                _INSERT_RUN,
                {
                    "run_id": run_id,
                    "source": source,
                    "status": status,
                    "started_at": started_at,
                    "finished_at": finished_at,
                    "extracted_count": extracted_count,
                    "raw_upserted_count": raw_upserted_count,
                    "staging_upserted_count": staging_upserted_count,
                    "error": error,
                },
            )

    def commit(self) -> None:
        self._connection.commit()

    def rollback(self) -> None:
        self._connection.rollback()


def _infodengue_params(row: InfoDengueAlert) -> dict[str, object]:
    return {
        "geocode": row.geocode,
        "disease": row.disease,
        "epi_week_start": row.epi_week_start,
        "epi_year": row.epi_year,
        "epi_week": row.epi_week,
        "se": row.se,
        "cases_notified": row.cases_notified,
        "cases_estimated": row.cases_estimated,
        "cases_estimated_min": row.cases_estimated_min,
        "cases_estimated_max": row.cases_estimated_max,
        "incidence_per_100k": row.incidence_per_100k,
        "alert_level": row.alert_level,
        "rt": row.rt,
        "probability_rt_gt1": row.probability_rt_gt1,
        "population": row.population,
        "municipality_name": row.municipality_name,
        "model_version": row.model_version,
        "source_record_id": row.source_record_id,
        "extracted_at": row.extracted_at,
    }


def _open_meteo_params(row: OpenMeteoDaily) -> dict[str, object]:
    return {
        "location_id": row.location_id,
        "forecast_date": row.forecast_date,
        "requested_latitude": row.requested_latitude,
        "requested_longitude": row.requested_longitude,
        "grid_latitude": row.grid_latitude,
        "grid_longitude": row.grid_longitude,
        "elevation_m": row.elevation_m,
        "timezone": row.timezone,
        "utc_offset_seconds": row.utc_offset_seconds,
        "weather_code": row.weather_code,
        "temperature_max_c": row.temperature_max_c,
        "temperature_min_c": row.temperature_min_c,
        "precipitation_sum_mm": row.precipitation_sum_mm,
        "precipitation_hours": row.precipitation_hours,
        "wind_speed_max_kmh": row.wind_speed_max_kmh,
        "extracted_at": row.extracted_at,
    }
