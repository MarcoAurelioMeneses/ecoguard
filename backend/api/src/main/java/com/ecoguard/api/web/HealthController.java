package com.ecoguard.api.web;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
  private final String environment;

  public HealthController(@Value("${ecoguard.environment:local}") String environment) {
    this.environment = environment;
  }

  @GetMapping("/health")
  public Map<String, String> health() {
    return Map.of(
        "status", "ok",
        "service", "ecoguard-api",
        "version", "0.1.0",
        "environment", environment);
  }

  @GetMapping("/")
  public Map<String, String> root() {
    return Map.of(
        "service", "ecoguard-api",
        "health", "/health",
        "docs", "/docs",
        "openapi", "/openapi.json",
        "note", "Foundation build. Mockup figures are not data.");
  }
}
