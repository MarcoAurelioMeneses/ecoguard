"""In-process sliding window. The HTTP adapter calls it. No web framework here."""

from collections import deque


class SlidingWindowLimiter:
    def __init__(self, limit: int, window_seconds: int) -> None:
        if limit < 1:
            raise ValueError("rate limit must be at least 1")
        if window_seconds < 1:
            raise ValueError("rate limit window must be at least 1 second")
        self._limit = limit
        self._window = window_seconds
        self._hits: dict[str, deque[float]] = {}

    @property
    def window_seconds(self) -> int:
        return self._window

    def allow(self, key: str, now: float) -> bool:
        bucket = self._hits.setdefault(key, deque())
        cutoff = now - self._window
        while bucket and bucket[0] <= cutoff:
            bucket.popleft()
        if len(bucket) >= self._limit:
            return False
        bucket.append(now)
        return True
