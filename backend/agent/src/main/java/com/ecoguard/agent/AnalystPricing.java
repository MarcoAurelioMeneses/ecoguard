package com.ecoguard.agent;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Analyst model id, Chat Completions reasoning effort, and per-model token prices. */
@ConfigurationProperties(prefix = "ecoguard.analyst")
public class AnalystPricing {
  private String model = "gpt-6-luna";
  private String reasoningEffort = "none";
  private Map<String, Rates> pricing = new LinkedHashMap<>();

  public Rates rates() {
    Rates rates = pricing.get(model);
    if (rates == null) {
      throw new IllegalStateException("no ecoguard.analyst.pricing entry for " + model);
    }
    return rates;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public String getReasoningEffort() {
    return reasoningEffort;
  }

  public void setReasoningEffort(String reasoningEffort) {
    this.reasoningEffort = reasoningEffort;
  }

  public Map<String, Rates> getPricing() {
    return pricing;
  }

  public void setPricing(Map<String, Rates> pricing) {
    this.pricing = pricing;
  }

  /** USD per 1,000,000 tokens. */
  public static class Rates {
    private BigDecimal input;
    private BigDecimal cachedInput;
    private BigDecimal output;

    public BigDecimal getInput() {
      return input;
    }

    public void setInput(BigDecimal input) {
      this.input = input;
    }

    public BigDecimal getCachedInput() {
      return cachedInput;
    }

    public void setCachedInput(BigDecimal cachedInput) {
      this.cachedInput = cachedInput;
    }

    public BigDecimal getOutput() {
      return output;
    }

    public void setOutput(BigDecimal output) {
      this.output = output;
    }
  }
}
