-- Coverage and indicator columns for the score job. History keeps every computation.
-- Current rows stay unique on district, date, and config so a rerun updates in place.
ALTER TABLE marts.district_risk_scores
  ADD COLUMN heat_score numeric(5, 2),
  ADD COLUMN rain_score numeric(5, 2),
  ADD COLUMN dengue_score numeric(5, 2),
  ADD COLUMN environmental_coverage numeric(4, 3),
  ADD COLUMN health_coverage numeric(4, 3),
  ADD COLUMN social_coverage numeric(4, 3),
  ADD COLUMN environmental_complete boolean NOT NULL DEFAULT false,
  ADD COLUMN health_complete boolean NOT NULL DEFAULT false,
  ADD COLUMN social_complete boolean NOT NULL DEFAULT false,
  ADD COLUMN missing_indicators text[] NOT NULL DEFAULT '{}',
  ADD COLUMN grain_note text NOT NULL DEFAULT '';

CREATE TABLE IF NOT EXISTS marts.district_risk_score_history (
  history_id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  district_code text NOT NULL,
  score_date date NOT NULL,
  config_key text NOT NULL,
  environmental_score numeric(5, 2),
  health_score numeric(5, 2),
  social_score numeric(5, 2),
  overall_score numeric(5, 2),
  heat_score numeric(5, 2),
  rain_score numeric(5, 2),
  dengue_score numeric(5, 2),
  environmental_coverage numeric(4, 3),
  health_coverage numeric(4, 3),
  social_coverage numeric(4, 3),
  environmental_complete boolean NOT NULL,
  health_complete boolean NOT NULL,
  social_complete boolean NOT NULL,
  missing_indicators text[] NOT NULL,
  grain_note text NOT NULL,
  computed_at timestamptz NOT NULL
);

GRANT SELECT, INSERT, UPDATE ON marts.district_risk_scores TO ecoguard_ingest;
GRANT SELECT, INSERT ON marts.district_risk_score_history TO ecoguard_ingest;
GRANT USAGE, SELECT ON SEQUENCE marts.district_risk_score_history_history_id_seq TO ecoguard_ingest;
GRANT SELECT ON marts.district_risk_scores TO ecoguard_api;
GRANT SELECT ON marts.district_risk_score_history TO ecoguard_api;
