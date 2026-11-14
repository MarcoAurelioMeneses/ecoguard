package com.ecoguard.agent;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Standard-tier prices for the analyst model, in USD per 1,000,000 tokens.
 *
 * <p>Short context is an input of at most 272,000 tokens. Figures are the gpt-6-luna row of the
 * OpenAI pricing page: https://developers.openai.com/api/docs/pricing
 */
public final class CostGuard {
  public static final String DEFAULT_MODEL = "gpt-6-luna";

  public static final BigDecimal SHORT_INPUT_USD_PER_MILLION = new BigDecimal("0.10");
  public static final BigDecimal SHORT_OUTPUT_USD_PER_MILLION = new BigDecimal("0.50");
  public static final BigDecimal SHORT_CACHED_INPUT_USD_PER_MILLION = new BigDecimal("0.01");
  public static final BigDecimal SHORT_CACHE_WRITE_30M_USD_PER_MILLION = new BigDecimal("0.125");
  public static final BigDecimal SHORT_CACHE_WRITE_12H_USD_PER_MILLION = new BigDecimal("0.25");

  public static final BigDecimal LONG_INPUT_USD_PER_MILLION = new BigDecimal("0.20");
  public static final BigDecimal LONG_OUTPUT_USD_PER_MILLION = new BigDecimal("0.75");
  public static final BigDecimal LONG_CACHED_INPUT_USD_PER_MILLION = new BigDecimal("0.02");
  public static final BigDecimal LONG_CACHE_WRITE_30M_USD_PER_MILLION = new BigDecimal("0.25");
  public static final BigDecimal LONG_CACHE_WRITE_12H_USD_PER_MILLION = new BigDecimal("0.50");

  private static final BigDecimal MILLION = new BigDecimal("1000000");

  private CostGuard() {}

  /** Short-context Standard estimate. Cached input is billed separately from uncached input. */
  public static BigDecimal standardUsd(
      long inputTokens, long cachedInputTokens, long outputTokens) {
    return usd(SHORT_INPUT_USD_PER_MILLION, inputTokens)
        .add(usd(SHORT_CACHED_INPUT_USD_PER_MILLION, cachedInputTokens))
        .add(usd(SHORT_OUTPUT_USD_PER_MILLION, outputTokens));
  }

  private static BigDecimal usd(BigDecimal perMillion, long tokens) {
    return perMillion.multiply(BigDecimal.valueOf(tokens)).divide(MILLION, 8, RoundingMode.HALF_UP);
  }
}
