package com.ecoguard.api.district;

import com.ecoguard.application.DistrictReadModel;
import com.ecoguard.domain.risk.ScoreRules;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
public class JdbcDistrictReadModel implements DistrictReadModel {
  private static final String SELECT =
      """
      SELECT d.district_code, d.name, d.abbreviation, d.subprefecture_id, d.area_km2,
             ST_AsGeoJSON(d.geom) AS geojson, d.source, d.source_updated_at,
             s.score_date, s.environmental_score, s.health_score, s.social_score, s.overall_score,
             s.heat_score, s.rain_score, s.dengue_score,
             s.environmental_coverage, s.health_coverage,
             s.environmental_complete, s.health_complete, s.social_complete, s.grain_note
      FROM marts.districts d
      LEFT JOIN LATERAL (
        SELECT *
        FROM marts.district_risk_scores
        WHERE district_code = d.district_code
          AND config_key = 'default'
          AND score_date <= CURRENT_DATE
        ORDER BY score_date DESC
        LIMIT 1
      ) s ON true
      """;

  private final JdbcClient jdbc;

  public JdbcDistrictReadModel(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public List<DistrictView> list() {
    return jdbc.sql(SELECT + " ORDER BY d.name").query(JdbcDistrictReadModel::map).list();
  }

  @Override
  public Optional<DistrictView> find(String code) {
    return jdbc.sql(SELECT + " WHERE d.district_code = :code")
        .param("code", code)
        .query(JdbcDistrictReadModel::map)
        .optional();
  }

  @Override
  public RiskConfigView riskConfig() {
    return jdbc.sql(
            """
            SELECT weight_environmental, weight_health, weight_social, status
            FROM marts.risk_score_config
            WHERE config_key = 'default'
            """)
        .query(
            (rs, row) ->
                new RiskConfigView(
                    rs.getBigDecimal("weight_environmental"),
                    rs.getBigDecimal("weight_health"),
                    rs.getBigDecimal("weight_social"),
                    rs.getString("status")))
        .optional()
        .orElseThrow(() -> new IllegalStateException("risk config default row is missing"));
  }

  @Override
  public CityScores cityScores() {
    List<SeriesPoint> series =
        jdbc.sql(
                """
                SELECT score_date,
                       round(avg(heat_score), 2) AS heat_score,
                       round(avg(rain_score), 2) AS rain_score,
                       round(avg(dengue_score), 2) AS dengue_score,
                       round(avg(environmental_score), 2) AS environmental_score,
                       round(avg(health_score), 2) AS health_score
                FROM marts.district_risk_scores
                WHERE config_key = 'default'
                  AND score_date <= CURRENT_DATE
                GROUP BY score_date
                ORDER BY score_date
                """)
            .query(
                (rs, row) ->
                    new SeriesPoint(
                        rs.getDate("score_date").toLocalDate(),
                        rs.getBigDecimal("heat_score"),
                        rs.getBigDecimal("rain_score"),
                        rs.getBigDecimal("dengue_score"),
                        rs.getBigDecimal("environmental_score"),
                        rs.getBigDecimal("health_score")))
            .list();
    ScoreSnapshot latest =
        jdbc
            .sql(
                """
                SELECT max(score_date) AS score_date,
                       round(avg(environmental_score), 2) AS environmental_score,
                       round(avg(health_score), 2) AS health_score,
                       round(avg(social_score), 2) AS social_score,
                       round(avg(overall_score), 2) AS overall_score,
                       round(avg(heat_score), 2) AS heat_score,
                       round(avg(rain_score), 2) AS rain_score,
                       round(avg(dengue_score), 2) AS dengue_score,
                       round(avg(environmental_coverage), 3) AS environmental_coverage,
                       round(avg(health_coverage), 3) AS health_coverage,
                       COALESCE(bool_and(environmental_complete), false) AS environmental_complete,
                       COALESCE(bool_and(health_complete), false) AS health_complete,
                       COALESCE(bool_and(social_complete), false) AS social_complete
                FROM marts.district_risk_scores
                WHERE config_key = 'default'
                  AND score_date = (
                    SELECT max(score_date)
                    FROM marts.district_risk_scores
                    WHERE config_key = 'default' AND score_date <= CURRENT_DATE
                  )
                """)
            .query((rs, row) -> citySnapshot(rs))
            .list()
            .stream()
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    return new CityScores(latest, series);
  }

  private static DistrictView map(ResultSet rs, int row) throws SQLException {
    return new DistrictView(
        rs.getString("district_code"),
        rs.getString("name"),
        rs.getString("abbreviation"),
        (Integer) rs.getObject("subprefecture_id"),
        (Double) rs.getObject("area_km2"),
        rs.getString("geojson"),
        rs.getString("source"),
        rs.getTimestamp("source_updated_at") == null
            ? null
            : rs.getTimestamp("source_updated_at").toInstant(),
        snapshot(rs));
  }

  private static ScoreSnapshot citySnapshot(ResultSet rs) throws SQLException {
    if (rs.getDate("score_date") == null) {
      return null;
    }
    return scored(rs, "city mean of district scores");
  }

  private static ScoreSnapshot snapshot(ResultSet rs) throws SQLException {
    Date date = rs.getDate("score_date");
    if (date == null) {
      return null;
    }
    return scored(rs, rs.getString("grain_note"));
  }

  private static ScoreSnapshot scored(ResultSet rs, String grainNote) throws SQLException {
    Date date = rs.getDate("score_date");
    BigDecimal environmental = rs.getBigDecimal("environmental_score");
    BigDecimal health = rs.getBigDecimal("health_score");
    BigDecimal social = rs.getBigDecimal("social_score");
    BigDecimal overall = rs.getBigDecimal("overall_score");
    return new ScoreSnapshot(
        date.toLocalDate(),
        environmental,
        health,
        social,
        overall,
        rs.getBigDecimal("heat_score"),
        rs.getBigDecimal("rain_score"),
        rs.getBigDecimal("dengue_score"),
        rs.getBigDecimal("environmental_coverage"),
        rs.getBigDecimal("health_coverage"),
        ScoreRules.riskClass(environmental),
        ScoreRules.riskClass(health),
        ScoreRules.riskClass(social),
        ScoreRules.riskClass(overall),
        rs.getBoolean("environmental_complete"),
        rs.getBoolean("health_complete"),
        rs.getBoolean("social_complete"),
        grainNote);
  }
}
