package com.ecoguard.api.web;

import com.ecoguard.agent.CityAnalyst;
import com.ecoguard.domain.analytics.AnalyticsTaxonomy;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AnalystController {
  private final CityAnalyst analyst;

  public AnalystController(CityAnalyst analyst) {
    this.analyst = analyst;
  }

  @PostMapping("/analyst/questions")
  public Map<String, String> ask(@Valid @RequestBody QuestionRequest request) {
    AnalyticsTaxonomy.check(
        AnalyticsTaxonomy.ANALYST_QUESTION_ASKED,
        Map.of(
            "screen",
            "04",
            "length_bucket",
            AnalyticsTaxonomy.questionLengthBucket(request.question())));
    try {
      String answer = analyst.answer(request.question());
      return Map.of("answer", answer == null ? "" : answer);
    } catch (IllegalStateException ex) {
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }
  }

  public record QuestionRequest(@NotBlank @Size(max = 2000) String question) {}
}
