-- Allowlist for the analyst. The role can read these views and nothing else.
-- Views run as the migration owner, so the agent does not need SELECT on marts.

CREATE SCHEMA IF NOT EXISTS agent;

CREATE OR REPLACE VIEW agent.risk_score_config AS
SELECT
  config_key,
  weight_environmental,
  weight_health,
  weight_social,
  status,
  notes
FROM marts.risk_score_config;

CREATE OR REPLACE VIEW agent.districts AS
SELECT
  district_code,
  name,
  abbreviation,
  subprefecture_id,
  area_km2,
  ST_AsGeoJSON(geom)::jsonb AS geom_geojson,
  source,
  source_updated_at
FROM marts.districts;

CREATE OR REPLACE VIEW agent.district_risk_scores AS
SELECT
  district_code,
  score_date,
  config_key,
  environmental_score,
  health_score,
  social_score,
  overall_score,
  computed_at
FROM marts.district_risk_scores;

COMMENT ON SCHEMA agent IS
  'Allowlisted read surface for ecoguard_agent. Views only.';

REVOKE ALL ON SCHEMA marts FROM ecoguard_agent;
REVOKE ALL ON ALL TABLES IN SCHEMA marts FROM ecoguard_agent;

GRANT USAGE ON SCHEMA agent TO ecoguard_agent;
GRANT SELECT ON ALL TABLES IN SCHEMA agent TO ecoguard_agent;
ALTER DEFAULT PRIVILEGES IN SCHEMA agent GRANT SELECT ON TABLES TO ecoguard_agent;
