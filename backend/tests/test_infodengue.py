"""InfoDengue parsing and idempotent upserts against a mocked HTTP API."""

from datetime import UTC, datetime

import httpx
import pytest

from ecoguard.adapters.config import Settings
from ecoguard.adapters.infodengue import InfoDengueSource, parse_alert
from ecoguard.adapters.memory import MemoryIngestionStore
from ecoguard.application.ingest import run_ingestion

_WEEK = {
    "data_iniSE": 1760227200000,
    "SE": 202542,
    "casos_est": 4.0,
    "casos_est_min": 3,
    "casos_est_max": 5,
    "casos": 3,
    "p_rt1": 0.5,
    "p_inc100k": 1.25,
    "nivel": 1,
    "id": 99,
    "versao_modelo": "test-model",
    "municipio_nome": "São Paulo",
    "Rt": 1.1,
    "pop": "1000",
    "casprov_est": None,
}


def _run(store: MemoryIngestionStore, client: httpx.Client, settings: Settings) -> None:
    run_ingestion(InfoDengueSource(client, settings), store)


def test_parse_alert_accepts_epoch_milliseconds_and_string_population() -> None:
    row = parse_alert(
        _WEEK,
        geocode=3550308,
        disease="dengue",
        extracted_at=datetime(2026, 11, 2, tzinfo=UTC),
    )
    assert row.epi_week_start.isoformat() == "2025-10-12"
    assert row.epi_year == 2025
    assert row.epi_week == 42
    assert row.population == 1000
    assert row.cases_notified == 3
    assert row.external_key == "infodengue:3550308:dengue:202542"


def test_job_requests_one_year_at_a_time_and_upserts_once() -> None:
    calls: list[httpx.Request] = []

    def handler(request: httpx.Request) -> httpx.Response:
        calls.append(request)
        return httpx.Response(200, json=[_WEEK])

    settings = Settings(
        infodengue_year_start=2025,
        infodengue_year_end=2025,
        infodengue_epi_week_start=42,
        infodengue_epi_week_end=42,
    )
    store = MemoryIngestionStore()
    client = httpx.Client(transport=httpx.MockTransport(handler))
    _run(store, client, settings)
    _run(store, client, settings)

    assert len(store.infodengue) == 1
    assert len(store.raw) == 1
    assert len(calls) == 2
    assert calls[0].url.params["geocode"] == "3550308"
    assert calls[0].url.params["disease"] == "dengue"
    assert calls[0].url.params["ey_start"] == "2025"
    assert store.runs[-1]["status"] == "succeeded"


def test_changed_week_updates_the_same_key() -> None:
    payloads = [[_WEEK], [{**_WEEK, "casos": 9, "casos_est": 9.0}]]

    def handler(request: httpx.Request) -> httpx.Response:
        del request
        return httpx.Response(200, json=payloads.pop(0))

    settings = Settings(infodengue_year_start=2025, infodengue_year_end=2025)
    store = MemoryIngestionStore()
    client = httpx.Client(transport=httpx.MockTransport(handler))
    _run(store, client, settings)
    _run(store, client, settings)
    stored = next(iter(store.infodengue.values()))
    assert stored.cases_notified == 9
    assert len(store.infodengue) == 1


def test_http_error_is_recorded_and_reraised() -> None:
    def handler(request: httpx.Request) -> httpx.Response:
        del request
        return httpx.Response(503, json={"error": True})

    settings = Settings(infodengue_year_start=2025, infodengue_year_end=2025)
    store = MemoryIngestionStore()
    client = httpx.Client(transport=httpx.MockTransport(handler))
    with pytest.raises(httpx.HTTPStatusError):
        _run(store, client, settings)
    assert store.infodengue == {}
    assert store.runs[-1]["status"] == "failed"


def test_api_error_object_is_rejected() -> None:
    def handler(request: httpx.Request) -> httpx.Response:
        del request
        return httpx.Response(200, json={"error_message": "epidate sent is empty."})

    settings = Settings(infodengue_year_start=2025, infodengue_year_end=2025)
    store = MemoryIngestionStore()
    client = httpx.Client(transport=httpx.MockTransport(handler))
    with pytest.raises(ValueError, match="InfoDengue error"):
        _run(store, client, settings)
