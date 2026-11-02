"""Idempotent SQL upserts and the agent allowlist. Requires a migrated database."""

import os
from collections.abc import Iterator
from datetime import date

import httpx
import psycopg
import pytest
from psycopg.errors import CheckViolation

from ecoguard.adapters.config import Settings
from ecoguard.adapters.infodengue import InfoDengueSource
from ecoguard.adapters.open_meteo import OpenMeteoSource
from ecoguard.adapters.postgres import PostgresIngestionStore
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
    "nivel": 2,
    "id": 99,
    "versao_modelo": "test-model",
    "municipio_nome": "São Paulo",
    "Rt": 1.1,
    "pop": "1000",
}

_FORECAST: dict[str, object] = {
    "latitude": -23.51,
    "longitude": -46.61,
    "elevation": 760.0,
    "utc_offset_seconds": -10800,
    "timezone": "America/Sao_Paulo",
    "daily": {
        "time": ["2026-11-02"],
        "weather_code": [1],
        "temperature_2m_max": [28.0],
        "temperature_2m_min": [18.0],
        "precipitation_sum": [0.0],
        "precipitation_hours": [0.0],
        "wind_speed_10m_max": [10.0],
    },
}


def _database_url() -> str:
    url = os.environ.get("ECOGUARD_DATABASE_URL", "")
    if os.environ.get("CI") == "true" and not url:
        pytest.fail("CI must set ECOGUARD_DATABASE_URL so upserts run against Postgres")
    if not url:
        pytest.skip("ECOGUARD_DATABASE_URL is not set")
    return url


@pytest.fixture
def database_url() -> str:
    return _database_url()


@pytest.fixture
def connection(database_url: str) -> Iterator[psycopg.Connection[tuple[object, ...]]]:
    with psycopg.connect(database_url) as conn:
        conn.execute(
            """
            TRUNCATE
              raw.source_payloads,
              staging.infodengue_alerts,
              staging.open_meteo_daily,
              ops.job_runs
            """
        )
        conn.commit()
        yield conn


def test_extensions_roles_and_confirmed_weights(database_url: str) -> None:
    with psycopg.connect(database_url) as conn:
        extensions = {row[0] for row in conn.execute("SELECT extname FROM pg_extension")}
        assert {"postgis", "vector"} <= extensions
        roles = {
            row[0]
            for row in conn.execute(
                """
                SELECT rolname FROM pg_roles
                WHERE rolname IN ('ecoguard_ingest', 'ecoguard_api', 'ecoguard_agent')
                """
            )
        }
        assert roles == {"ecoguard_ingest", "ecoguard_api", "ecoguard_agent"}
        row = conn.execute(
            """
            SELECT weight_environmental, weight_health, weight_social, status
            FROM marts.risk_score_config
            WHERE config_key = 'default'
            """
        ).fetchone()
        assert row is not None
        assert float(row[0]) == pytest.approx(0.40)
        assert float(row[1]) == pytest.approx(0.35)
        assert float(row[2]) == pytest.approx(0.25)
        assert row[3] == "confirmed"
        assert conn.execute("SELECT count(*) FROM marts.districts").fetchone() == (0,)
        assert conn.execute("SELECT count(*) FROM marts.district_risk_scores").fetchone() == (0,)


def test_agent_role_reads_views_only(database_url: str) -> None:
    with psycopg.connect(database_url) as conn:
        conn.execute("SET ROLE ecoguard_agent")
        visible = conn.execute("SELECT config_key FROM agent.risk_score_config").fetchone()
        assert visible == ("default",)
        with pytest.raises(psycopg.errors.InsufficientPrivilege):
            conn.execute("SELECT 1 FROM marts.risk_score_config")
        conn.rollback()
        conn.execute("SET ROLE ecoguard_agent")
        with pytest.raises(psycopg.errors.InsufficientPrivilege):
            conn.execute("SELECT 1 FROM staging.infodengue_alerts")
        conn.rollback()


def test_districts_reject_the_2016_geojson_source(database_url: str) -> None:
    with psycopg.connect(database_url) as conn:
        with pytest.raises(CheckViolation):
            conn.execute(
                """
                INSERT INTO marts.districts (district_code, name, geom, source)
                VALUES (
                  '1',
                  'Example',
                  ST_GeomFromText('MULTIPOLYGON(((0 0, 1 0, 1 1, 0 0)))', 4326),
                  'codigourbano/distritos-sp'
                )
                """
            )
        conn.rollback()


def _week_response(request: httpx.Request) -> httpx.Response:
    del request
    return httpx.Response(200, json=[_WEEK])


def test_infodengue_upsert_is_idempotent(
    connection: psycopg.Connection[tuple[object, ...]],
) -> None:
    payloads = [[_WEEK], [{**_WEEK, "casos": 9}]]

    def handler(request: httpx.Request) -> httpx.Response:
        del request
        return httpx.Response(200, json=payloads.pop(0))

    settings = Settings(infodengue_year_start=2025, infodengue_year_end=2025)
    store = PostgresIngestionStore(connection)
    client = httpx.Client(transport=httpx.MockTransport(handler))
    run_ingestion(InfoDengueSource(client, settings), store)
    run_ingestion(InfoDengueSource(client, settings), store)
    row = connection.execute(
        """
        SELECT count(*), max(cases_notified)
        FROM staging.infodengue_alerts
        WHERE geocode = 3550308 AND disease = 'dengue'
        """
    ).fetchone()
    assert row == (1, 9)
    assert connection.execute(
        "SELECT count(*) FROM raw.source_payloads WHERE source = 'infodengue'"
    ).fetchone() == (1,)


def test_identical_raw_payload_does_not_churn(
    connection: psycopg.Connection[tuple[object, ...]],
) -> None:
    settings = Settings(infodengue_year_start=2025, infodengue_year_end=2025)
    store = PostgresIngestionStore(connection)
    first = run_ingestion(
        InfoDengueSource(httpx.Client(transport=httpx.MockTransport(_week_response)), settings),
        store,
    )
    second = run_ingestion(
        InfoDengueSource(httpx.Client(transport=httpx.MockTransport(_week_response)), settings),
        store,
    )
    assert first.raw_upserted == 1
    assert second.raw_upserted == 0


def test_open_meteo_upsert_is_idempotent(
    connection: psycopg.Connection[tuple[object, ...]],
) -> None:
    hotter = dict(_FORECAST)
    daily = hotter["daily"]
    assert isinstance(daily, dict)
    copied = dict(daily)
    copied["temperature_2m_max"] = [31.5]
    hotter["daily"] = copied
    payloads = [_FORECAST, hotter]

    def handler(request: httpx.Request) -> httpx.Response:
        del request
        return httpx.Response(200, json=payloads.pop(0))

    store = PostgresIngestionStore(connection)
    client = httpx.Client(transport=httpx.MockTransport(handler))
    run_ingestion(OpenMeteoSource(client, Settings()), store)
    run_ingestion(OpenMeteoSource(client, Settings()), store)
    row = connection.execute(
        """
        SELECT count(*), max(temperature_max_c)
        FROM staging.open_meteo_daily
        WHERE location_id = 'sp-city-reference' AND forecast_date = %s
        """,
        (date(2026, 11, 2),),
    ).fetchone()
    assert row is not None
    assert row[0] == 1
    assert isinstance(row[1], float)
    assert row[1] == pytest.approx(31.5)
