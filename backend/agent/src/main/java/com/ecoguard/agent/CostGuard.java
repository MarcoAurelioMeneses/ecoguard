package com.ecoguard.agent;

import com.ecoguard.agent.AnalystPricing.Rates;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Turns token counts into USD using {@code ecoguard.analyst.pricing}. */
public final class CostGuard {
  private static final BigDecimal MILLION = new BigDecimal("1000000");

  private CostGuard() {}

  public static BigDecimal standardUsd(
      Rates rates, long inputTokens, long cachedInputTokens, long outputTokens) {
    return usd(rates.getInput(), inputTokens)
        .add(usd(rates.getCachedInput(), cachedInputTokens))
        .add(usd(rates.getOutput(), outputTokens));
  }

  private static BigDecimal usd(BigDecimal perMillion, long tokens) {
    return perMillion.multiply(BigDecimal.valueOf(tokens)).divide(MILLION, 8, RoundingMode.HALF_UP);
  }
}
