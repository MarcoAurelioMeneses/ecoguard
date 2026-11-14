package com.ecoguard.agent;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class AnalystConfigurationTest {
  private final ApplicationContextRunner runner =
      new ApplicationContextRunner().withUserConfiguration(AnalystConfiguration.class);

  @Test
  void blankKeyDoesNotBuildTheModelAndAnswers503ViaIllegalState() {
    runner
        .withPropertyValues("ecoguard.openai.api-key=")
        .run(
            context -> {
              CityAnalyst analyst = context.getBean(CityAnalyst.class);
              assertThrows(IllegalStateException.class, () -> analyst.answer("chuva"));
            });
  }
}
