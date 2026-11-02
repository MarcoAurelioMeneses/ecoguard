package com.ecoguard.agent;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Tools the model may call. Every method drops to the read-only agent role. */
@Component
public class AnalystTools {
  private final JdbcClient jdbc;

  public AnalystTools(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  @Tool(description = "Read the published risk score for one district code and ISO date.")
  @Transactional(readOnly = true)
  public List<Map<String, Object>> getRisk(
      @ToolParam(description = "GeoSampa district code") String districtCode,
      @ToolParam(description = "ISO-8601 date") String date) {
    asAgent();
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

  @Tool(description = "Run one SELECT against agent views. Do not invent tables.")
  @Transactional(readOnly = true)
  public List<Map<String, Object>> query(@ToolParam(description = "A single SELECT") String sql) {
    String checked = SqlGuard.check(sql);
    asAgent();
    jdbc.sql("SET LOCAL statement_timeout = '3s'").update();
    return jdbc.sql(checked).query().listOfRows();
  }

  @Tool(description = "Search curated methodology documents by meaning. Pass no free-form SQL.")
  @Transactional(readOnly = true)
  public List<Map<String, Object>> searchDocs(
      @ToolParam(description = "pgvector literal such as [0.1,0.2,...]") String embedding,
      int limit) {
    if (limit < 1 || limit > 8) {
      throw new IllegalArgumentException("limit must be between 1 and 8");
    }
    asAgent();
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

  private void asAgent() {
    jdbc.sql("SET LOCAL ROLE ecoguard_agent").update();
  }
}
