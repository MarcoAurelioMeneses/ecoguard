# API reference

The running contract is the OpenAPI document springdoc builds from the controllers. With the API running:

- `GET /openapi.json` — the document
- `GET /docs` — the rendered reference
- `GET /health` — liveness
- `GET /` — pointers to those paths
- `GET /districts` — GeoJSON `FeatureCollection` of `marts.districts`, ordered by name
- `GET /districts/{code}` — one feature, or 404
- `GET /risk-config` — confirmed overall weights (`environmental`, `health`, `social`, `status`)
- `POST /analyst/questions` — body `{ "question": "..." }`, response `{ "answer": "..." }`. Returns 503 until `OPENAI_API_KEY` is set

Scores and alerts are not served yet. A district feature has geometry and official attributes only. `properties` does not invent a score.

The web client is generated from the committed snapshot `frontend/openapi/openapi.json`:

```bash
pnpm --filter @ecoguard/web generate:api
```

Refresh that snapshot from a running API when a controller changes, then regenerate `frontend/shared/api/generated.ts`. `frontend/shared/api/contract.test.mjs` checks the operation ids the UI depends on.

Security on every API response: `X-Content-Type-Options`, `X-Frame-Options: DENY`, `Referrer-Policy: no-referrer`, a locked-down `Permissions-Policy`, and `Content-Security-Policy: default-src 'none'`. The browser origin allowed to call the API is `ECOGUARD_WEB_ORIGIN` (default `http://localhost:3000`). `GET /health` and the documentation paths are not rate limited, so a platform probe is not throttled.

Run it from the repository root:

```bash
./backend/gradlew -p backend :api:bootRun
```

The API listens on port 8000, which is what the web client uses when `NEXT_PUBLIC_API_BASE_URL` is unset.
```
