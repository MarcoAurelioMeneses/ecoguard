package com.ecoguard.domain.geo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class Wgs84PointTest {
  @Test
  void storesLongitudeAsX() {
    var point = new Wgs84Point(-23.55, -46.63).toPoint();
    assertEquals(4326, point.getSRID());
    assertEquals(-46.63, point.getX());
    assertEquals(-23.55, point.getY());
  }
}
