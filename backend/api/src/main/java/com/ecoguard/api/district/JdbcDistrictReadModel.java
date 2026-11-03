package com.ecoguard.api.district;

import com.ecoguard.domain.risk.ScoreRules;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
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
             s.environmental_coverage, s.health_coverage, s.social_coverage,
             s.environmental_complete, s.health_complete, s.social_complete, s.grain_note
      FROM marts.districts d
      LEFT JOIN LATERAL (
        SELECT *
        FROM marts.district_risk_scores
        WHERE district_code = d.district_code AND config_key = 'default'
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
                       max(heat_score) AS heat_score,
                       max(rain_score) AS rain_score,
                       max(dengue_score) AS dengue_score,
                       max(environmental_score) AS environmental_score,
                       max(health_score) AS health_score
                FROM marts.district_risk_scores
                WHERE config_key = 'default'
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
    ScoreSnapshot latest = list().stream().findFirst().map(DistrictView::score).orElse(null);
    return new CityScores(latest, series);
  }

  private static DistrictView map(java.sql.ResultSet rs, int row) throws java.sql.SQLException {
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

  private static ScoreSnapshot snapshot(java.sql.ResultSet rs) throws java.sql.SQLException {
    Date date = rs.getDate("score_date");
    if (date == null) {
      return null;
    }
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
        rs.getBigDecimal("social_coverage"),
        ScoreRules.riskClass(environmental),
        ScoreRules.riskClass(health),
        ScoreRules.riskClass(social),
        ScoreRules.riskClass(overall),
        rs.getBoolean("environmental_complete"),
        rs.getBoolean("health_complete"),
        rs.getBoolean("social_complete"),
        rs.getString("grain_note"));
  }
}
