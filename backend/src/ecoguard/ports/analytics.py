"""Product analytics sink. The default adapter records nothing."""

from typing import Protocol

from ecoguard.domain.analytics import ALLOWED_EVENTS, BANNED_PROPERTY_KEYS


class AnalyticsSink(Protocol):
    def capture(self, event: str, properties: dict[str, str | int]) -> None:
        """Record one allowed event. Implementations must refuse banned keys."""


def assert_event(event: str, properties: dict[str, str | int]) -> None:
    if event not in ALLOWED_EVENTS:
        raise ValueError(f"unknown analytics event: {event}")
    banned = BANNED_PROPERTY_KEYS.intersection(properties)
    if banned:
        raise ValueError(f"analytics properties are not allowed: {sorted(banned)}")
