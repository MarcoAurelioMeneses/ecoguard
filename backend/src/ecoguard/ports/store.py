"""Persistence port. Postgres and the in-memory double both satisfy it."""

from datetime import datetime
from typing import Protocol

from ecoguard.domain.records import InfoDengueAlert, OpenMeteoDaily, RawRecord


class IngestionStore(Protocol):
    def upsert_raw(self, records: tuple[RawRecord, ...]) -> int:
        """Insert or update by (source, external_key)."""

    def upsert_infodengue(self, rows: tuple[InfoDengueAlert, ...]) -> int:
        """Insert or update by (geocode, disease, epi_week_start)."""

    def upsert_open_meteo(self, rows: tuple[OpenMeteoDaily, ...]) -> int:
        """Insert or update by (location_id, forecast_date)."""

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
        """Persist one ops.job_runs row."""

    def commit(self) -> None:
        """Commit the current unit of work."""

    def rollback(self) -> None:
        """Drop uncommitted writes from a failed attempt."""
