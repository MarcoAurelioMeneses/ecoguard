-- Roles are NOLOGIN until operations sets a password out of band.
-- Do not put passwords, tokens, or connection strings in migrations.

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'ecoguard_ingest') THEN
    CREATE ROLE ecoguard_ingest NOLOGIN;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'ecoguard_api') THEN
    CREATE ROLE ecoguard_api NOLOGIN;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'ecoguard_agent') THEN
    CREATE ROLE ecoguard_agent NOLOGIN;
  END IF;
END
$$;

COMMENT ON ROLE ecoguard_ingest IS
  'Workers. Insert and update raw, staging, marts, and ops.';
COMMENT ON ROLE ecoguard_api IS
  'FastAPI. Read staging and marts. No writes.';
COMMENT ON ROLE ecoguard_agent IS
  'LangGraph analyst. Read-only on the agent schema views. No raw, staging, marts, or ops.';

GRANT USAGE ON SCHEMA raw, staging, marts, ops TO ecoguard_ingest;
GRANT USAGE ON SCHEMA staging, marts TO ecoguard_api;

ALTER DEFAULT PRIVILEGES IN SCHEMA raw
  GRANT SELECT, INSERT, UPDATE ON TABLES TO ecoguard_ingest;
ALTER DEFAULT PRIVILEGES IN SCHEMA staging
  GRANT SELECT, INSERT, UPDATE ON TABLES TO ecoguard_ingest;
ALTER DEFAULT PRIVILEGES IN SCHEMA marts
  GRANT SELECT, INSERT, UPDATE ON TABLES TO ecoguard_ingest;
ALTER DEFAULT PRIVILEGES IN SCHEMA ops
  GRANT SELECT, INSERT, UPDATE ON TABLES TO ecoguard_ingest;

ALTER DEFAULT PRIVILEGES IN SCHEMA staging GRANT SELECT ON TABLES TO ecoguard_api;
ALTER DEFAULT PRIVILEGES IN SCHEMA marts GRANT SELECT ON TABLES TO ecoguard_api;
