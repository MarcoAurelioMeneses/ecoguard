package com.ecoguard.ingestion.persistence;

import com.ecoguard.domain.port.Ports.DistrictSampler;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
public class JdbcDistrictSampler implements DistrictSampler {
  private final JdbcClient jdbc;

  public JdbcDistrictSampler(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public List<SamplePoint> points() {
    return jdbc.sql(
            """
            SELECT district_code,
                   ST_Y(ST_PointOnSurface(geom)) AS lat,
                   ST_X(ST_PointOnSurface(geom)) AS lon
            FROM marts.districts
            ORDER BY district_code
            """)
        .query(
            (rs, row) ->
                new SamplePoint(
                    rs.getString("district_code"), rs.getDouble("lat"), rs.getDouble("lon")))
        .list();
  }
}
