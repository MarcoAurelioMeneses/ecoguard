"""Stable hashes for raw payloads."""

import hashlib
import json
from collections.abc import Mapping


def _reject_non_json(value: object) -> str:
    raise TypeError(f"payload value is not JSON-safe: {type(value).__name__}")


def payload_sha256(payload: Mapping[str, object]) -> str:
    """Hash a JSON object independent of key order."""

    encoded = json.dumps(
        payload,
        sort_keys=True,
        separators=(",", ":"),
        ensure_ascii=False,
        default=_reject_non_json,
    )
    return hashlib.sha256(encoded.encode("utf-8")).hexdigest()
