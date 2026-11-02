package com.ecoguard.api.district;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DistrictReadModel {
  List<DistrictView> list();

  Optional<DistrictView> find(String code);

  RiskConfigView riskConfig();

  record DistrictView(
      String code,
      String name,
      String abbreviation,
      Integer subprefectureId,
      Double areaKm2,
      String geoJson,
      String source,
      Instant sourceUpdatedAt) {}

  record RiskConfigView(
      BigDecimal environmental, BigDecimal health, BigDecimal social, String status) {}
}
