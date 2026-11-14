package com.ecoguard.agent;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * Tools the model may call. The JDBC client logs in as {@code ecoguard_agent}, which can read
 * {@code agent} views and nothing else. There is no free-SQL tool.
 */
@Component
public class AnalystTools {
  private final JdbcClient jdbc;

  public AnalystTools(@Qualifier("agentJdbc") JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  @Tool(description = "Read the published risk score for one district code and ISO date.")
  public List<Map<String, Object>> getRisk(
      @ToolParam(description = "GeoSampa district code") String districtCode,
      @ToolParam(description = "ISO-8601 date") String date) {
    return jdbc.sql(
            """
            SELECT district_code, score_date, config_key, environmental_score, health_score,
                   social_score, overall_score
            FROM agent.district_risk_scores
            WHERE district_code = :district AND score_date = :on
            """)
        .param("district", districtCode)
        .param("on", LocalDate.parse(date))
        .query()
        .listOfRows();
  }

  @Tool(description = "Search curated methodology documents by meaning. Pass no free-form SQL.")
  public List<Map<String, Object>> searchDocs(
      @ToolParam(description = "pgvector literal such as [0.1,0.2,...]") String embedding,
      int limit) {
    if (limit < 1 || limit > 8) {
      throw new IllegalArgumentException("limit must be between 1 and 8");
    }
    return jdbc.sql(
            """
            SELECT document_id::text AS document_id, source, title, left(body, 400) AS excerpt, published_on
            FROM agent.documents
            ORDER BY embedding <=> CAST(:embedding AS vector)
            LIMIT :limit
            """)
        .param("embedding", embedding)
        .param("limit", limit)
        .query()
        .listOfRows();
  }
}
