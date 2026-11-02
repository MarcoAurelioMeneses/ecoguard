"""CLI: `python -m ecoguard.interfaces.cli infodengue|open-meteo`."""

import argparse
import logging
import sys

import httpx

from ecoguard.adapters.config import Settings
from ecoguard.application.ingest import run_ingestion
from ecoguard.composition import build_source, build_store

logger = logging.getLogger("ecoguard.workers")


def main() -> None:
    raise SystemExit(run())


def run(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(prog="ecoguard-workers")
    parser.add_argument("job", choices=("infodengue", "open-meteo"))
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Extract and upsert in memory. Still calls the source. Does not need a database.",
    )
    args = parser.parse_args(argv)
    logging.basicConfig(level=logging.INFO, format="%(levelname)s %(message)s")
    settings = Settings()
    try:
        store, postgres = build_store(settings, dry_run=args.dry_run)
    except RuntimeError as exc:
        logger.error("%s", exc)
        return 2
    try:
        with httpx.Client(timeout=settings.http_timeout_seconds) as client:
            source = build_source(args.job, client, settings)
            result = run_ingestion(source, store)
    except Exception as exc:
        logger.error("%s failed: %s", args.job, exc)
        return 1
    finally:
        if postgres is not None:
            postgres.close()
    logger.info(
        "%s extracted=%s raw_upserted=%s staging_upserted=%s run_id=%s",
        result.source,
        result.extracted,
        result.raw_upserted,
        result.staging_upserted,
        result.run_id,
    )
    return 0


if __name__ == "__main__":
    sys.exit(run())
