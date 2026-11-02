package com.ecoguard.domain.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.Test;

class AnalyticsTaxonomyTest {
  @Test
  void rejectsQuestionText() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            AnalyticsTaxonomy.check(
                AnalyticsTaxonomy.ANALYST_QUESTION_ASKED, Map.of("question", "raw")));
  }

  @Test
  void bucketsQuestionLengthWithoutKeepingTheText() {
    assertEquals("1-40", AnalyticsTaxonomy.questionLengthBucket("short"));
    assertEquals("401+", AnalyticsTaxonomy.questionLengthBucket("x".repeat(401)));
  }
}
