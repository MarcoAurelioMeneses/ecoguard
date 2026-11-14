package com.ecoguard.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecoguard.domain.risk.CovisaDengue.Edition;
import com.ecoguard.domain.risk.OverallWeights;
import com.ecoguard.domain.risk.ScoreInputs.DengueReading;
import com.ecoguard.domain.risk.ScoreInputs.DistrictCovisa;
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
  void scoresTheCovisaWindowValidOnThatDate() {
    List<DistrictCovisa> bulletins =
        List.of(
            new DistrictCovisa(
                "1",
                new Edition(
                    2025,
                    39,
                    LocalDate.parse("2025-09-28"),
                    LocalDate.parse("2025-10-04"),
                    1485,
                    new BigDecimal("610.0"))),
            new DistrictCovisa(
                "1",
                new Edition(
                    2025,
                    43,
                    LocalDate.parse("2025-10-26"),
                    LocalDate.parse("2025-11-01"),
                    1525,
                    new BigDecimal("629.1"))));
    Memory memory = new Memory(bulletins, List.of(day("2025-10-28"), day("2025-11-10")));
    new ComputeRiskScores()
        .execute(memory, memory, Instant.parse("2026-11-03T00:00:00Z"), "default");
    assertEquals(0, new BigDecimal("16.50").compareTo(memory.written.get(0).score().dengue()));
    assertEquals(0, new BigDecimal("16.50").compareTo(memory.written.get(1).score().dengue()));
    assertTrue(memory.written.get(0).grainNote().contains("cases=40"));
    assertTrue(memory.written.get(0).grainNote().contains("population_source=covisa_implied"));
    assertTrue(memory.written.get(0).grainNote().contains("window_weeks=4"));
    assertTrue(memory.written.get(0).grainNote().contains("editions_used=2"));
  }

  private static DistrictDay day(String date) {
    return new DistrictDay(
        "1", LocalDate.parse(date), null, null, null, null, null, null, null, null);
  }

  private static final class Memory implements Readings, Writer {
    private final List<DistrictCovisa> bulletins;
    private final List<DistrictDay> days;
    private final List<PersistedScore> written = new ArrayList<>();

    private Memory(List<DistrictCovisa> bulletins, List<DistrictDay> days) {
      this.bulletins = bulletins;
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
      return List.of();
    }

    @Override
    public List<DistrictCovisa> covisaEditions() {
      return bulletins;
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
