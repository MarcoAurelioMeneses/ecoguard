"""In-memory store for tests and `--dry-run`. Same natural keys as Postgres."""

from dataclasses import dataclass
from datetime import datetime

from ecoguard.adapters.hashing import payload_sha256
from ecoguard.domain.records import InfoDengueAlert, OpenMeteoDaily, RawRecord


@dataclass
class _RawState:
    payload_sha256: str
    record: RawRecord


class MemoryIngestionStore:
    def __init__(self) -> None:
        self.raw: dict[tuple[str, str], _RawState] = {}
        self.infodengue: dict[tuple[int, str, object], InfoDengueAlert] = {}
        self.open_meteo: dict[tuple[str, object], OpenMeteoDaily] = {}
        self.runs: list[dict[str, object]] = []

    def upsert_raw(self, records: tuple[RawRecord, ...]) -> int:
        written = 0
        for record in records:
            digest = payload_sha256(record.payload)
            key = (record.source, record.external_key)
            current = self.raw.get(key)
            if current is not None and current.payload_sha256 == digest:
                continue
            self.raw[key] = _RawState(payload_sha256=digest, record=record)
            written += 1
        return written

    def upsert_infodengue(self, rows: tuple[InfoDengueAlert, ...]) -> int:
        for row in rows:
            self.infodengue[(row.geocode, row.disease, row.epi_week_start)] = row
        return len(rows)

    def upsert_open_meteo(self, rows: tuple[OpenMeteoDaily, ...]) -> int:
        for row in rows:
            self.open_meteo[(row.location_id, row.forecast_date)] = row
        return len(rows)

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
        self.runs.append(
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
            }
        )

    def commit(self) -> None:
        return None

    def rollback(self) -> None:
        return None
