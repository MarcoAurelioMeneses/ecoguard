"""FastAPI delivery. Maps HTTP onto application functions and adds guards."""

import time
from collections.abc import Awaitable, Callable

from fastapi import FastAPI
from fastapi.responses import JSONResponse
from starlette.requests import Request
from starlette.responses import Response

from ecoguard.adapters.config import Settings
from ecoguard.application.health import health_report
from ecoguard.application.rate_limit import SlidingWindowLimiter
from ecoguard.interfaces.http.schemas import HealthResponse, RootResponse

_ROOT_NOTE = (
    "Foundation build. Read endpoints and the analyst are not served yet. "
    "Mockup figures are not data."
)
_OPEN_PATHS = frozenset({"/health", "/openapi.json", "/docs", "/redoc", "/docs/oauth2-redirect"})

_SECURITY_HEADERS = {
    "X-Content-Type-Options": "nosniff",
    "X-Frame-Options": "DENY",
    "Referrer-Policy": "no-referrer",
    "Permissions-Policy": "geolocation=(), camera=(), microphone=()",
    "Content-Security-Policy": "default-src 'none'; frame-ancestors 'none'; base-uri 'none'",
    "Cache-Control": "no-store",
}


def create_app(settings: Settings | None = None) -> FastAPI:
    resolved = settings if settings is not None else Settings()
    limiter = SlidingWindowLimiter(
        resolved.rate_limit_requests,
        resolved.rate_limit_window_seconds,
    )
    app = FastAPI(
        title=resolved.api_title,
        version=resolved.api_version,
        summary="City risk intelligence for São Paulo.",
        description=(
            "Typed HTTP API. This foundation exposes liveness and OpenAPI. "
            "Later revisions add read-only district and score endpoints. "
            "The analyst is restricted to allowlisted database views."
        ),
    )

    @app.middleware("http")
    async def guard(
        request: Request,
        call_next: Callable[[Request], Awaitable[Response]],
    ) -> Response:
        if request.url.path not in _OPEN_PATHS:
            client = request.client.host if request.client is not None else "unknown"
            if not limiter.allow(client, time.monotonic()):
                return JSONResponse(
                    {"detail": "rate limit exceeded"},
                    status_code=429,
                    headers={
                        "Retry-After": str(limiter.window_seconds),
                        **_SECURITY_HEADERS,
                    },
                )
        response = await call_next(request)
        for key, value in _SECURITY_HEADERS.items():
            response.headers[key] = value
        return response

    @app.get("/", response_model=RootResponse)
    def root() -> RootResponse:
        return RootResponse(service="ecoguard-api", note=_ROOT_NOTE)

    @app.get("/health", response_model=HealthResponse)
    def health() -> HealthResponse:
        report = health_report(version=resolved.api_version, environment=resolved.environment)
        return HealthResponse(
            status=report.status,
            service=report.service,
            version=report.version,
            environment=report.environment,
        )

    return app


app = create_app()
