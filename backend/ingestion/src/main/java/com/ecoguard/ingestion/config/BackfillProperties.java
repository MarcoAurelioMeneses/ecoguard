package com.ecoguard.ingestion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ecoguard.backfill")
public record BackfillProperties(
    int days,
    String airQualityBaseUrl,
    String inpeDailyCsv,
    String ibgeBasicoUrl,
    String ibgeDomicileUrl) {}
