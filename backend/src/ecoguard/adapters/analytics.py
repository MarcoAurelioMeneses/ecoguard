"""Analytics adapter that drops events after validating the taxonomy.

PostHog is the planned sink (self-hosted, cookieless or consent-based). It is
not called from this foundation. See docs/analytics.md.
"""

from ecoguard.ports.analytics import assert_event


class NullAnalytics:
    def __init__(self) -> None:
        self.events: list[tuple[str, dict[str, str | int]]] = []

    def capture(self, event: str, properties: dict[str, str | int]) -> None:
        assert_event(event, properties)
        self.events.append((event, dict(properties)))
