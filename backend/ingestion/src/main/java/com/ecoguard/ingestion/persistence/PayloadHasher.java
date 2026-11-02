package com.ecoguard.ingestion.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

public final class PayloadHasher {
  private static final ObjectMapper MAPPER =
      new ObjectMapper().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

  private PayloadHasher() {}

  public static String sha256(Map<String, Object> payload) {
    try {
      byte[] json = MAPPER.writeValueAsBytes(payload);
      byte[] digest = MessageDigest.getInstance("SHA-256").digest(json);
      return HexFormat.of().formatHex(digest);
    } catch (Exception ex) {
      throw new IllegalArgumentException("payload is not JSON-safe", ex);
    }
  }

  public static String json(Map<String, Object> payload) {
    try {
      return new String(MAPPER.writeValueAsBytes(payload), StandardCharsets.UTF_8);
    } catch (Exception ex) {
      throw new IllegalArgumentException("payload is not JSON-safe", ex);
    }
  }
}
