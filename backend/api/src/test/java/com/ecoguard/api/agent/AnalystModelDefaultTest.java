package com.ecoguard.api.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecoguard.agent.AnalystPricing;
import com.ecoguard.agent.CostGuard;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;

class AnalystModelDefaultTest {
  @Test
  void defaultModelIsGpt6LunaAtThePublishedRates() throws Exception {
    PropertySource<?> source =
        new YamlPropertySourceLoader()
            .load("application.yml", new ClassPathResource("application.yml"))
            .get(0);
    StandardEnvironment environment = new StandardEnvironment();
    environment.getPropertySources().addFirst(source);
    assertEquals("gpt-6-luna", environment.getRequiredProperty("ecoguard.analyst.model"));
    assertEquals("none", environment.getRequiredProperty("ecoguard.analyst.reasoning-effort"));
    assertEquals(
        "gpt-6-luna", environment.getRequiredProperty("spring.ai.openai.chat.options.model"));
    assertEquals(
        "none", environment.getRequiredProperty("spring.ai.openai.chat.options.reasoning-effort"));
    AnalystPricing pricing =
        Binder.get(environment).bind("ecoguard.analyst", AnalystPricing.class).get();
    assertEquals(0, new BigDecimal("0.10").compareTo(pricing.rates().getInput()));
    assertEquals(0, new BigDecimal("0.01").compareTo(pricing.rates().getCachedInput()));
    assertEquals(0, new BigDecimal("0.50").compareTo(pricing.rates().getOutput()));
    assertEquals(
        0,
        new BigDecimal("0.60")
            .compareTo(CostGuard.standardUsd(pricing.rates(), 1_000_000, 0, 1_000_000)));
    String yaml =
        new ClassPathResource("application.yml").getContentAsString(StandardCharsets.UTF_8);
    assertTrue(yaml.contains("temperature is accepted"));
  }
}
