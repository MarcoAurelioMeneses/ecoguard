package com.ecoguard.api.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecoguard.agent.CostGuard;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class AnalystModelDefaultTest {
  @Test
  void defaultModelIsGpt6Luna() throws Exception {
    String yaml =
        new ClassPathResource("application.yml").getContentAsString(StandardCharsets.UTF_8);
    assertTrue(
        yaml.contains("model: ${OPENAI_MODEL:" + CostGuard.DEFAULT_MODEL + "}"),
        "the chat model must default to the cost-guard model");
    assertEquals("gpt-6-luna", CostGuard.DEFAULT_MODEL);
    assertEquals(0, new BigDecimal("0.10").compareTo(CostGuard.SHORT_INPUT_USD_PER_MILLION));
    assertEquals(0, new BigDecimal("0.50").compareTo(CostGuard.SHORT_OUTPUT_USD_PER_MILLION));
    assertEquals(0, new BigDecimal("0.01").compareTo(CostGuard.SHORT_CACHED_INPUT_USD_PER_MILLION));
    assertEquals(
        0, new BigDecimal("0.60").compareTo(CostGuard.standardUsd(1_000_000, 0, 1_000_000)));
  }
}
