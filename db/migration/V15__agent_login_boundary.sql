-- The analyst connects as this role. It is not a member of the table owner, so
-- set_config('role', session_user) cannot climb back to that owner.
-- A password is assigned out of band. Local compose does that in docker-entrypoint-initdb.d.
ALTER ROLE ecoguard_agent WITH NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT NOBYPASSRLS;

REVOKE ALL ON SCHEMA public FROM ecoguard_agent;
REVOKE ALL ON SCHEMA raw FROM ecoguard_agent;
REVOKE ALL ON SCHEMA staging FROM ecoguard_agent;
REVOKE ALL ON SCHEMA marts FROM ecoguard_agent;
REVOKE ALL ON SCHEMA ops FROM ecoguard_agent;

COMMENT ON ROLE ecoguard_agent IS
  'Analyst login. SELECT on agent views only. Not a member of the migration owner.';
