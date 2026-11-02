CREATE TABLE IF NOT EXISTS ops.job_runs (
  run_id uuid PRIMARY KEY,
  source text NOT NULL,
  status text NOT NULL,
  started_at timestamptz NOT NULL,
  finished_at timestamptz,
  extracted_count integer,
  raw_upserted_count integer,
  staging_upserted_count integer,
  error text,
  CONSTRAINT job_runs_status CHECK (status IN ('running', 'succeeded', 'failed'))
);

COMMENT ON TABLE ops.job_runs IS
  'One row per worker execution. Used later by the freshness monitor.';
