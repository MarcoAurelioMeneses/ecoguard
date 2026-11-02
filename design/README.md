# Design package

Originals as delivered, plus this note. Portuguese copy in `LEIA-ME.md` and `design-system.md` is part of the package and is left as delivered.

| Path | What it is |
| --- | --- |
| `design-system.md` | Tokens, type, components |
| `tokens.css` | Dark on `:root`, light on `.light`. Copied into `frontend/shared/styles/` |
| `mockups/`, `html/` | Screens 00 through 06, dark and light |
| `logo/` | Vector icon and AI-generated PNG lockups |
| `src/` | Generators. Not a runtime dependency |
| `src/distritos-sp.geojson` | Dev fixture only. See below |

The running app does not render these screens. `/` is reserved for screen 00. `scripts/check-design-sync.sh` checks the token and logo copies.

## District fixture

`src/distritos-sp.geojson` is the file the mockup generator reads.

| Field | Value |
| --- | --- |
| Origin | `codigourbano/distritos-sp`, itself a GeoSampa extract |
| Vintage | 2016, as stated in `design-system.md` |
| CRS | CRS84 (longitude, latitude) |
| Properties | `ds_codigo`, `ds_nome`, `ds_sigla`, `ds_subpref`, `ds_cd_sub` |
| Use | Regenerating mockups with `src/screens.py` only |

It is not the ingestion source and it is not served to MapLibre. The source of truth is the current GeoSampa WFS layer `geoportal:distrito_municipal` (EPSG:31983, 96 features). See `docs/data-sources.md`. A database check constraint rejects any other district source label.

## Other rules

- Numbers on the mockups are examples. Do not seed them.
- There is no observability screen.
- Light-theme accent in the running app is `#076E94` (`--accent` on `.light`). Control borders use `--border-strong`. Body text stays at least 14px. The path `ecoguard-design/` (design-system v0.2, `extras/estados-*.png`, `uiux-review/auditoria.md`) was not in the workspace. Those token rules are applied here. Pixel comparison against the missing audit images is not done.
- Overall weights 0.40 / 0.35 / 0.25 are confirmed and configurable. The design-system sentence that still calls them a proposal is the original package text. `docs/risk-methodology.md` is the product rule.
