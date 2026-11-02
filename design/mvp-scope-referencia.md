# EcoGuard — MVP scope (v0.1, Bucky)

## Goal
Portfolio-grade "mini real product": city risk intelligence for São Paulo (capital first, district level),
combining environmental, health and social data with an AI City Analyst. Shows architecture, cloud,
security, APIs, agents, LLMs, RAG, observability and data processing.

## MVP cut (what's IN)
- Scope: city of São Paulo, 96 districts (or subprefeituras), weekly + daily granularity.
- Environmental: weather/rain/heat (INMET + Open-Meteo forecast), air quality (CETESB QUALAR), fire hotspots (INPE BDQueimadas), flood/landslide alerts (CEMADEN).
- Health: dengue/arboviruses (InfoDengue API), respiratory hospitalizations (DataSUS SIH via OpenDataSUS, monthly lag).
- Social: population, income, vulnerability (IBGE Censo 2022 via SIDRA/malhas + SP GeoSampa layers).
- Composite risk score per district (environmental, health, social, overall) with transparent formula.
- AI City Analyst: chat answering questions with tools (SQL over curated tables, risk score lookup) + RAG over methodology docs and public bulletins; always cites sources/dates.
- Observability: Langfuse (LLM), OpenTelemetry + logs for pipelines, data freshness monitor.
- OUT of MVP: other cities, real-time streaming, user accounts, alerts by email/SMS, ML forecasting (phase 2).

## Architecture
- Ingestion: Python workers (scheduled jobs on Railway cron) per source -> raw (Supabase Storage) -> clean tables.
- Storage: Supabase Postgres + PostGIS (geometries) + pgvector (RAG docs).
- Processing: Python (pandas/polars) jobs compute daily indicators and risk scores; dbt-style SQL layers (raw, staging, marts).
- API: FastAPI (typed, OpenAPI), read-only public endpoints, rate limited.
- Agent: LangGraph/LangChain agent in the API with tools (text-to-SQL on marts with allowlist, get_risk(district, date), search_docs); OpenAI gpt-4o-mini.
- Frontend: Next.js + MapLibre GL (choropleth) + charts (Recharts/ECharts).
- Deploy: Railway (api, workers, web); Supabase managed DB.
- Security: read-only DB role for agent, SQL allowlist/guardrails, secrets in env, CSP, rate limits, prompt-injection mitigations.

## Screens (for mockups)
1. Overview dashboard: city-wide KPIs (overall risk, temp/heat, air quality index, dengue incidence, active alerts), trend sparklines, top 5 riskiest districts, data freshness badge.
2. Risk map: SP choropleth by district, layer toggle (Ambiental / Saúde / Social / Geral), date selector, legend.
3. District detail (drawer or page): scores breakdown, indicators over time, sources with last update.
4. AI City Analyst chat: question input, streamed answer with mini map/table/chart inline and source citations; suggested questions.
5. Alerts feed: active weather/flood/fire alerts with severity.
6. Data sources & methodology: list of sources, update frequency, risk score formula.
7. (Optional) Observability/status page: pipeline runs, freshness, LLM usage.
