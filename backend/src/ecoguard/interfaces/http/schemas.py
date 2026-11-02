"""OpenAPI response models. They stay in the interface, not in the domain."""

from typing import Literal

from pydantic import BaseModel, Field


class HealthResponse(BaseModel):
    status: Literal["ok"]
    service: Literal["ecoguard-api"]
    version: str
    environment: str


class RootResponse(BaseModel):
    service: Literal["ecoguard-api"]
    health: str = "/health"
    docs: str = "/docs"
    openapi: str = "/openapi.json"
    note: str = Field(description="Human-readable scope of this build.")
