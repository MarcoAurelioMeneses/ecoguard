-- Local only. The password matches the API default and is not a production secret.
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'ecoguard_agent') THEN
    CREATE ROLE ecoguard_agent LOGIN PASSWORD 'ecoguard_agent'
      NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT NOBYPASSRLS;
  ELSE
    ALTER ROLE ecoguard_agent WITH LOGIN PASSWORD 'ecoguard_agent'
      NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT NOBYPASSRLS;
  END IF;
END
$$;
