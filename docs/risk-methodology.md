# Risk score methodology

Overall weights are **confirmed** by Marco and stored as configuration. They are not fitted to outcomes. Indicator sub-weights and the class cuts below are still a draft.

Mockup scores, sparklines, and alert counts are example drawings. They are not in the database and must not be copied into seeds, fixtures that pretend to be production, or the UI.

## Overall score

For a district `d` and a day `t`, when all three pillars are present:

```text
overall(d, t) = 0.40 * environmental(d, t)
              + 0.35 * health(d, t)
              + 0.25 * social(d, t)
```

| Pillar | Default weight | Status |
| --- | --- | --- |
| Environmental | 0.40 | Confirmed |
| Health | 0.35 | Confirmed |
| Social | 0.25 | Confirmed |

The weights live in `marts.risk_score_config` under `config_key = 'default'`, `status = 'confirmed'`. They sum to 1 (enforced by a check constraint). Changing them is an update to that row, not a code change and not a second copy in the frontend. The methodology page must read the same row the job uses. A different row can be inserted later; only a row the job is told to read affects scores.

`marts.district_risk_scores` is filled by the `risk-scores` job. Heat, rain, and PM2.5 come from Open-Meteo at each district point-on-surface. Fire counts are INPE hotspots inside the polygon. Dengue is the municipal InfoDengue value, labeled as such. Social uses Censo 2022 district population density and the share of occupied households off the sewer network. Income is not in that universe release, and DataSUS hospitalizations have no anonymous API, so both stay missing.

A pillar is published when at least 50% of its indicator weight is present. The present weights are renormalized and the coverage fraction is stored. Overall uses 0.40 / 0.35 / 0.25 when all three pillars clear that bar. Re-running a date updates the current row and appends `marts.district_risk_score_history`.

Schedule the same sequence as the backfill on the ingestion process with `ECOGUARD_SCHEDULE_CRON` (Spring cron, `America/Sao_Paulo`), for example `0 0 6 * * *`. Unset, the job runs only when asked. `ECOGUARD_BACKFILL_DAYS` defaults to 30 (maximum 92).

If any pillar is null, `overall` is null. The map class for a null score is "sem dado", not a zero and not an interpolated colour.

## Classes

These bands match the design system so the map legend and the number agree. They are presentation cuts on a 0–100 score. Marco confirmed the pillar weights, not these cuts.

| Class | Score |
| --- | --- |
| Baixo | 0–29 |
| Moderado | 30–54 |
| Alto | 55–74 |
| Crítico | 75–100 |

Colour is never the only encoding. A badge or a number goes with it.

## Pillar shape

Each pillar is a weighted mean of indicators that have been scaled to 0–100, using only indicators that are present. The sub-weights below are a draft so the overall weights have something to sit on. They are **not** confirmed and they are **not** loaded as a second config table yet. When they are implemented they should be rows, not literals in the job.

An indicator is null when the source is missing, late beyond its documented cadence, or not defined at that grain. It is not filled with the city average unless the methodology text for that indicator says the value is city-wide on purpose.

### Environmental (draft sub-weights)

| Indicator | Draft weight | Source the job reads | Grain today | Rule of thumb for 100 |
| --- | --- | --- | --- | --- |
| Heat | 0.25 | Open-Meteo forecast daily maximum at the district point-on-surface. Labeled as a forecast, not an INMET observation. | District point. | 100 at or above 35 °C. 0 at or below 25 °C. Linear between. |
| Heavy rain | 0.30 | Open-Meteo forecast daily precipitation at the same point. Labeled as a forecast. | District point. | 100 at or above 80 mm. 0 at 0 mm. |
| Air quality | 0.30 | Open-Meteo Air Quality daily mean PM2.5 (PM10 and ozone are stored, not scored). CETESB QUALAR has no anonymous API. | District point. | 0 at or below 5 µg/m³. 100 at or above 50. |
| Fire | 0.15 | INPE daily Brazil CSV, rows for municipality 3550308, `ST_Within` the district polygon. | District polygon. | 100 if one or more hotspots fall in the district that day. 0 if the file was read and the join is empty. Null if the file was not read. |

A missing indicator stays in the missing list. Present indicator weights are renormalized once their share of the pillar weight is at least 0.50. Below that, the pillar score is null and coverage is still stored. The UI shows the coverage fraction. Pillar weights 0.40 / 0.35 / 0.25 are not renormalized: if any pillar score is null, overall is null.

### Health (draft sub-weights)

| Indicator | Draft weight | Source | Grain | Rule of thumb for 100 |
| --- | --- | --- | --- | --- |
| Dengue transmission | 0.60 | InfoDengue `nivel` and `p_inc100k` for geocode 3550308. | **Municipality.** The same value is attached to every district and labeled "municipal, not district". | Map alert level 1→15, 2→40, 3→70, 4→100. If level is null, scale incidence per 100,000 against a documented threshold instead of inventing a level. |
| Respiratory hospitalizations | 0.40 | DataSUS SIH has no confirmed anonymous API. The job records `respiratory_datasus` as missing and does not substitute a proxy. | Not loaded. | Null until an anonymous file pull is confirmed. |

Health does not vary across districts with the sources in the MVP. Showing it on a choropleth without the municipal label would overstate precision. The district page should say so.

InfoDengue `pop` stays the incidence denominator for dengue. Censo 2022 resident population (SIDRA table 4709, variable 93, 2022: 11,451,999) is the denominator for any census-based rate, not a silent replacement for InfoDengue's population.

### Social (draft sub-weights)

Social indicators change slowly. They are carried forward from the latest census or PMSP publication and labeled with that year. They are not weekly measurements.

| Indicator | Draft weight | Source the job reads | Grain |
| --- | --- | --- | --- |
| Population density | 0.25 | IBGE Censo 2022 district universe, population `v0001` and `AREA_KM2`, matched to GeoSampa by normalized district name. | District. 0 at or below 2,000/km², 100 at or above 20,000. |
| Sanitation | 0.25 | Same universe, share of occupied households (`v0007`) not on the sewer network (`V00309`). | District. The share times 100. |
| Income | 0.50 | Not in the 2022 district universe tables. SIDRA tables probed for rendimento rejected the municipal level, and no district income table was found. The indicator stays missing. | Not loaded. |

SIDRA level N6 is the municipality. The district figures come from the official “Agregados por distrito” CSVs (96 rows, `CD_MUN` 3550308), not from a census-tract spatial join and not from the IBGE district mesh. Income, when a table exists, must point the other way: lower income is higher risk.

## Worked arithmetic (hypothetical)

This is not a district. If the three pillars were 80, 40, and 20 under the default weights:

```text
overall = 0.40 * 80 + 0.35 * 40 + 0.25 * 20
        = 32 + 14 + 5
        = 51
```

Class: Moderado (30–54). If the health pillar score were null, overall would be null. The 0.40 / 0.35 / 0.25 weights are not renormalized across pillars. Inside a pillar, indicator weights are renormalized only after coverage reaches 0.50. With dengue present (0.60) and respiratory missing (0.40), health coverage is 0.60 and the pillar equals the dengue indicator. With density and sanitation present (0.25 + 0.25) and income missing (0.50), social coverage is 0.50 and the pillar is the mean of those two indicators.

## What a published score must carry

- `config_key` and the three weights
- which indicators were null
- source name and `extracted_at` or publication date for each indicator that was used
- the grain (district, nearest station, or municipal broadcast)
- the class cut version

The analyst is only allowed to quote a score that has that metadata in `marts`. It does not estimate a missing pillar.
