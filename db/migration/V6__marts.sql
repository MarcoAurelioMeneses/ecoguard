-- Canonical district geography is the live GeoSampa WFS layer
-- geoportal:distrito_municipal (EPSG:31983), transformed to EPSG:4326 on load.
-- Do not load the 2016 codigourbano/distritos-sp GeoJSON (properties ds_codigo, ds_nome).
-- That file is an outdated design input. This table starts empty on purpose.
CREATE TABLE IF NOT EXISTS marts.districts (
  district_code text PRIMARY KEY,
  name text NOT NULL,
  abbreviation text,
  subprefecture_id integer,
  area_km2 double precision,
  geom geometry(MultiPolygon, 4326) NOT NULL,
  source text NOT NULL,
  source_updated_at timestamptz,
  loaded_at timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT districts_official_source CHECK (
    source = 'geosampa:wfs:geoportal:distrito_municipal'
  )
);

COMMENT ON TABLE marts.districts IS
  '96 municipal districts of São Paulo from the current GeoSampa WFS layer. Empty until that load runs.';
COMMENT ON COLUMN marts.districts.source IS
  'Fixed to the official WFS layer so the 2016 GeoJSON cannot be inserted under another label.';

CREATE TABLE IF NOT EXISTS marts.risk_score_config (
  config_key text PRIMARY KEY,
  weight_environmental numeric(4, 3) NOT NULL,
  weight_health numeric(4, 3) NOT NULL,
  weight_social numeric(4, 3) NOT NULL,
  status text NOT NULL,
  notes text NOT NULL,
  updated_at timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT risk_weights_nonnegative CHECK (
    weight_environmental >= 0
    AND weight_health >= 0
    AND weight_social >= 0
  ),
  CONSTRAINT risk_weights_sum_to_one CHECK (
    abs(weight_environmental + weight_health + weight_social - 1) < 0.0005
  ),
  CONSTRAINT risk_config_status CHECK (status = 'confirmed')
);

COMMENT ON TABLE marts.risk_score_config IS
  'Configurable overall-score weights. The default row is the split Marco confirmed: 0.40 / 0.35 / 0.25.';

-- Scores are computed later. No example or mockup numbers are inserted.
CREATE TABLE IF NOT EXISTS marts.district_risk_scores (
  district_code text NOT NULL REFERENCES marts.districts (district_code),
  score_date date NOT NULL,
  config_key text NOT NULL REFERENCES marts.risk_score_config (config_key),
  environmental_score numeric(5, 2),
  health_score numeric(5, 2),
  social_score numeric(5, 2),
  overall_score numeric(5, 2),
  computed_at timestamptz NOT NULL,
  PRIMARY KEY (district_code, score_date, config_key),
  CONSTRAINT risk_scores_in_range CHECK (
    (environmental_score IS NULL OR environmental_score BETWEEN 0 AND 100)
    AND (health_score IS NULL OR health_score BETWEEN 0 AND 100)
    AND (social_score IS NULL OR social_score BETWEEN 0 AND 100)
    AND (overall_score IS NULL OR overall_score BETWEEN 0 AND 100)
  )
);

COMMENT ON TABLE marts.district_risk_scores IS
  'Computed district scores. Empty in this baseline. Mockup figures are not real and are not seeded.';

-- Embedding width 1536 matches the planned text-embedding-3-small default.
-- Change it with a migration if the embedding model changes. No documents are loaded here.
CREATE TABLE IF NOT EXISTS marts.documents (
  document_id uuid PRIMARY KEY,
  source text NOT NULL,
  title text NOT NULL,
  body text NOT NULL,
  published_on date,
  embedding vector(1536),
  loaded_at timestamptz NOT NULL DEFAULT now()
);

COMMENT ON TABLE marts.documents IS
  'RAG corpus placeholder. Methodology docs will be loaded later. No example text is inserted.';
