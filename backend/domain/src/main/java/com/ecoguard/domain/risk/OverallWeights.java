package com.ecoguard.domain.risk;

import java.math.BigDecimal;

/** Confirmed overall-score weights. The stored row is the configuration, not a constant. */
public record OverallWeights(
    BigDecimal environmental, BigDecimal health, BigDecimal social, String status) {

  public BigDecimal combine(
      BigDecimal environmentalScore, BigDecimal healthScore, BigDecimal socialScore) {
    if (!"confirmed".equals(status)) {
      throw new IllegalStateException("only a confirmed weight row can publish a score");
    }
    if (environmentalScore == null || healthScore == null || socialScore == null) {
      return null;
    }
    return environmental
        .multiply(environmentalScore)
        .add(health.multiply(healthScore))
        .add(social.multiply(socialScore));
  }
}
