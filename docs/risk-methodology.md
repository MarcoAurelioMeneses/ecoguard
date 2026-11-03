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

Schedule it on the ingestion process with `ECOGUARD_SCORE_CRON` (Spring cron, `America/Sao_Paulo`), for example `0 0 6 * * *`. Unset, the job runs only when asked.

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

| Indicator | Draft weight | Source | Grain today | Rule of thumb for 100 |
| --- | --- | --- | --- | --- |
| Heat | 0.25 | INMET observations when a tokened pull works. Open-Meteo is forecast context, not a substitute observation. | Station points. A district gets the nearest operating station inside a stated radius, else null. | Daily maximum temperature at or above 35 °C, or a documented heat-index threshold. Linear below that from 25 °C. |
| Heavy rain | 0.30 | INMET and, if a credentialed feed exists, CEMADEN. Open-Meteo precipitation is a forecast indicator and must be labeled as such. | Station or gauge. | 100 at or above 80 mm in 24 h. 0 at 0 mm. |
| Air quality | 0.30 | CETESB QUALAR. | Station. | 100 when the station index is the worst class under the rule in force (CONAMA 506/2024 for dates the agency classifies that way). |
| Fire proximity | 0.15 | INPE 10-minute hotspots, spatially joined to the district. | Point in polygon. | 100 if one or more hotspots fall in the district on that day. 0 if the file was read and the join is empty. Null if the file was not read. |

If CETESB is still inaccessible, the air-quality term is null and the environmental pillar is null. The other terms must not be renormalised in secret to hide a missing 0.30. The UI says which term is missing.

### Health (draft sub-weights)

| Indicator | Draft weight | Source | Grain | Rule of thumb for 100 |
| --- | --- | --- | --- | --- |
| Dengue transmission | 0.60 | InfoDengue `nivel` and `p_inc100k` for geocode 3550308. | **Municipality.** The same value is attached to every district and labeled "municipal, not district". | Map alert level 1→15, 2→40, 3→70, 4→100. If level is null, scale incidence per 100,000 against a documented threshold instead of inventing a level. |
| Respiratory hospitalizations | 0.40 | DataSUS SIH, CID-10 J00–J99 (filter versioned in the job), municipality of residence 3550308, rate per 100,000 residents. | **Municipality**, monthly, lagged. | 100 at or above the 90th percentile of the city's own monthly series over the last five published years. Null until that baseline is computed from real files. |

Health does not vary across districts with the sources in the MVP. Showing it on a choropleth without the municipal label would overstate precision. The district page should say so.

InfoDengue `pop` stays the incidence denominator for dengue. Censo 2022 resident population (SIDRA table 4709, variable 93, 2022: 11,451,999) is the denominator for any census-based rate, not a silent replacement for InfoDengue's population.

### Social (draft sub-weights)

Social indicators change slowly. They are carried forward from the latest census or PMSP publication and labeled with that year. They are not weekly measurements.

| Indicator | Draft weight | Source | Grain |
| --- | --- | --- | --- |
| Income | 0.50 | Censo 2022 rendimento, after a SIDRA table is shown to support the territorial level, aggregated from census tracts to GeoSampa districts. | District, via spatial join. |
| Vulnerability / crowding or inadequate sanitation | 0.50 | Same census, or a GeoSampa/Infocidade district table if one is adopted instead. | District. |

A direct SIDRA call at level N6 is the municipality. Probes of tables 7435, 7395, and 7531 rejected N6. Do not pick a table id until `values` returns the variable name and the district (or tract) geography we can join.

Scaling for income: 100 means more vulnerability, so lower income maps to a higher score. Publish the direction next to the number.

## Worked arithmetic (hypothetical)

This is not a district. If the three pillars were 80, 40, and 20 under the default weights:

```text
overall = 0.40 * 80 + 0.35 * 40 + 0.25 * 20
        = 32 + 14 + 5
        = 51
```

Class: Moderado (30–54). If health were null, overall would be null, not 51 and not 0.40/0.65 of the remaining pillars.

## What a published score must carry

- `config_key` and the three weights
- which indicators were null
- source name and `extracted_at` or publication date for each indicator that was used
- the grain (district, nearest station, or municipal broadcast)
- the class cut version

The analyst is only allowed to quote a score that has that metadata in `marts`. It does not estimate a missing pillar.
