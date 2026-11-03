-- Per-district observations from anonymous sources. Census income is not in the
-- 2022 universe district tables, so that column is intentionally absent.
CREATE TABLE IF NOT EXISTS staging.district_environment_daily (
  district_code text NOT NULL REFERENCES marts.districts (district_code),
  observation_date date NOT NULL,
  temperature_max_c double precision,
  precipitation_mm double precision,
  pm2_5_mean double precision,
  pm10_mean double precision,
  ozone_mean double precision,
  requested_latitude double precision NOT NULL,
  requested_longitude double precision NOT NULL,
  source text NOT NULL,
  extracted_at timestamptz NOT NULL,
  PRIMARY KEY (district_code, observation_date)
);

CREATE TABLE IF NOT EXISTS staging.district_fire_daily (
  district_code text NOT NULL REFERENCES marts.districts (district_code),
  observation_date date NOT NULL,
  hotspot_count integer NOT NULL,
  source text NOT NULL,
  extracted_at timestamptz NOT NULL,
  PRIMARY KEY (district_code, observation_date)
);

CREATE TABLE IF NOT EXISTS staging.district_census (
  district_code text PRIMARY KEY REFERENCES marts.districts (district_code),
  ibge_district_code text NOT NULL,
  district_name text NOT NULL,
  population double precision NOT NULL,
  area_km2 double precision NOT NULL,
  occupied_households double precision,
  sewage_network_households double precision,
  reference_year integer NOT NULL,
  source text NOT NULL,
  extracted_at timestamptz NOT NULL
);

COMMENT ON TABLE staging.district_environment_daily IS
  'Open-Meteo forecast and air-quality sampled at each district point-on-surface.';
COMMENT ON TABLE staging.district_fire_daily IS
  'INPE daily hotspot counts inside each district. Zero means the file was read and the join was empty.';
COMMENT ON TABLE staging.district_census IS
  'IBGE Censo 2022 universe aggregates published at district grain. Income is not in that release.';

GRANT SELECT, INSERT, UPDATE ON staging.district_environment_daily TO ecoguard_ingest;
GRANT SELECT, INSERT, UPDATE ON staging.district_fire_daily TO ecoguard_ingest;
GRANT SELECT, INSERT, UPDATE ON staging.district_census TO ecoguard_ingest;
GRANT SELECT ON staging.district_environment_daily TO ecoguard_api;
GRANT SELECT ON staging.district_fire_daily TO ecoguard_api;
GRANT SELECT ON staging.district_census TO ecoguard_api;
