"""Composition root. The only place that wires adapters to use cases."""

import httpx

from ecoguard.adapters.config import Settings
from ecoguard.adapters.infodengue import InfoDengueSource
from ecoguard.adapters.memory import MemoryIngestionStore
from ecoguard.adapters.open_meteo import OpenMeteoSource
from ecoguard.adapters.postgres import PostgresIngestionStore
from ecoguard.ports.sources import SourcePort
from ecoguard.ports.store import IngestionStore


def build_source(name: str, client: httpx.Client, settings: Settings) -> SourcePort:
    if name == "infodengue":
        return InfoDengueSource(client, settings)
    if name == "open-meteo":
        return OpenMeteoSource(client, settings)
    raise ValueError(f"unknown source: {name}")


def build_store(
    settings: Settings, *, dry_run: bool
) -> tuple[IngestionStore, PostgresIngestionStore | None]:
    if dry_run:
        return MemoryIngestionStore(), None
    if not settings.database_url:
        raise RuntimeError("ECOGUARD_DATABASE_URL is required unless --dry-run is set")
    postgres = PostgresIngestionStore.connect(settings.database_url)
    return postgres, postgres
