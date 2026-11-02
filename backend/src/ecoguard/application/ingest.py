"""Run one source: extract, upsert raw, upsert staging, record the run."""

import uuid
from datetime import UTC, datetime

from ecoguard.domain.records import JobResult
from ecoguard.ports.sources import SourcePort
from ecoguard.ports.store import IngestionStore


def run_ingestion(source: SourcePort, store: IngestionStore) -> JobResult:
    """Commit the batch and the run row together, or record a failure after rollback."""

    started = datetime.now(UTC)
    run_id = str(uuid.uuid4())
    try:
        extraction = source.extract(extracted_at=started)
        raw_count = store.upsert_raw(extraction.records)
        staging_count = store.upsert_infodengue(extraction.infodengue)
        staging_count += store.upsert_open_meteo(extraction.open_meteo)
    except Exception as exc:
        store.rollback()
        store.record_run(
            run_id=run_id,
            source=source.source_name,
            status="failed",
            started_at=started,
            finished_at=datetime.now(UTC),
            extracted_count=0,
            raw_upserted_count=0,
            staging_upserted_count=0,
            error=str(exc),
        )
        store.commit()
        raise
    store.record_run(
        run_id=run_id,
        source=source.source_name,
        status="succeeded",
        started_at=started,
        finished_at=datetime.now(UTC),
        extracted_count=len(extraction.records),
        raw_upserted_count=raw_count,
        staging_upserted_count=staging_count,
        error=None,
    )
    store.commit()
    return JobResult(
        source=source.source_name,
        run_id=run_id,
        extracted=len(extraction.records),
        raw_upserted=raw_count,
        staging_upserted=staging_count,
    )
