# ADR 0005 — Next.js and MapLibre for the web app

- Status: accepted
- Date: 2026-11-02

## Context

The interface is a desktop-first data product: choropleth, charts, and a chat panel. The visual design (dark default, light optional, risk scale, six screens) is in `design/`. MapLibre GL can paint GeoJSON from our API without a Mapbox token. The screen implementation is the next PR. Observability (screen 7 in the MVP notes) was not designed and will not get a page until it is.

## Decision

Use Next.js (App Router) and TypeScript in `frontend/`, with pnpm. MapLibre is the chosen map library and is not installed yet, because no map is rendered. `/` is screen 00 (What is EcoGuard). The other designed screens have placeholder routes.

This PR only wires the theme foundation:

- `design/tokens.css` is copied to `frontend/shared/styles/tokens.css` and imported globally
- `design/logo/logo-icone.svg` and `logo-icone-claro.svg` are copied to `public/`
- the root is dark; the class `light` on `<html>` switches theme, matching the token file
- the first paint follows `prefers-color-scheme`, then `localStorage` key `ecoguard-theme`
- the page is a placeholder, not a screen from `design/html`

Light-theme `--accent` (`#0A7EA8`) measures about 4.3:1 against `--bg` (`#F4F7F9`). That is large-text contrast only. The placeholder uses the accent on the wordmark at 24px / 600 and nowhere else. Body copy stays on `--text`.

Mockup numbers, including the chip "Dados de exemplo", are not shown and are not fetched.

## Consequences

- Designers can keep editing `design/` without the app drifting: CI fails if the wired CSS or SVG differs.
- The next PR can implement screens from `design/html` against these tokens.
- MapLibre, Recharts or ECharts, and the district GeoJSON endpoint are still to be added.
- The 2016 simplified polygons baked into the design HTML are not reused as a map source.
