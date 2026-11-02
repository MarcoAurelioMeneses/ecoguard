"""Analyst boundary. No model client is wired in this foundation.

The future LangGraph graph will depend on these ports and on the `ecoguard_agent`
role, which can read only the `agent` schema views. See security/llm-top10.md.
"""

from dataclasses import dataclass
from datetime import date
from typing import Protocol


@dataclass(frozen=True)
class RiskQuote:
    district_code: str
    on: date
    overall: float | None
    config_key: str


class RiskLookup(Protocol):
    def get_risk(self, district_code: str, on: date) -> RiskQuote: ...


class AllowlistedSql(Protocol):
    def query(self, sql: str) -> list[dict[str, object]]: ...


class DocumentSearch(Protocol):
    def search(self, query: str, limit: int) -> list[dict[str, object]]: ...
