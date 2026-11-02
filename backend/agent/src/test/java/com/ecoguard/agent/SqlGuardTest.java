package com.ecoguard.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SqlGuardTest {
  @Test
  void allowsASingleAgentSelect() {
    assertEquals(
        "select config_key from agent.risk_score_config",
        SqlGuard.check("select config_key from agent.risk_score_config;"));
  }

  @Test
  void rejectsASecondStatementAndOtherSchemas() {
    assertThrows(IllegalArgumentException.class, () -> SqlGuard.check("select 1; select 2"));
    assertThrows(
        IllegalArgumentException.class,
        () -> SqlGuard.check("select * from staging.infodengue_alerts"));
    assertThrows(
        IllegalArgumentException.class, () -> SqlGuard.check("delete from agent.districts"));
    assertThrows(
        IllegalArgumentException.class,
        () -> SqlGuard.check("select 1 from agent.districts -- comment"));
  }
}
