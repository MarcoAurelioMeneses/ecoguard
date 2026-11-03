package com.ecoguard.ingestion.score;

import com.ecoguard.domain.risk.OverallWeights;
import com.ecoguard.domain.risk.ScoreInputs.DengueReading;
import com.ecoguard.domain.risk.ScoreInputs.ForecastDay;
import com.ecoguard.domain.risk.ScoreInputs.PersistedScore;
import com.ecoguard.domain.risk.ScoreInputs.Readings;
import com.ecoguard.domain.risk.ScoreInputs.Writer;
import com.ecoguard.domain.risk.ScoreRules.DayScore;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
public class JdbcScoreStore implements Readings, Writer {
  private static final String UPSERT =
      """
      INSERT INTO marts.district_risk_scores (
        district_code, score_date, config_key,
        environmental_score, health_score, social_score, overall_score, computed_at,
        heat_score, rain_score, dengue_score,
        environmental_coverage, health_coverage, social_coverage,
        environmental_complete, health_complete, social_complete,
        missing_indicators, grain_note)
      VALUES (
        :district_code, :score_date, :config_key,
        :environmental_score, :health_score, :social_score, :overall_score, :computed_at,
        :heat_score, :rain_score, :dengue_score,
        :environmental_coverage, :health_coverage, :social_coverage,
        :environmental_complete, :health_complete, :social_complete,
        COALESCE(string_to_array(NULLIF(:missing, ''), ','), '{}'), :grain_note)
      ON CONFLICT (district_code, score_date, config_key) DO UPDATE SET
        environmental_score = EXCLUDED.environmental_score,
        health_score = EXCLUDED.health_score,
        social_score = EXCLUDED.social_score,
        overall_score = EXCLUDED.overall_score,
        computed_at = EXCLUDED.computed_at,
        heat_score = EXCLUDED.heat_score,
        rain_score = EXCLUDED.rain_score,
        dengue_score = EXCLUDED.dengue_score,
        environmental_coverage = EXCLUDED.environmental_coverage,
        health_coverage = EXCLUDED.health_coverage,
        social_coverage = EXCLUDED.social_coverage,
        environmental_complete = EXCLUDED.environmental_complete,
        health_complete = EXCLUDED.health_complete,
        social_complete = EXCLUDED.social_complete,
        missing_indicators = EXCLUDED.missing_indicators,
        grain_note = EXCLUDED.grain_note
      """;

  private static final String HISTORY =
      """
      INSERT INTO marts.district_risk_score_history (
        district_code, score_date, config_key,
        environmental_score, health_score, social_score, overall_score,
        heat_score, rain_score, dengue_score,
        environmental_coverage, health_coverage, social_coverage,
        environmental_complete, health_complete, social_complete,
        missing_indicators, grain_note, computed_at)
      VALUES (
        :district_code, :score_date, :config_key,
        :environmental_score, :health_score, :social_score, :overall_score,
        :heat_score, :rain_score, :dengue_score,
        :environmental_coverage, :health_coverage, :social_coverage,
        :environmental_complete, :health_complete, :social_complete,
        COALESCE(string_to_array(NULLIF(:missing, ''), ','), '{}'), :grain_note, :computed_at)
      """;

  private final JdbcClient jdbc;

  public JdbcScoreStore(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public List<String> districtCodes() {
    return jdbc.sql("SELECT district_code FROM marts.districts ORDER BY district_code")
        .query((rs, row) -> rs.getString(1))
        .list();
  }

  @Override
  public List<ForecastDay> forecasts(String locationId) {
    return jdbc.sql(
            """
            SELECT forecast_date, temperature_max_c, precipitation_sum_mm
            FROM staging.open_meteo_daily
            WHERE location_id = :location
            ORDER BY forecast_date
            """)
        .param("location", locationId)
        .query(
            (rs, row) ->
                new ForecastDay(
                    rs.getDate("forecast_date").toLocalDate(),
                    rs.getBigDecimal("temperature_max_c"),
                    rs.getBigDecimal("precipitation_sum_mm")))
        .list();
  }

  @Override
  public DengueReading latestDengue(int geocode, String disease) {
    return jdbc.sql(
            """
            SELECT epi_week_start, alert_level, incidence_per_100k
            FROM staging.infodengue_alerts
            WHERE geocode = :geocode AND disease = :disease
            ORDER BY epi_week_start DESC
            LIMIT 1
            """)
        .param("geocode", geocode)
        .param("disease", disease)
        .query(
            (rs, row) ->
                new DengueReading(
                    rs.getDate("epi_week_start").toLocalDate(),
                    (Integer) rs.getObject("alert_level"),
                    rs.getBigDecimal("incidence_per_100k")))
        .optional()
        .orElse(null);
  }

  @Override
  public OverallWeights weights(String configKey) {
    return jdbc.sql(
            """
            SELECT weight_environmental, weight_health, weight_social, status
            FROM marts.risk_score_config
            WHERE config_key = :key
            """)
        .param("key", configKey)
        .query(
            (rs, row) ->
                new OverallWeights(
                    rs.getBigDecimal("weight_environmental"),
                    rs.getBigDecimal("weight_health"),
                    rs.getBigDecimal("weight_social"),
                    rs.getString("status")))
        .optional()
        .orElseThrow(() -> new IllegalStateException("risk config row is missing: " + configKey));
  }

  @Override
  public int upsert(List<PersistedScore> scores) {
    return write(UPSERT, scores);
  }

  @Override
  public int appendHistory(List<PersistedScore> scores) {
    return write(HISTORY, scores);
  }

  private int write(String sql, List<PersistedScore> scores) {
    int written = 0;
    for (PersistedScore score : scores) {
      DayScore day = score.score();
      written += bind(jdbc.sql(sql), score, day).update();
    }
    return written;
  }

  private static org.springframework.jdbc.core.simple.JdbcClient.StatementSpec bind(
      org.springframework.jdbc.core.simple.JdbcClient.StatementSpec spec,
      PersistedScore score,
      DayScore day) {
    List<String> missing = new java.util.ArrayList<>();
    missing.addAll(day.environmental().missing());
    missing.addAll(day.health().missing());
    missing.addAll(day.social().missing());
    return spec.param("district_code", score.districtCode())
        .param("score_date", Date.valueOf(score.scoreDate()))
        .param("config_key", score.configKey())
        .param("environmental_score", day.environmental().score())
        .param("health_score", day.health().score())
        .param("social_score", day.social().score())
        .param("overall_score", day.overall())
        .param("computed_at", Timestamp.from(score.computedAt()))
        .param("heat_score", day.heat())
        .param("rain_score", day.rain())
        .param("dengue_score", day.dengue())
        .param("environmental_coverage", day.environmental().coverage())
        .param("health_coverage", day.health().coverage())
        .param("social_coverage", day.social().coverage())
        .param("environmental_complete", day.environmental().complete())
        .param("health_complete", day.health().complete())
        .param("social_complete", day.social().complete())
        .param("missing", missing.stream().distinct().collect(Collectors.joining(",")))
        .param("grain_note", score.grainNote());
  }
}
