package com.ecoguard.ingestion.collector;

import com.ecoguard.domain.ingestion.Records.District;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** GeoJSON from GeoSampa WFS layer geoportal:distrito_municipal. */
public final class GeoSampaDistrictParser {
  static final String TYPE_NAME = "geoportal:distrito_municipal";

  private GeoSampaDistrictParser() {}

  public static List<District> parse(JsonNode collection) {
    if (collection == null || !collection.path("features").isArray()) {
      throw new IllegalArgumentException("GeoSampa response must be a FeatureCollection");
    }
    int srid = srid(collection);
    List<District> districts = new ArrayList<>();
    for (JsonNode feature : collection.path("features")) {
      JsonNode properties = feature.path("properties");
      String code = text(properties, "cd_distrito_municipal");
      String name = text(properties, "nm_distrito_municipal");
      if (code == null || name == null) {
        throw new IllegalArgumentException("district feature is missing code or name");
      }
      districts.add(
          new District(
              code,
              name,
              text(properties, "sg_distrito_municipal"),
              integer(properties, "cd_identificador_subprefeitura"),
              decimal(properties, "qt_area_quilometro"),
              toMultiPolygonWkt(feature.path("geometry")),
              srid,
              instant(properties.path("dt_atualizacao").asText(null))));
    }
    return List.copyOf(districts);
  }

  static int srid(JsonNode collection) {
    String name = collection.path("crs").path("properties").path("name").asText("");
    if (name.endsWith("4326") || name.contains("CRS84")) {
      return 4326;
    }
    return 31983;
  }

  static String toMultiPolygonWkt(JsonNode geometry) {
    String type = geometry.path("type").asText();
    JsonNode coordinates = geometry.path("coordinates");
    if ("Polygon".equals(type)) {
      return "MULTIPOLYGON(" + rings(coordinates) + ")";
    }
    if ("MultiPolygon".equals(type)) {
      StringBuilder wkt = new StringBuilder("MULTIPOLYGON(");
      for (int i = 0; i < coordinates.size(); i++) {
        if (i > 0) {
          wkt.append(',');
        }
        wkt.append(rings(coordinates.get(i)));
      }
      return wkt.append(')').toString();
    }
    throw new IllegalArgumentException("unsupported geometry " + type);
  }

  private static String rings(JsonNode rings) {
    StringBuilder wkt = new StringBuilder("(");
    for (int i = 0; i < rings.size(); i++) {
      if (i > 0) {
        wkt.append(',');
      }
      wkt.append('(');
      JsonNode ring = rings.get(i);
      for (int p = 0; p < ring.size(); p++) {
        if (p > 0) {
          wkt.append(',');
        }
        wkt.append(ring.get(p).get(0).asDouble()).append(' ').append(ring.get(p).get(1).asDouble());
      }
      wkt.append(')');
    }
    return wkt.append(')').toString();
  }

  private static String text(JsonNode node, String field) {
    JsonNode value = node.get(field);
    if (value == null || value.isNull()) {
      return null;
    }
    String text = value.asText().trim();
    return text.isEmpty() ? null : text;
  }

  private static Integer integer(JsonNode node, String field) {
    JsonNode value = node.get(field);
    if (value == null || value.isNull() || value.asText().isBlank()) {
      return null;
    }
    return value.asInt();
  }

  private static Double decimal(JsonNode node, String field) {
    JsonNode value = node.get(field);
    if (value == null || value.isNull() || value.asText().isBlank()) {
      return null;
    }
    return value.asDouble();
  }

  private static Instant instant(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return Instant.parse(value);
  }
}
