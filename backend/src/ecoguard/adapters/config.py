"""Process settings. Secrets come from the environment only."""

from datetime import date
from typing import Any

from pydantic import Field, field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_prefix="ECOGUARD_",
        extra="ignore",
        env_file=".env",
        env_file_encoding="utf-8",
    )

    environment: str = "local"
    api_title: str = "EcoGuard API"
    api_version: str = "0.1.0"
    rate_limit_requests: int = Field(default=120, ge=1)
    rate_limit_window_seconds: int = Field(default=60, ge=1)

    database_url: str | None = None
    http_timeout_seconds: float = 30.0

    infodengue_base_url: str = "https://info.dengue.mat.br/api/alertcity"
    infodengue_geocode: int = 3550308
    infodengue_disease: str = "dengue"
    infodengue_epi_week_start: int = 1
    infodengue_epi_week_end: int = 53
    infodengue_year_start: int | None = None
    infodengue_year_end: int | None = None

    open_meteo_base_url: str = "https://api.open-meteo.com/v1/forecast"
    open_meteo_api_key: str = ""
    open_meteo_latitude: float = -23.5505
    open_meteo_longitude: float = -46.6333
    open_meteo_timezone: str = "America/Sao_Paulo"
    open_meteo_forecast_days: int = Field(default=7, ge=1, le=16)
    open_meteo_location_id: str = "sp-city-reference"

    @field_validator("database_url", "infodengue_year_start", "infodengue_year_end", mode="before")
    @classmethod
    def blank_is_missing(cls, value: Any) -> Any:
        if value == "":
            return None
        return value

    def infodengue_years(self, *, today: date | None = None) -> range:
        """Inclusive year window. Default is the previous calendar year through this one."""

        current = (today or date.today()).year
        start = current - 1 if self.infodengue_year_start is None else self.infodengue_year_start
        end = current if self.infodengue_year_end is None else self.infodengue_year_end
        if end < start:
            raise ValueError("InfoDengue year_end is before year_start")
        return range(start, end + 1)
