# Design package

Originals as delivered, plus this note. Portuguese copy in `LEIA-ME.md` and `design-system.md` is part of the package and is left as delivered.

| Path | What it is |
| --- | --- |
| `design-system.md` | v0.2 tokens, type, components |
| `tokens.css` | Dark on `:root`, light on `.light`. Copied into `frontend/src/styles/` |
| `mockups/`, `html/` | Screens 00 through 06, dark and light |
| `mockups/extras/estados-*.png` | Loading, empty, error, focus, and no-data sheet |
| `uiux-review/auditoria.md` | v0.2 audit. Portuguese, left as delivered |
| `logo/v2/` | Final logo. Horizontal, vertical, and icon SVGs in light, dark, and mono, plus favicon and PNG exports. Copied to `frontend/public/logo/v2/` |
| `src/` | Generators. Not a runtime dependency |
| `src/distritos-sp.geojson` | Dev fixture only. See below |

`/` is screen 00. `scripts/check-design-sync.sh` checks the token file and `logo/v2`. Mockup numbers are examples and are not seeded. The in-app wordmark is text: Eco in `--accent`, Guard in `--text`. Marks smaller than 24px use `favicon.svg`.

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
- This folder is the v0.2 package. Light-theme accent is `#076E94`. Control borders use `--border-strong`. Body text stays at least 14px. The audit is `uiux-review/auditoria.md`. Mockup numbers stay examples.
- Overall weights 0.40 / 0.35 / 0.25 are confirmed and configurable. The design-system sentence that still calls them a proposal is the original package text. `docs/risk-methodology.md` is the product rule.
