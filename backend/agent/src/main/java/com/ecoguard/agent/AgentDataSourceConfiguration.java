package com.ecoguard.agent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/** Separate login from the API. This user is not a member of the table owner. */
@Configuration
public class AgentDataSourceConfiguration {
  @Bean(name = "agentJdbc")
  JdbcClient agentJdbc(
      @Value("${ecoguard.agent.jdbc-url}") String url,
      @Value("${ecoguard.agent.username}") String username,
      @Value("${ecoguard.agent.password}") String password) {
    return JdbcClient.create(new DriverManagerDataSource(url, username, password));
  }
}
