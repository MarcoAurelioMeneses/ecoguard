"""Liveness, rate limit, and security headers."""

from fastapi.testclient import TestClient

from ecoguard.adapters.config import Settings
from ecoguard.application.rate_limit import SlidingWindowLimiter
from ecoguard.interfaces.http.app import create_app


def test_health_ok() -> None:
    client = TestClient(create_app(Settings(environment="test")))
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "ok"
    assert response.json()["environment"] == "test"
    assert response.headers["x-content-type-options"] == "nosniff"
    assert response.headers["x-frame-options"] == "DENY"
    assert "frame-ancestors 'none'" in response.headers["content-security-policy"]


def test_openapi_lists_health() -> None:
    client = TestClient(create_app(Settings(environment="test")))
    document = client.get("/openapi.json").json()
    assert "/health" in document["paths"]
    assert document["info"]["title"] == "EcoGuard API"


def test_rate_limit_blocks_the_second_call() -> None:
    client = TestClient(
        create_app(
            Settings(
                environment="test",
                rate_limit_requests=1,
                rate_limit_window_seconds=60,
            )
        )
    )
    assert client.get("/").status_code == 200
    blocked = client.get("/")
    assert blocked.status_code == 429
    assert blocked.headers["retry-after"] == "60"
    assert client.get("/health").status_code == 200


def test_sliding_window_drops_old_hits() -> None:
    limiter = SlidingWindowLimiter(1, 10)
    assert limiter.allow("a", 0)
    assert not limiter.allow("a", 5)
    assert limiter.allow("a", 11)
