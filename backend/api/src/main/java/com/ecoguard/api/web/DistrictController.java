package com.ecoguard.api.web;

import com.ecoguard.api.district.DistrictReadModel;
import com.ecoguard.api.district.DistrictReadModel.DistrictView;
import com.ecoguard.api.district.DistrictReadModel.RiskConfigView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class DistrictController {
  private final DistrictReadModel districts;
  private final ObjectMapper mapper;

  public DistrictController(DistrictReadModel districts, ObjectMapper mapper) {
    this.districts = districts;
    this.mapper = mapper;
  }

  @GetMapping("/districts")
  public FeatureCollection list() {
    return new FeatureCollection(
        "FeatureCollection", districts.list().stream().map(this::feature).toList());
  }

  @GetMapping("/districts/{code}")
  public Feature one(@PathVariable String code) {
    return districts
        .find(code)
        .map(this::feature)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "district not found"));
  }

  @GetMapping("/risk-config")
  public RiskConfigView riskConfig() {
    return districts.riskConfig();
  }

  @GetMapping("/scores/summary")
  public DistrictReadModel.CityScores scores() {
    return districts.cityScores();
  }

  private Feature feature(DistrictView district) {
    JsonNode geometry;
    try {
      geometry = mapper.readTree(district.geoJson());
    } catch (Exception ex) {
      throw new IllegalStateException("stored geometry is not GeoJSON", ex);
    }
    return new Feature(
        "Feature",
        new Properties(
            district.code(),
            district.name(),
            district.abbreviation(),
            district.subprefectureId(),
            district.areaKm2(),
            district.source(),
            district.sourceUpdatedAt(),
            district.score()),
        geometry);
  }

  public record FeatureCollection(String type, List<Feature> features) {}

  public record Feature(String type, Properties properties, JsonNode geometry) {}

  public record Properties(
      String code,
      String name,
      String abbreviation,
      Integer subprefectureId,
      Double areaKm2,
      String source,
      Instant sourceUpdatedAt,
      DistrictReadModel.ScoreSnapshot score) {}
}
