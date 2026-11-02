package com.ecoguard.domain.geo;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

/** WGS84 point. Longitude is the X coordinate, matching PostGIS. */
public record Wgs84Point(double latitude, double longitude) {

  public Point toPoint() {
    GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
    return factory.createPoint(new Coordinate(longitude, latitude));
  }
}
