# Data source catalog

São Paulo municipality only (IBGE `3550308`), district grain where a source actually has it. Checked on **2026-11-02** from this environment unless a row says the live call failed. "Verified" means an HTTP response was read. Official documentation is cited when a host did not answer.

Mockup lag labels ("Atualizado há 2 h", and the hour estimates in the design system) are **design copy**, not measured SLAs. Freshness in the product must come from `extracted_at`.

## Access summary

| Source | Programmatic access | Grain for São Paulo | Flag |
| --- | --- | --- | --- |
| InfoDengue | Yes. Public JSON/CSV, no key. Worker implemented. | Municipality × epidemiological week. Not district. | Usable. Revised nowcast. |
| Open-Meteo | Yes. Public JSON, no key on the free tier. Worker implemented. | One forecast grid cell for a city reference point. Not district. Not observations. | Free tier is non-commercial. |
| INMET | Partial. Station catalog is anonymous. Observations were not reliably downloadable. | Automatic stations (points). Two operating stations in the capital. | Do not build the worker until a tokened pull succeeds. |
| CETESB QUALAR | No anonymous API. Login-gated web app and CSV export. | Air-quality stations (points), hourly. | **Not programmatically accessible** without an account and an unofficial client. |
| INPE BDQueimadas | Yes. HTTPS CSV tree, no key. | Point hotspots. 10-minute files have no municipality column. | Usable. Spatial join required. Not a stable REST contract. |
| CEMADEN | No anonymous API verified. Download page is an HTML form. | Gauges and alerts, not districts. | **Not programmatically accessible** for anonymous ingestion. |
| DataSUS SIH | File-based (DBC on the DATASUS file server). Live fetch **not confirmed** (timeout). OpenDataSUS CKAN API was not available. | Hospital admission, municipality of residence, month. Not district. | Programmatic in principle, unconfirmed live, not a JSON API. |
| IBGE Censo 2022 / SIDRA | Yes for published aggregates. SIDRA HTTP API answered. `servicodados` v3 timed out. | Municipality for the verified population table. IBGE districts ≠ the 96 PMSP districts. | Usable at municipal grain. District figures need a spatial join of census tracts onto GeoSampa. |
| IBGE malhas | Municipal mesh API exists in the official docs. Census-tract and IBGE-district meshes are GeoFTP files (GeoPackage HEAD returned 200). | Not the PMSP district layer. | Do not use IBGE district meshes as the choropleth. |
| GeoSampa | Yes. Public WFS, no key. | **96 municipal districts.** This is the choropleth. | Use the live WFS. Do not use the 2016 GeoJSON. |

## Geography rule

The map unit is the municipal district from GeoSampa, layer `geoportal:distrito_municipal`.

Verified `GetFeature` with `resultType=hits` on 2026-11-02:

- `numberMatched`: **96**
- CRS: `EPSG:31983` (SIRGAS 2000 / UTM zone 23S)
- sample properties include `cd_distrito_municipal`, `nm_distrito_municipal`, `sg_distrito_municipal`, `cd_identificador_subprefeitura`, `qt_area_quilometro`, `dt_atualizacao`
- sample feature `dt_atualizacao`: `2026-10-27T22:04:13.456Z`
- WFS `timeStamp` on the response: `2026-11-02T14:34:33Z` (that is the request time, not a dataset version)

Endpoint (one feature, JSON):

`https://wfs.geosampa.prefeitura.sp.gov.br/geoserver/geoportal/ows?service=WFS&version=2.0.0&request=GetFeature&typeNames=geoportal:distrito_municipal&count=1&outputFormat=application/json`

Store geometries as EPSG:4326 after `ST_Transform`. Record `source = geosampa:wfs:geoportal:distrito_municipal`. The check constraint on `marts.districts` allows only that label.

The collector `geosampa-districts` requests `srsName=EPSG:4326` and `outputFormat=application/json`. A live feature on 2026-11-02 returned a GeoJSON polygon whose first coordinate was about `-46.76, -23.55` (longitude, latitude) and CRS `urn:ogc:def:crs:EPSG::4326`. The parser treats a CRS name ending in `4326` or containing `CRS84` as SRID 4326 and otherwise assumes the layer default, EPSG:31983. It refuses any count other than 96. The upsert is idempotent on `district_code`. Migrations do not insert districts.

The design package includes `design/src/distritos-sp.geojson`. It is a **2016** extract from `codigourbano/distritos-sp` (properties `ds_codigo`, `ds_nome`, `ds_sigla`, CRS84), which `design-system.md` identifies as an older GeoSampa dump. `design/src/data.py` loads it to draw mockups. That is the only allowed use: a dev fixture for regenerating the design images. It is not ingested, not copied into `frontend/public`, and not a MapLibre source. The database check constraint rejects that source label.

IBGE "distrito" (Censo 2022 mesh `SP_distritos_CD2022.gpkg` on GeoFTP) is a different partition. It must not be swapped in for the 96 PMSP districts.

## InfoDengue

| | |
| --- | --- |
| What | Dengue, chikungunya, and zika alert series. This worker loads dengue. |
| Access | `GET https://info.dengue.mat.br/api/alertcity` |
| Required query | `geocode`, `disease` (`dengue`, `chikungunya`, `zika`), `format` (`json` or `csv`), `ew_start`, `ew_end` (1–53), `ey_start`, `ey_end` |
| Auth | None. |
| License | No machine-readable license on the API documentation reviewed. Treat as a public research API. Cite InfoDengue (Fiocruz and partners). Confirm redistribution terms before any commercial republishing. |
| Update | Weekly. The provider's own documentation says notified and estimated cases are revised every week (nowcast). `versao_modelo` is on each row. |
| Grain | Municipality × epidemiological week. For São Paulo, `Localidade_id` was `0`. Sub-municipal series are documented for Rio de Janeiro, not for the 96 districts. |
| Verified | `geocode=3550308`, dengue, weeks 40–42 of 2025 returned a JSON list of 3 objects for `São Paulo`. |

Response notes that the parser relies on:

- `data_iniSE` is **epoch milliseconds**, not an ISO date. Week 42 of 2025 arrived as `1760227200000`, which is `2025-10-12` (Sunday) in UTC.
- `SE` is `YYYYWW` (for example `202542`).
- `pop` and the temperature/humidity fields arrived as **strings**. Population on that row was the provider's denominator, not the Censo 2022 total below.
- `casprov_est` and related fields can be JSON `null`.
- The object does not include `geocode`. The worker stores the geocode it requested.

Limitations: recent weeks move; incidence uses InfoDengue's population, so do not overwrite it with the census figure; a city-wide series cannot be painted as a district choropleth without saying it is uniform.

## Open-Meteo

| | |
| --- | --- |
| What | Numerical weather forecast. |
| Access | `GET https://api.open-meteo.com/v1/forecast` |
| Auth | None on the free host. A commercial subscription uses `customer-api.open-meteo.com` and `apikey`. |
| License | Data: CC BY 4.0. Attribution required, including a link of the form "Weather data by Open-Meteo.com" to `https://open-meteo.com/`. |
| Terms | Free tier is **non-commercial**: under 10,000 calls/day, 5,000/hour, and 600/minute, with no uptime guarantee. Commercial use (a paid product, ads, client work, internal business operations) needs a subscription. A portfolio or non-commercial evaluation fits the free tier. A commercial launch does not. |
| Update | Each call returns a fresh forecast. Default horizon is 7 days; the API allows up to 16 (`forecast_days`). |
| Grain | Grid cell. Not a district. Not a station observation. |
| Verified | `latitude=-23.55&longitude=-46.63`, daily variables, `timezone=America/Sao_Paulo`, `forecast_days=2` returned HTTP 200. |

The worker asks for `-23.5505, -46.6333` (city reference near the historic centre) and stores both the request and the grid point the API returns. On the verification call the API snapped `-23.55, -46.63` to about `-23.514938, -46.610504`. Upserts key on our `location_id` (`sp-city-reference`) plus the forecast date, so a snap does not create a second series.

Variables stored: `weather_code`, `temperature_2m_max`, `temperature_2m_min`, `precipitation_sum`, `precipitation_hours`, `wind_speed_10m_max`.

District forecasts, if added later, should pass district centroids explicitly. Do not repeat the city cell 96 times and call it a district forecast.

## INMET

| | |
| --- | --- |
| What | Official station observations (temperature, rain, humidity, wind, pressure). |
| Catalog | `GET https://apitempo.inmet.gov.br/estacoes/T` (automatic) and `/estacoes/M` (manual). |
| Observations | Documented by third-party clients as `GET /estacao/{YYYY-MM-DD}/{YYYY-MM-DD}/{station_code}`. |
| Auth | Catalog did not require a token. A token is reported as required for the API by current client libraries, requested from `cadastro.act@inmet.gov.br`. |
| License | Federal public meteorological data. Cite INMET. There is no separate OSI license. |
| Update | Automatic stations are hourly. Series are corrected after the fact. |
| Grain | Station point. |
| Verified | Catalog HTTP 200, 669 automatic stations, no token. In the capital, **A701 São Paulo - Mirante** (`-23.4963, -46.6201`, Operante) and **A771 São Paulo - Interlagos** (`-23.7244, -46.6775`, Operante). |
| Not verified | `GET /estacao/2026-10-28/2026-10-28/A701` returned **204 No Content**. A retry and a 2024-01-15 request dropped the connection (`curl` exit 52). `GET /estacao/diaria/...` returned 404 `E_ROUTE_NOT_FOUND`. |

BDMEP (`https://bdmep.inmet.gov.br/`) is an email-and-queue CSV workflow, not an API.

**Flag:** anonymous observation download is not reliable enough to implement. Next step is a token from INMET and one successful hourly pull for A701 and A771. Until then, heat and rain observations are missing and Open-Meteo remains forecast-only.

## CETESB QUALAR

| | |
| --- | --- |
| What | Air quality (pollutants and a quality index) and meteorological parameters from the state automatic network. |
| Human access | `https://qualar.cetesb.sp.gov.br/qualar/home.do` (HTTP 200 on 2026-11-02). Export to CSV is a logged-in feature: period, station, parameter, hourly or moving average. |
| API | No public REST API was found in CETESB documentation. |
| Unofficial clients | R package `qualR` and Python `qualR.py` log into QUALAR with a username and password and download CSV. That is a private session, not a published contract. |
| License | State environmental monitoring data. Cite CETESB. An account does not grant an open license to republish. |
| Update | Automatic network is hourly. The agency states that values are subject to later validation. The daily bulletin is published around 11:00. |
| Grain | Station point inside and around the city. Not district. A district value needs a documented spatial rule (nearest station or interpolation). |
| Classification | From 2026-01-08, QUALAR classifies even historical hours with the CONAMA 506/2024 limits that took effect on 2026-01-01. Do not mix old and new index legends without labeling the rule. |

**Flag: not programmatically accessible** for this project until Marco has a QUALAR account and accepts the unofficial client (or CETESB offers a real API). Do not scrape the login in this repository. Air quality cannot honestly feed the environmental score before that decision.

## INPE BDQueimadas

| | |
| --- | --- |
| What | Active fire hotspots from satellite imagery. |
| Access verified | HTTPS directory listing `https://dataserver-coids.inpe.br/queimadas/queimadas/focos/csv/10min/` (HTTP 200). Files are named `focos_10min_YYYYMMDD_HHMM.csv`. A sample file began with the header `lat,lon,satelite,data` and rows from GOES-19. |
| Also published | The open-data page (`https://data.inpe.br/queimadas/dados-abertos/`) lists CSV and KML from 10 minutes up to annual archives, plus fire-event products marked provisional. |
| Auth | None on the CSV tree. |
| License / cost | INPE states that queimadas products are published free of charge. Cite INPE Programa Queimadas (the FAQ points at ABNT citation). There is no OSI license text. |
| Update | The programme describes a 10-minute refresh after automated quality control. Portal statistics can lag by hours relative to the raw detections. |
| Grain | Pixel centre, not a burned polygon and not a district. The 10-minute file has **no municipality, biome, or FRP column**. Attribute the point to a district with a spatial join against `marts.districts`. |
| Limitation | Detection depends on satellite resolution, cloud, and the algorithm. A hotspot is not confirmation of a fire perimeter. São Paulo capital will often have zero vegetation hotspots; an empty join is a real result. |

**Flag:** accessible, with a weak schema. Pin the expected header and fail the job if columns move. Do not assume the richer monthly CSV (municipality, biome, precipitation) has the same columns; that schema was not re-downloaded in this check. The directory index for `mensal/` showed an old folder timestamp (2023-12-28) and was not audited file by file.

A third-party description of a JSON API under `terrabrasilis.dpi.inpe.br/queimadas/bdqueimadas/api` was **not** used as the contract, because the CSV tree is the access method INPE documents and the one this check actually read.

## CEMADEN

| | |
| --- | --- |
| What | Rain gauges and early warnings for floods and landslides. |
| Checked | `http://www2.cemaden.gov.br/mapainterativo/download/downpluv.php` returned HTTP 200 and an HTML page titled "Baixar dados". It is a form, not a dataset URL. |
| Other access | Municipal clients describe `sws.cemaden.gov.br` as a web service that needs a token issued after email registration. That call was not verified, and there is no anonymous key to try. |
| License | Federal disaster-monitoring data. Cite CEMADEN. |
| Update | Gauges are sub-hourly when the station is up. Alerts are event-driven. |
| Grain | Gauge or alert polygon/point. Not the 96 districts. |

**Flag: not programmatically accessible** for anonymous ingestion. Historical rain behind the download form is an interactive, email-gated flow (the public form has long required an email and a captcha). Do not bypass the captcha. Next step is to ask CEMADEN for API credentials, or to drop flood alerts from the MVP until a public feed exists.

## DataSUS SIH / OpenDataSUS

| | |
| --- | --- |
| What | SUS hospital admissions (AIH), used for respiratory hospitalizations. |
| Canonical files | `RD{UF}{YYMM}.dbc` (reduced AIH) under `ftp://ftp.datasus.gov.br/dissemin/publicos/SIHSUS/200801_/Dados/`. State of São Paulo files are `RDSP*.dbc`. A mirrored HTTPS layout is commonly `https://ftp.datasus.gov.br/dissemin/publicos/SIHSUS/200801_/Dados/`. |
| OpenDataSUS | `https://opendatasus.saude.gov.br/api/3/action/package_search` redirected to the HTML portal `https://dadosabertos.saude.gov.br/`. `https://dadosabertos.saude.gov.br/api/3/action/package_search` returned **404**. The classic CKAN API is not a working SIH endpoint on this check. |
| Live file fetch | The DATASUS HTTPS file host **timed out** on 2026-11-02. Access is therefore **not confirmed live** from this network. |
| Auth | Public files, no key, when the server answers. |
| License | Public SUS administrative data under Brazilian access-to-information rules. Cite DATASUS / Ministério da Saúde. Microdata has no patient identifier. |
| Update | Monthly competence. Practical lag is often about two months and can stretch toward six. Files are replaced as AIHs are reprocessed. |
| Grain | One row per AIH. Place is the municipality of residence (`MUNIC_RES`), not a district. Respiratory use is a CID-10 filter (chapter X, J00–J99, or a narrower list). The filter must be versioned in the worker; it is not in the file name. |
| Tooling | Files are DBC (compressed DBF). PySUS is the usual reader. |

Limitations that change the score: SUS-funded admissions only; a long stay can produce more than one AIH; coding follows billing practice; there is no district. A district health score cannot treat SIH as if it varied inside the city.

**Flag:** programmatic as a file download, not as a JSON API, and not verified live here. Do not point the pipeline at an unofficial third-party API that republishes SIH.

## IBGE Censo 2022, SIDRA, and meshes

| | |
| --- | --- |
| What | Population, and later income and vulnerability, from the 2022 census. |
| SIDRA API verified | `GET https://apisidra.ibge.gov.br/values/t/4709/n6/3550308/v/93/p/2022` |
| Result | Table **4709**, variable **93** "População residente", year **2022**, municipality **São Paulo (SP)**: **11,451,999** people. |
| Agregados v3 | `https://servicodados.ibge.gov.br/api/v3/agregados` is the documented service behind SIDRA. It **timed out** on 2026-11-02, so this check does not claim the v3 host was reachable. Use SIDRA until v3 answers. |
| Malhas API | Official v3 docs cover simplified meshes for regions, states, municipalities, and similar units, as SVG, GeoJSON, or TopoJSON. They do **not** serve census tracts or the PMSP districts. The malhas host timed out in the same way as agregados v3. |
| Intra-municipal files | `https://geoftp.ibge.gov.br/.../censo_2022/distritos/gpkg/UF/SP/SP_distritos_CD2022.gpkg` responded **HTTP 200** to HEAD. Census-tract meshes are on the same GeoFTP tree. |
| License | IBGE open data. Cite IBGE. |
| Update | Census 2022 is a stock. IBGE publishes revisions; it is not a weekly feed. |
| Grain | The verified table is the **municipality**. Several rendimento tables that were probed (7435, 7395, 7531) answered **400** "Parâmetro N6 incompatível com a tabela", so a table number is not enough: the territorial level has to be checked. Income at district grain is not a single SIDRA call. |

**Flag:** municipal aggregates are accessible. The 96-district social layer is **not** a direct SIDRA pull. Build it by joining census tracts to the GeoSampa polygons, or by using a PMSP table that is already published at district grain (Infocidade / GeoSampa), and record which one.

InfoDengue's `pop` on the verified week was about 12.2 million. That is their denominator. The census resident population is 11,451,999. Keep both, labeled.

## GeoSampa

Covered in the geography rule above.

| | |
| --- | --- |
| Metadata | GeoNetwork record for Distrito, Prefeitura de São Paulo. The record lists revisions of district lines through 2025 and into 2026. The WFS feature timestamp can be newer than the catalogue blurb. Trust the feature attribute `dt_atualizacao` once the layer is loaded (`max` over the 96 rows). |
| Auth | None for the public WFS that was called. |
| License | Municipal geographic data, publicly readable. Metadata on related layers uses an open-access constraint. Cite Prefeitura de São Paulo / GeoSampa / SMUL. Keep the WFS as the source rather than redistributing a frozen extract without a date. |
| Update | The agency revises lines when boundaries change. Not a daily feed. The sample row was updated on 2026-10-27. |
| Grain | District, subprefecture id, and the 5- and 8-region codes on the same feature. |

Other GeoSampa layers (vulnerability, flooding susceptibility, green areas) are candidates for indicators. Each one needs its own WFS `typeName` check. They are not loaded here.

## What the reference workers deliberately skip

## What the score job reads

Checked again on 2026-11-03. Only anonymous endpoints are used.

| Feed | Access | Grain | License / attribution |
| --- | --- | --- | --- |
| Open-Meteo forecast | `https://api.open-meteo.com/v1/forecast`, one call per batch of district points-on-surface, `past_days` up to 92 | District centroid, daily max temperature and precipitation | CC BY 4.0. "Weather data by Open-Meteo.com" |
| Open-Meteo Air Quality | `https://air-quality-api.open-meteo.com/v1/air-quality`, hourly PM2.5, PM10 and ozone averaged to a day | Same district points | CC BY 4.0. "Air quality data by Open-Meteo.com" |
| INPE BDQueimadas | Daily Brazil CSV `focos_diario_br_YYYYMMDD.csv` under `.../focos/csv/diario/Brasil/`. Rows with `municipio_id` 3550308 are joined with `ST_Within` | District polygon, daily count. Zero is a real zero | Free of charge. Cite INPE Programa Queimadas. No OSI license text |
| IBGE Censo 2022 | District universe zips `Agregados_por_distritos_basico_*` and `Agregados_por_distritos_caracteristicas_domicilio2_*`. 96 rows for municipality 3550308, matched to GeoSampa by normalized name | District. Population `v0001`, area `AREA_KM2`, occupied households `v0007`, sewer network `V00309` | IBGE open data. Cite IBGE, Censo Demográfico 2022 |
| InfoDengue | Existing municipal weekly API | Municipality, broadcast to every district and labeled as such | Cite Fiocruz/InfoDengue. No OSI license found |
| DataSUS SIH | No anonymous API confirmed | Not loaded | Respiratory hospitalizations stay missing. The health pillar says so and does not invent a proxy |

Census income is not in the 2022 universe district tables, so the income indicator stays missing. Density and sewer coverage are the social indicators that are present. A pillar is published when at least half of its indicator weight is present; the present weights are renormalized and the coverage is stored. Overall uses 0.40 / 0.35 / 0.25 only when all three pillars clear that bar.

`scripts/up-and-seed.sh` builds the local database and runs `--ecoguard.job=backfill`. Each feed is also its own job: `open-meteo-districts`, `inpe-fires`, `ibge-census`, `infodengue`, `geosampa-districts`, `risk-scores`. Set `ECOGUARD_SCHEDULE_CRON` on the ingestion process to run that sequence daily in `America/Sao_Paulo`. `ECOGUARD_BACKFILL_DAYS` defaults to 30.
