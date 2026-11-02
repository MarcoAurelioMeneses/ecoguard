"""Source records and the raw/staging batch. No I/O."""

from dataclasses import dataclass
from datetime import date, datetime


@dataclass(frozen=True)
class RawRecord:
    source: str
    external_key: str
    payload: dict[str, object]
    extracted_at: datetime


@dataclass(frozen=True)
class InfoDengueAlert:
    geocode: int
    disease: str
    epi_week_start: date
    epi_year: int
    epi_week: int
    se: int
    cases_notified: int | None
    cases_estimated: float | None
    cases_estimated_min: int | None
    cases_estimated_max: int | None
    incidence_per_100k: float | None
    alert_level: int | None
    rt: float | None
    probability_rt_gt1: float | None
    population: float | None
    municipality_name: str | None
    model_version: str | None
    source_record_id: int | None
    extracted_at: datetime

    @property
    def external_key(self) -> str:
        return f"infodengue:{self.geocode}:{self.disease}:{self.se}"


@dataclass(frozen=True)
class OpenMeteoDaily:
    location_id: str
    forecast_date: date
    requested_latitude: float
    requested_longitude: float
    grid_latitude: float
    grid_longitude: float
    elevation_m: float | None
    timezone: str
    utc_offset_seconds: int
    weather_code: int | None
    temperature_max_c: float | None
    temperature_min_c: float | None
    precipitation_sum_mm: float | None
    precipitation_hours: float | None
    wind_speed_max_kmh: float | None
    extracted_at: datetime

    @property
    def external_key(self) -> str:
        return f"open-meteo:{self.location_id}:{self.forecast_date.isoformat()}"


@dataclass(frozen=True)
class Extraction:
    """What a source adapter returns. The use case writes it. The adapter does not."""

    source: str
    records: tuple[RawRecord, ...]
    infodengue: tuple[InfoDengueAlert, ...] = ()
    open_meteo: tuple[OpenMeteoDaily, ...] = ()


@dataclass(frozen=True)
class JobResult:
    source: str
    run_id: str
    extracted: int
    raw_upserted: int
    staging_upserted: int
