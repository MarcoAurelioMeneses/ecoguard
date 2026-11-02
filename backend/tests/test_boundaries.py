"""Architecture and configuration guards."""

from pathlib import Path

from ecoguard.adapters.analytics import NullAnalytics
from ecoguard.domain.analytics import ANALYST_QUESTION_ASKED, SCREEN_VIEWED
from ecoguard.domain.risk import OverallWeights

ROOT = Path(__file__).resolve().parents[2]
BANNED = ("fastapi", "httpx", "psycopg", "pydantic", "starlette", "openai")


def test_domain_and_application_do_not_import_frameworks() -> None:
    for folder in ("domain", "application", "ports"):
        for path in (ROOT / "backend" / "src" / "ecoguard" / folder).rglob("*.py"):
            text = path.read_text(encoding="utf-8")
            for name in BANNED:
                assert f"import {name}" not in text
                assert f"from {name}" not in text


def test_runtime_does_not_load_the_2016_district_file() -> None:
    roots = [ROOT / "backend" / "src", ROOT / "frontend"]
    for root in roots:
        for path in root.rglob("*"):
            if path.suffix not in {".py", ".ts", ".tsx", ".sql"}:
                continue
            text = path.read_text(encoding="utf-8")
            assert "distritos-sp.geojson" not in text
            assert "codigourbano" not in text


def test_confirmed_weights_combine_and_skip_missing_pillars() -> None:
    weights = OverallWeights(environmental=0.40, health=0.35, social=0.25, status="confirmed")
    assert weights.combine(80, 40, 20) == 51
    assert weights.combine(80, None, 20) is None


def test_unconfirmed_weights_cannot_publish() -> None:
    weights = OverallWeights(environmental=0.40, health=0.35, social=0.25, status="draft")
    try:
        weights.combine(1, 1, 1)
    except ValueError as exc:
        assert "confirmed" in str(exc)
    else:
        raise AssertionError("draft weights were accepted")


def test_default_weight_seed_is_confirmed() -> None:
    seed = (ROOT / "db" / "migrations" / "0008_risk_config.sql").read_text(encoding="utf-8")
    assert "0.400" in seed
    assert "0.350" in seed
    assert "0.250" in seed
    assert "'confirmed'" in seed
    assert "provisional" not in seed.lower()


def test_analytics_rejects_question_text() -> None:
    sink = NullAnalytics()
    sink.capture(SCREEN_VIEWED, {"screen": "00"})
    try:
        sink.capture(ANALYST_QUESTION_ASKED, {"question": "raw text"})
    except ValueError:
        return
    raise AssertionError("question text was accepted")
