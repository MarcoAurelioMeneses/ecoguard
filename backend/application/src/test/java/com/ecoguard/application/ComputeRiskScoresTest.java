package com.ecoguard.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.ecoguard.domain.risk.OverallWeights;
import com.ecoguard.domain.risk.ScoreInputs.DengueReading;
import com.ecoguard.domain.risk.ScoreInputs.DistrictDay;
import com.ecoguard.domain.risk.ScoreInputs.ForecastDay;
import com.ecoguard.domain.risk.ScoreInputs.PersistedScore;
import com.ecoguard.domain.risk.ScoreInputs.Readings;
import com.ecoguard.domain.risk.ScoreInputs.Writer;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ComputeRiskScoresTest {
  @Test
  void usesTheDengueWeekThatContainsEachDate() {
    List<DengueReading> weeks =
        List.of(
            new DengueReading(LocalDate.parse("2026-10-04"), 1, null),
            new DengueReading(LocalDate.parse("2026-10-11"), 4, null));
    List<DistrictDay> days =
        List.of(day("2026-10-04"), day("2026-10-10"), day("2026-10-11"), day("2026-10-18"));
    Memory memory = new Memory(weeks, days);
    new ComputeRiskScores()
        .execute(
            memory, memory, Instant.parse("2026-11-03T00:00:00Z"), "default", 3550308, "dengue");
    assertEquals(new BigDecimal("15.00"), memory.written.get(0).score().dengue());
    assertEquals(new BigDecimal("15.00"), memory.written.get(1).score().dengue());
    assertEquals(new BigDecimal("100.00"), memory.written.get(2).score().dengue());
    assertNull(memory.written.get(3).score().dengue());
  }

  private static DistrictDay day(String date) {
    return new DistrictDay(
        "1", LocalDate.parse(date), null, null, null, null, null, null, null, null);
  }

  private static final class Memory implements Readings, Writer {
    private final List<DengueReading> weeks;
    private final List<DistrictDay> days;
    private final List<PersistedScore> written = new ArrayList<>();

    private Memory(List<DengueReading> weeks, List<DistrictDay> days) {
      this.weeks = weeks;
      this.days = days;
    }

    @Override
    public List<String> districtCodes() {
      return List.of("1");
    }

    @Override
    public List<ForecastDay> forecasts(String locationId) {
      return List.of();
    }

    @Override
    public List<DistrictDay> districtDays() {
      return days;
    }

    @Override
    public List<DengueReading> dengueReadings(int geocode, String disease) {
      return weeks;
    }

    @Override
    public OverallWeights weights(String configKey) {
      return new OverallWeights(
          new BigDecimal("0.40"), new BigDecimal("0.35"), new BigDecimal("0.25"), "confirmed");
    }

    @Override
    public int upsert(List<PersistedScore> scores) {
      written.addAll(scores);
      return scores.size();
    }

    @Override
    public int appendHistory(List<PersistedScore> scores) {
      return scores.size();
    }
  }
}
