package com.ecoguard.api.district;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DistrictReadModel {
  List<DistrictView> list();

  Optional<DistrictView> find(String code);

  RiskConfigView riskConfig();

  CityScores cityScores();

  record DistrictView(
      String code,
      String name,
      String abbreviation,
      Integer subprefectureId,
      Double areaKm2,
      String geoJson,
      String source,
      Instant sourceUpdatedAt,
      ScoreSnapshot score) {}

  record ScoreSnapshot(
      LocalDate scoreDate,
      BigDecimal environmental,
      BigDecimal health,
      BigDecimal social,
      BigDecimal overall,
      BigDecimal heat,
      BigDecimal rain,
      BigDecimal dengue,
      BigDecimal environmentalCoverage,
      BigDecimal healthCoverage,
      BigDecimal socialCoverage,
      String environmentalClass,
      String healthClass,
      String socialClass,
      String overallClass,
      boolean environmentalComplete,
      boolean healthComplete,
      boolean socialComplete,
      String grainNote) {}

  record SeriesPoint(
      LocalDate scoreDate,
      BigDecimal heat,
      BigDecimal rain,
      BigDecimal dengue,
      BigDecimal environmental,
      BigDecimal health) {}

  record CityScores(ScoreSnapshot latest, List<SeriesPoint> series) {}

  record RiskConfigView(
      BigDecimal environmental, BigDecimal health, BigDecimal social, String status) {}
}
