-- A nightly backfill recomputes the same district-days. History keeps one row per
-- district and date and updates it, instead of inserting another copy.
DELETE FROM marts.district_risk_score_history AS older
WHERE older.history_id NOT IN (
  SELECT DISTINCT ON (district_code, score_date) history_id
  FROM marts.district_risk_score_history
  ORDER BY district_code, score_date, computed_at DESC, history_id DESC
);

ALTER TABLE marts.district_risk_score_history
  ADD CONSTRAINT district_risk_score_history_district_date_key
  UNIQUE (district_code, score_date);

GRANT UPDATE ON marts.district_risk_score_history TO ecoguard_ingest;
