-- InfoDengue is municipality x epidemiological week. São Paulo city is geocode 3550308.
-- It is not a district series. Recent weeks are revised by the provider (nowcast).
CREATE TABLE IF NOT EXISTS staging.infodengue_alerts (
  geocode integer NOT NULL,
  disease text NOT NULL,
  epi_week_start date NOT NULL,
  epi_year integer NOT NULL,
  epi_week integer NOT NULL,
  se integer NOT NULL,
  cases_notified integer,
  cases_estimated double precision,
  cases_estimated_min integer,
  cases_estimated_max integer,
  incidence_per_100k double precision,
  alert_level smallint,
  rt double precision,
  probability_rt_gt1 double precision,
  population double precision,
  municipality_name text,
  model_version text,
  source_record_id bigint,
  extracted_at timestamptz NOT NULL,
  loaded_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (geocode, disease, epi_week_start),
  CONSTRAINT infodengue_disease_known CHECK (
    disease IN ('dengue', 'chikungunya', 'zika')
  ),
  CONSTRAINT infodengue_week_in_range CHECK (epi_week BETWEEN 1 AND 53),
  CONSTRAINT infodengue_alert_level_known CHECK (
    alert_level IS NULL OR alert_level BETWEEN 1 AND 4
  )
);

COMMENT ON TABLE staging.infodengue_alerts IS
  'Weekly municipal arbovirus alert from InfoDengue. Natural key (geocode, disease, epi_week_start).';

-- Open-Meteo forecast for one city reference point, not a district grid.
CREATE TABLE IF NOT EXISTS staging.open_meteo_daily (
  location_id text NOT NULL,
  forecast_date date NOT NULL,
  requested_latitude double precision NOT NULL,
  requested_longitude double precision NOT NULL,
  grid_latitude double precision NOT NULL,
  grid_longitude double precision NOT NULL,
  elevation_m double precision,
  timezone text NOT NULL,
  utc_offset_seconds integer NOT NULL,
  weather_code integer,
  temperature_max_c double precision,
  temperature_min_c double precision,
  precipitation_sum_mm double precision,
  precipitation_hours double precision,
  wind_speed_max_kmh double precision,
  extracted_at timestamptz NOT NULL,
  loaded_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (location_id, forecast_date)
);

COMMENT ON TABLE staging.open_meteo_daily IS
  'Daily forecast rows. Re-fetches update the same location and date in place. Not observations.';
