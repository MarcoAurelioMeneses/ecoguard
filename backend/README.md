# Backend

API, ingestion workers, and the analyst boundary. One package, `ecoguard`, in hexagonal layers. See `docs/architecture.md` and `docs/adr/0009-hexagonal-backend.md`.

```text
src/ecoguard/domain        records, weights, analytics event names
src/ecoguard/application   ingest, health, rate limit
src/ecoguard/ports         store, source, analytics
src/ecoguard/adapters      HTTP sources, Postgres, memory, settings
src/ecoguard/interfaces    FastAPI and the worker CLI
src/ecoguard/agent         tool ports only; no model client
src/ecoguard/composition.py
```

```bash
uv run uvicorn ecoguard.interfaces.http.app:app --app-dir backend/src --port 8000
uv run python -m ecoguard.interfaces.cli infodengue --dry-run
```

The analyst must use the `ecoguard_agent` role and the `agent` views. Do not add an OpenAI client in this package until the controls in `security/llm-top10.md` are implemented with it.
