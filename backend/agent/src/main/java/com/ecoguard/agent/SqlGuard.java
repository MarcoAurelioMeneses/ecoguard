package com.ecoguard.agent;

/** One SELECT against the agent schema. This is the application guard; the role is the backstop. */
public final class SqlGuard {
  private SqlGuard() {}

  public static String check(String sql) {
    if (sql == null || sql.isBlank()) {
      throw new IllegalArgumentException("sql is required");
    }
    String trimmed = sql.trim();
    if (trimmed.contains("--") || trimmed.contains("/*") || trimmed.contains("*/")) {
      throw new IllegalArgumentException("sql comments are not allowed");
    }
    if (trimmed.endsWith(";")) {
      trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
    }
    if (trimmed.contains(";")) {
      throw new IllegalArgumentException("only one sql statement is allowed");
    }
    if (!trimmed.regionMatches(true, 0, "select", 0, 6)) {
      throw new IllegalArgumentException("only SELECT is allowed");
    }
    String lower = trimmed.toLowerCase();
    if (lower.contains(" raw.")
        || lower.contains(" staging.")
        || lower.contains(" marts.")
        || lower.contains(" ops.")
        || lower.contains(" pg_")) {
      throw new IllegalArgumentException("only the agent schema is allowed");
    }
    if (!lower.contains("agent.")) {
      throw new IllegalArgumentException("query must read the agent schema");
    }
    return trimmed;
  }
}
