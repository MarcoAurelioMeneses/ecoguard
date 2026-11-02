-- Grid point for the forecast row, built from the coordinates the collector already stores.
ALTER TABLE staging.open_meteo_daily
  ADD COLUMN IF NOT EXISTS geom geometry(Point, 4326);

-- The analyst searches curated documents, including the embedding, only through this view.
CREATE OR REPLACE VIEW agent.documents AS
SELECT
  document_id,
  source,
  title,
  body,
  published_on,
  embedding
FROM marts.documents;

GRANT SELECT ON agent.documents TO ecoguard_agent;
