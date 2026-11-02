"""Open-Meteo forecast adapter for one São Paulo reference coordinate.

The free API is non-commercial and CC BY 4.0. The grid point can differ from
the request. The upsert key is our location id plus the forecast date.
"""

from datetime import date, datetime

import httpx

from ecoguard.adapters.config import Settings
from ecoguard.adapters.parsing import as_object, optional_float, required_float, required_int
from ecoguard.domain.records import Extraction, OpenMeteoDaily, RawRecord

USER_AGENT = "EcoGuard-ingestion/0.1"
_DAILY_FIELDS = (
    "weather_code",
    "temperature_2m_max",
    "temperature_2m_min",
    "precipitation_sum",
    "precipitation_hours",
    "wind_speed_10m_max",
)


class OpenMeteoSource:
    def __init__(self, client: httpx.Client, settings: Settings) -> None:
        self._client = client
        self._settings = settings

    @property
    def source_name(self) -> str:
        return "open-meteo"

    def extract(self, *, extracted_at: datetime) -> Extraction:
        body = self._fetch()
        rows = parse_daily(
            body,
            location_id=self._settings.open_meteo_location_id,
            requested_latitude=self._settings.open_meteo_latitude,
            requested_longitude=self._settings.open_meteo_longitude,
            extracted_at=extracted_at,
        )
        records = tuple(
            RawRecord(
                source=self.source_name,
                external_key=row.external_key,
                payload=_raw_payload(row),
                extracted_at=extracted_at,
            )
            for row in rows
        )
        return Extraction(source=self.source_name, records=records, open_meteo=rows)

    def _fetch(self) -> dict[str, object]:
        params: dict[str, str] = {
            "latitude": str(self._settings.open_meteo_latitude),
            "longitude": str(self._settings.open_meteo_longitude),
            "daily": ",".join(_DAILY_FIELDS),
            "timezone": self._settings.open_meteo_timezone,
            "forecast_days": str(self._settings.open_meteo_forecast_days),
        }
        api_key = self._settings.open_meteo_api_key.strip()
        if api_key:
            params["apikey"] = api_key
        response = self._client.get(
            self._settings.open_meteo_base_url,
            params=params,
            headers={"Accept": "application/json", "User-Agent": USER_AGENT},
        )
        response.raise_for_status()
        body: object = response.json()
        document = as_object(body, label="Open-Meteo forecast")
        if document.get("error") is True:
            raise ValueError(f"Open-Meteo error: {document.get('reason')}")
        return document


def parse_daily(
    body: dict[str, object],
    *,
    location_id: str,
    requested_latitude: float,
    requested_longitude: float,
    extracted_at: datetime,
) -> tuple[OpenMeteoDaily, ...]:
    daily = as_object(body.get("daily"), label="Open-Meteo daily")
    times = _string_list(daily.get("time"), label="daily.time")
    columns = {
        name: _optional_number_list(daily.get(name), label=name, size=len(times))
        for name in _DAILY_FIELDS
    }
    timezone = body.get("timezone")
    if not isinstance(timezone, str) or not timezone:
        raise ValueError("Open-Meteo timezone is required")
    rows: list[OpenMeteoDaily] = []
    for index, time_value in enumerate(times):
        rows.append(
            OpenMeteoDaily(
                location_id=location_id,
                forecast_date=date.fromisoformat(time_value),
                requested_latitude=requested_latitude,
                requested_longitude=requested_longitude,
                grid_latitude=required_float(body.get("latitude"), label="latitude"),
                grid_longitude=required_float(body.get("longitude"), label="longitude"),
                elevation_m=optional_float(body.get("elevation")),
                timezone=timezone,
                utc_offset_seconds=required_int(
                    body.get("utc_offset_seconds"),
                    label="utc_offset_seconds",
                ),
                weather_code=_int_at(columns["weather_code"], index),
                temperature_max_c=columns["temperature_2m_max"][index],
                temperature_min_c=columns["temperature_2m_min"][index],
                precipitation_sum_mm=columns["precipitation_sum"][index],
                precipitation_hours=columns["precipitation_hours"][index],
                wind_speed_max_kmh=columns["wind_speed_10m_max"][index],
                extracted_at=extracted_at,
            )
        )
    return tuple(rows)


def _string_list(value: object, *, label: str) -> list[str]:
    if not isinstance(value, list) or not value:
        raise ValueError(f"{label} must be a non-empty list")
    result: list[str] = []
    for item in value:
        if not isinstance(item, str):
            raise ValueError(f"{label} entries must be strings")
        result.append(item)
    return result


def _optional_number_list(value: object, *, label: str, size: int) -> list[float | None]:
    if not isinstance(value, list):
        raise ValueError(f"{label} must be a list")
    if len(value) != size:
        raise ValueError(f"{label} length {len(value)} does not match time length {size}")
    return [optional_float(item) for item in value]


def _int_at(values: list[float | None], index: int) -> int | None:
    value = values[index]
    if value is None:
        return None
    if not float(value).is_integer():
        raise ValueError(f"weather_code must be an integer, got {value}")
    return int(value)


def _raw_payload(row: OpenMeteoDaily) -> dict[str, object]:
    return {
        "location_id": row.location_id,
        "forecast_date": row.forecast_date.isoformat(),
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
    }
