"""Open-Meteo parsing and idempotent forecast upserts."""

from datetime import UTC, date, datetime

import httpx
import pytest

from ecoguard.adapters.config import Settings
from ecoguard.adapters.memory import MemoryIngestionStore
from ecoguard.adapters.open_meteo import OpenMeteoSource, parse_daily
from ecoguard.application.ingest import run_ingestion

_BODY: dict[str, object] = {
    "latitude": -23.51,
    "longitude": -46.61,
    "elevation": 760.0,
    "utc_offset_seconds": -10800,
    "timezone": "America/Sao_Paulo",
    "daily": {
        "time": ["2026-11-02", "2026-11-03"],
        "weather_code": [1, 61],
        "temperature_2m_max": [28.0, 22.5],
        "temperature_2m_min": [18.0, 17.0],
        "precipitation_sum": [0.0, 12.5],
        "precipitation_hours": [0.0, 4.0],
        "wind_speed_10m_max": [10.0, 18.0],
    },
}


def _with_max(values: list[float]) -> dict[str, object]:
    body = dict(_BODY)
    daily = body["daily"]
    assert isinstance(daily, dict)
    copied = dict(daily)
    copied["temperature_2m_max"] = values
    body["daily"] = copied
    return body


def test_parse_daily_keeps_requested_point_and_grid_point() -> None:
    rows = parse_daily(
        _BODY,
        location_id="sp-city-reference",
        requested_latitude=-23.5505,
        requested_longitude=-46.6333,
        extracted_at=datetime(2026, 11, 2, tzinfo=UTC),
    )
    assert len(rows) == 2
    assert rows[0].requested_latitude == -23.5505
    assert rows[0].grid_latitude == -23.51
    assert rows[0].temperature_max_c == 28.0
    assert rows[1].precipitation_sum_mm == 12.5
    assert rows[0].external_key == "open-meteo:sp-city-reference:2026-11-02"


def test_mismatched_daily_columns_fail() -> None:
    broken = dict(_BODY)
    daily = broken["daily"]
    assert isinstance(daily, dict)
    copied = dict(daily)
    copied["precipitation_sum"] = [0.0]
    broken["daily"] = copied
    with pytest.raises(ValueError, match="precipitation_sum length"):
        parse_daily(
            broken,
            location_id="sp-city-reference",
            requested_latitude=-23.5505,
            requested_longitude=-46.6333,
            extracted_at=datetime(2026, 11, 2, tzinfo=UTC),
        )


def test_job_is_idempotent_and_updates_a_forecast_day() -> None:
    payloads = [_BODY, _with_max([30.0, 22.5])]
    seen_keys: list[str | None] = []

    def handler(request: httpx.Request) -> httpx.Response:
        seen_keys.append(request.url.params.get("apikey"))
        assert request.url.params["timezone"] == "America/Sao_Paulo"
        assert request.url.params["forecast_days"] == "7"
        return httpx.Response(200, json=payloads.pop(0))

    settings = Settings(open_meteo_api_key="")
    store = MemoryIngestionStore()
    client = httpx.Client(transport=httpx.MockTransport(handler))
    run_ingestion(OpenMeteoSource(client, settings), store)
    run_ingestion(OpenMeteoSource(client, settings), store)
    updated = store.open_meteo[("sp-city-reference", date(2026, 11, 2))]
    assert len(store.open_meteo) == 2
    assert updated.temperature_max_c == 30.0
    assert seen_keys == [None, None]


def test_api_key_is_sent_only_when_configured() -> None:
    def handler(request: httpx.Request) -> httpx.Response:
        assert request.url.params["apikey"] == "test-key"
        return httpx.Response(200, json=_BODY)

    settings = Settings(open_meteo_api_key="test-key")
    client = httpx.Client(transport=httpx.MockTransport(handler))
    result = run_ingestion(OpenMeteoSource(client, settings), MemoryIngestionStore())
    assert result.extracted == 2
