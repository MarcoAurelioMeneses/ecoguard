package com.ecoguard.api.district;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
public class JdbcDistrictReadModel implements DistrictReadModel {
  private static final String SELECT =
      """
      SELECT district_code, name, abbreviation, subprefecture_id, area_km2,
             ST_AsGeoJSON(geom) AS geojson, source, source_updated_at
      FROM marts.districts
      """;

  private final JdbcClient jdbc;

  public JdbcDistrictReadModel(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public List<DistrictView> list() {
    return jdbc.sql(SELECT + " ORDER BY name").query(JdbcDistrictReadModel::map).list();
  }

  @Override
  public Optional<DistrictView> find(String code) {
    return jdbc.sql(SELECT + " WHERE district_code = :code")
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
            : rs.getTimestamp("source_updated_at").toInstant());
  }
}
