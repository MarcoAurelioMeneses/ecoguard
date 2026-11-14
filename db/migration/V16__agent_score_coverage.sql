-- The analyst allowlist follows the coverage columns added in V12.
-- CREATE OR REPLACE cannot insert columns ahead of computed_at, so the view is rebuilt.

DROP VIEW IF EXISTS agent.district_risk_scores;

CREATE VIEW agent.district_risk_scores AS
SELECT
  district_code,
  score_date,
  config_key,
  environmental_score,
  health_score,
  social_score,
  overall_score,
  heat_score,
  rain_score,
  dengue_score,
  environmental_coverage,
  health_coverage,
  social_coverage,
  environmental_complete,
  health_complete,
  social_complete,
  computed_at
FROM marts.district_risk_scores;

GRANT SELECT ON agent.district_risk_scores TO ecoguard_agent;
