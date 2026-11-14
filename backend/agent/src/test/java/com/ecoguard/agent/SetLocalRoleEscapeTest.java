package com.ecoguard.agent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Free SQL after {@code SET LOCAL ROLE} runs on the owner session. {@code set_config('role',
 * session_user, true)} puts that owner back for the next statement. A login that is not a member of
 * the owner cannot do that.
 */
class SetLocalRoleEscapeTest {
  @Test
  void setLocalRoleOnTheOwnerConnectionIsEscapable() throws Exception {
    try (PostgreSQLContainer<?> postgres = postgres()) {
      postgres.start();
      try (Connection owner = owner(postgres)) {
        prepareSecret(owner, "limited", false);
        owner.setAutoCommit(false);
        try (Statement statement = owner.createStatement()) {
          statement.execute("SET LOCAL ROLE limited");
        }
        assertTrue(escaped(owner), "SET LOCAL ROLE contained the session user");
      }
    }
  }

  @Test
  void aLoginWithOnlyItsOwnGrantsCannotReadTheSecret() throws Exception {
    try (PostgreSQLContainer<?> postgres = postgres()) {
      postgres.start();
      try (Connection owner = owner(postgres)) {
        prepareSecret(owner, "agent_login", true);
      }
      try (Connection agent =
          DriverManager.getConnection(postgres.getJdbcUrl(), "agent_login", "agent_login")) {
        assertFalse(escaped(agent), "dedicated login read secret.vault");
      }
    }
  }

  private static PostgreSQLContainer<?> postgres() {
    return new PostgreSQLContainer<>(DockerImageName.parse("postgres:18-bookworm"));
  }

  private static Connection owner(PostgreSQLContainer<?> postgres) throws SQLException {
    return DriverManager.getConnection(
        postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
  }

  private static void prepareSecret(Connection owner, String role, boolean login)
      throws SQLException {
    String loginSql =
        login
            ? " LOGIN PASSWORD 'agent_login' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT"
            : " NOLOGIN NOSUPERUSER NOINHERIT";
    try (Statement statement = owner.createStatement()) {
      statement.execute("CREATE SCHEMA secret");
      statement.execute("CREATE TABLE secret.vault (note text)");
      statement.execute("INSERT INTO secret.vault VALUES ('hidden')");
      statement.execute("CREATE ROLE " + role + loginSql);
      statement.execute("REVOKE ALL ON SCHEMA secret FROM " + role);
      statement.execute("REVOKE ALL ON secret.vault FROM " + role);
    }
  }

  private static boolean escaped(Connection connection) {
    readSecret(connection, "SELECT set_config('role', session_user, true)");
    if (readSecret(connection, "SELECT note FROM secret.vault")) {
      return true;
    }
    return readSecret(
        connection, "SELECT query_to_xml('SELECT note FROM secret.vault', true, false, '')");
  }

  private static boolean readSecret(Connection connection, String sql) {
    try (Statement statement = connection.createStatement();
        ResultSet rows = statement.executeQuery(sql)) {
      while (rows.next()) {
        for (int column = 1; column <= rows.getMetaData().getColumnCount(); column++) {
          String value = rows.getString(column);
          if (value != null && value.contains("hidden")) {
            return true;
          }
        }
      }
      return false;
    } catch (SQLException ex) {
      return false;
    }
  }
}
