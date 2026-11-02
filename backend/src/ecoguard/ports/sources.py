"""A source adapter fetches. It does not touch the database."""

from datetime import datetime
from typing import Protocol

from ecoguard.domain.records import Extraction


class SourcePort(Protocol):
    @property
    def source_name(self) -> str:
        """Stable source id stored on raw rows and job runs."""

    def extract(self, *, extracted_at: datetime) -> Extraction:
        """Download and map into domain records."""
