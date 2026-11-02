"""Small JSON coercions shared by the source parsers."""


def as_object(value: object, *, label: str) -> dict[str, object]:
    if not isinstance(value, dict):
        raise ValueError(f"{label} must be a JSON object")
    return {str(key): item for key, item in value.items()}


def as_object_list(value: object, *, label: str) -> list[dict[str, object]]:
    if not isinstance(value, list):
        raise ValueError(f"{label} must be a JSON list")
    return [as_object(item, label=label) for item in value]


def optional_float(value: object) -> float | None:
    if value is None or value == "":
        return None
    if isinstance(value, bool) or not isinstance(value, (int, float, str)):
        raise ValueError(f"expected a number, got {type(value).__name__}")
    return float(value)


def optional_int(value: object) -> int | None:
    if value is None or value == "":
        return None
    if isinstance(value, bool):
        raise ValueError("expected an integer, got bool")
    if isinstance(value, int):
        return value
    if isinstance(value, float):
        if not value.is_integer():
            raise ValueError(f"expected an integer, got {value}")
        return int(value)
    if isinstance(value, str):
        return int(value)
    raise ValueError(f"expected an integer, got {type(value).__name__}")


def required_int(value: object, *, label: str) -> int:
    parsed = optional_int(value)
    if parsed is None:
        raise ValueError(f"{label} is required")
    return parsed


def required_float(value: object, *, label: str) -> float:
    parsed = optional_float(value)
    if parsed is None:
        raise ValueError(f"{label} is required")
    return parsed


def optional_str(value: object) -> str | None:
    if value is None:
        return None
    if not isinstance(value, str):
        raise ValueError(f"expected a string, got {type(value).__name__}")
    stripped = value.strip()
    return stripped or None
