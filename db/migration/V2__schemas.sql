CREATE SCHEMA IF NOT EXISTS raw;
CREATE SCHEMA IF NOT EXISTS staging;
CREATE SCHEMA IF NOT EXISTS marts;
CREATE SCHEMA IF NOT EXISTS ops;

COMMENT ON SCHEMA raw IS
  'Landed source payloads, one row per natural key. Not readable by the agent role.';
COMMENT ON SCHEMA staging IS
  'Typed, cleaned records. One row per natural key. Not readable by the agent role.';
COMMENT ON SCHEMA marts IS
  'Curated tables for the public API and the read-only agent role.';
COMMENT ON SCHEMA ops IS
  'Pipeline run metadata. Not readable by the agent role.';
