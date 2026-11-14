-- Cumulative dengue by district from COVISA arbovirus bulletins, section 4.
-- population is implied by the bulletin (cases / published incidence), not IBGE.

CREATE TABLE IF NOT EXISTS staging.covisa_dengue_districts (
  epi_year integer NOT NULL,
  epi_week integer NOT NULL,
  week_start date NOT NULL,
  week_end date NOT NULL,
  district_name text NOT NULL,
  cumulative_cases integer NOT NULL,
  incidence_per_100k numeric(8, 1) NOT NULL,
  implied_population numeric(12, 2),
  population_source text NOT NULL DEFAULT 'covisa_implied',
  PRIMARY KEY (epi_year, epi_week, district_name),
  CONSTRAINT covisa_population_source CHECK (population_source = 'covisa_implied')
);

COMMENT ON TABLE staging.covisa_dengue_districts IS
  'COVISA cumulative district dengue. use with source citation; licence not verified';

GRANT SELECT, INSERT, UPDATE ON staging.covisa_dengue_districts TO ecoguard_ingest;
GRANT SELECT ON staging.covisa_dengue_districts TO ecoguard_api;
