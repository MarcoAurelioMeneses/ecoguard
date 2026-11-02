CREATE TABLE IF NOT EXISTS raw.source_payloads (
  source text NOT NULL,
  external_key text NOT NULL,
  payload jsonb NOT NULL,
  payload_sha256 text NOT NULL,
  extracted_at timestamptz NOT NULL,
  loaded_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (source, external_key)
);

COMMENT ON TABLE raw.source_payloads IS
  'Idempotent landing table. Re-runs update the same natural key instead of inserting a duplicate.';
