package com.ecoguard.domain.analytics;

import java.util.Map;
import java.util.Set;

/** Event names shared with the frontend tracker and docs/analytics.md. */
public final class AnalyticsTaxonomy {
  public static final String SCREEN_VIEWED = "screen_viewed";
  public static final String MAP_LAYER_VIEWED = "map_layer_viewed";
  public static final String DISTRICT_VIEWED = "district_viewed";
  public static final String ANALYST_QUESTION_ASKED = "analyst_question_asked";

  public static final Set<String> ALLOWED_EVENTS =
      Set.of(SCREEN_VIEWED, MAP_LAYER_VIEWED, DISTRICT_VIEWED, ANALYST_QUESTION_ASKED);

  public static final Set<String> BANNED_PROPERTY_KEYS =
      Set.of("email", "name", "ip", "user_id", "question", "prompt", "sql", "address");

  private AnalyticsTaxonomy() {}

  public static void check(String event, Map<String, ?> properties) {
    if (!ALLOWED_EVENTS.contains(event)) {
      throw new IllegalArgumentException("unknown analytics event: " + event);
    }
    for (String key : properties.keySet()) {
      if (BANNED_PROPERTY_KEYS.contains(key)) {
        throw new IllegalArgumentException("analytics property is not allowed: " + key);
      }
    }
  }

  public static String questionLengthBucket(String question) {
    int length = question == null ? 0 : question.trim().length();
    if (length <= 40) {
      return "1-40";
    }
    if (length <= 120) {
      return "41-120";
    }
    if (length <= 400) {
      return "121-400";
    }
    return "401+";
  }
}
