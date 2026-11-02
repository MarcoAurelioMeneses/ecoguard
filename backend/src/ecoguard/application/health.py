"""Liveness report. The HTTP layer maps it into the OpenAPI schema."""

from dataclasses import dataclass
from typing import Literal


@dataclass(frozen=True)
class HealthReport:
    status: Literal["ok"]
    service: Literal["ecoguard-api"]
    version: str
    environment: str


def health_report(*, version: str, environment: str) -> HealthReport:
    return HealthReport(
        status="ok",
        service="ecoguard-api",
        version=version,
        environment=environment,
    )
