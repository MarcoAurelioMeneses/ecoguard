"""InfoDengue HTTP adapter.

Municipal series only. `data_iniSE` is epoch milliseconds. The geocode is not
in the JSON object, so the requested geocode is stored on the row.
"""

from datetime import UTC, date, datetime

import httpx

from ecoguard.adapters.config import Settings
from ecoguard.adapters.parsing import (
    as_object_list,
    optional_float,
    optional_int,
    optional_str,
    required_int,
)
from ecoguard.domain.records import Extraction, InfoDengueAlert, RawRecord

_DISEASES = frozenset({"dengue", "chikungunya", "zika"})
USER_AGENT = "EcoGuard-ingestion/0.1"


class InfoDengueSource:
    def __init__(self, client: httpx.Client, settings: Settings) -> None:
        self._client = client
        self._settings = settings

    @property
    def source_name(self) -> str:
        return "infodengue"

    def extract(self, *, extracted_at: datetime) -> Extraction:
        disease = self._settings.infodengue_disease
        if disease not in _DISEASES:
            raise ValueError(f"unsupported InfoDengue disease: {disease}")
        rows: list[InfoDengueAlert] = []
        for year in self._settings.infodengue_years():
            payload = self._fetch_year(year)
            rows.extend(
                parse_alert(
                    item,
                    geocode=self._settings.infodengue_geocode,
                    disease=disease,
                    extracted_at=extracted_at,
                )
                for item in payload
            )
        deduped = _dedupe_weeks(tuple(rows))
        records = tuple(
            RawRecord(
                source=self.source_name,
                external_key=row.external_key,
                payload=_raw_payload(row),
                extracted_at=extracted_at,
            )
            for row in deduped
        )
        return Extraction(source=self.source_name, records=records, infodengue=deduped)

    def _fetch_year(self, year: int) -> list[dict[str, object]]:
        response = self._client.get(
            self._settings.infodengue_base_url,
            params={
                "geocode": str(self._settings.infodengue_geocode),
                "disease": self._settings.infodengue_disease,
                "format": "json",
                "ew_start": str(self._settings.infodengue_epi_week_start),
                "ew_end": str(self._settings.infodengue_epi_week_end),
                "ey_start": str(year),
                "ey_end": str(year),
            },
            headers={"Accept": "application/json", "User-Agent": USER_AGENT},
        )
        response.raise_for_status()
        body: object = response.json()
        if isinstance(body, dict):
            message = body.get("error_message") or body.get("error") or body
            raise ValueError(f"InfoDengue error for {year}: {message}")
        return as_object_list(body, label="InfoDengue alertcity")


def parse_alert(
    item: dict[str, object],
    *,
    geocode: int,
    disease: str,
    extracted_at: datetime,
) -> InfoDengueAlert:
    se = required_int(item.get("SE"), label="SE")
    epi_year, epi_week = divmod(se, 100)
    if not 1 <= epi_week <= 53:
        raise ValueError(f"SE week out of range: {se}")
    alert_level = optional_int(item.get("nivel"))
    if alert_level is not None and not 1 <= alert_level <= 4:
        raise ValueError(f"alert level out of range: {alert_level}")
    return InfoDengueAlert(
        geocode=geocode,
        disease=disease,
        epi_week_start=_week_start(item.get("data_iniSE")),
        epi_year=epi_year,
        epi_week=epi_week,
        se=se,
        cases_notified=optional_int(item.get("casos")),
        cases_estimated=optional_float(item.get("casos_est")),
        cases_estimated_min=optional_int(item.get("casos_est_min")),
        cases_estimated_max=optional_int(item.get("casos_est_max")),
        incidence_per_100k=optional_float(item.get("p_inc100k")),
        alert_level=alert_level,
        rt=optional_float(item.get("Rt")),
        probability_rt_gt1=optional_float(item.get("p_rt1")),
        population=optional_float(item.get("pop")),
        municipality_name=optional_str(item.get("municipio_nome")),
        model_version=optional_str(item.get("versao_modelo")),
        source_record_id=optional_int(item.get("id")),
        extracted_at=extracted_at,
    )


def _week_start(value: object) -> date:
    if isinstance(value, str) and value.isdigit():
        value = int(value)
    if isinstance(value, bool) or not isinstance(value, (int, float)):
        raise ValueError("data_iniSE must be epoch milliseconds")
    seconds = value / 1000 if value > 10_000_000_000 else value
    return datetime.fromtimestamp(seconds, tz=UTC).date()


def _dedupe_weeks(rows: tuple[InfoDengueAlert, ...]) -> tuple[InfoDengueAlert, ...]:
    by_key: dict[tuple[int, str, date], InfoDengueAlert] = {}
    for row in rows:
        by_key[(row.geocode, row.disease, row.epi_week_start)] = row
    return tuple(by_key.values())


def _raw_payload(row: InfoDengueAlert) -> dict[str, object]:
    return {
        "geocode": row.geocode,
        "disease": row.disease,
        "epi_week_start": row.epi_week_start.isoformat(),
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
    }
