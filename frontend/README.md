# Frontend

Next.js App Router application in `src/`. It is separate from the Java API. Structure and import rules are in [ARCHITECTURE.md](ARCHITECTURE.md).

| Route        | Feature             | Screen                                                                |
| ------------ | ------------------- | --------------------------------------------------------------------- |
| `/`          | `features/about`    | 00 What is EcoGuard                                                   |
| `/overview`  | `features/overview` | 01 City overview. KPIs are no-data until scores exist. Chart or table |
| `/map`       | `features/map`      | 02 Risk map. Geometry from `GET /districts`. Scores are no-data       |
| `/districts` | `features/district` | 03 District list and `/districts/[code]`                              |
| `/analyst`   | `features/analyst`  | 04 AI City Analyst                                                    |
| `/alerts`    | `features/alerts`   | 05 No alerts until those feeds exist                                  |
| `/sources`   | `features/sources`  | 06 Weights from `GET /risk-config` plus the catalog                   |

`src/styles/tokens.css` and `public/logo/v2/` are copies of `design/`. `public/favicon.svg` is the mark used below 24px. `scripts/check-design-sync.sh` checks them. The wordmark is Eco in `--accent` and Guard in `--text`. Light-theme accent is `#076E94`. Control borders use `--border-strong`. Body copy stays at least 14px. Risk marks use a shape as well as a color. Charts and the map have a "View as table" control. Focus is visible. Motion is removed when `prefers-reduced-motion` is set.

`src/lib/analytics` validates events and forwards them to PostHog only when both `NEXT_PUBLIC_POSTHOG_KEY` and `NEXT_PUBLIC_POSTHOG_HOST` are set. Autocapture, cookies, session replay, and question text are off.

Do not render numbers from `design/html` or polygons from `design/src/distritos-sp.geojson`.

```bash
pnpm --filter @ecoguard/web dev
pnpm --filter @ecoguard/web test
pnpm --filter @ecoguard/web generate:api
```

`NEXT_PUBLIC_API_BASE_URL` defaults to `http://localhost:8000`. The API also allows `http://127.0.0.1:3000`. Development CSP adds `'unsafe-eval'` because the Next dev server compiles with `eval`. The production build does not.
