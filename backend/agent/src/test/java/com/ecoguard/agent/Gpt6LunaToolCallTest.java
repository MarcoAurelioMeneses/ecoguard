package com.ecoguard.agent;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

@SpringBootTest(classes = Gpt6LunaToolCallTest.App.class)
class Gpt6LunaToolCallTest {
  private static final WireMockServer SERVER =
      new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());

  static {
    SERVER.start();
    SERVER.stubFor(
        post(urlPathEqualTo("/v1/chat/completions"))
            .inScenario("tool-call")
            .whenScenarioStateIs(Scenario.STARTED)
            .willReturn(json(toolCall()))
            .willSetStateTo("answered"));
    SERVER.stubFor(
        post(urlPathEqualTo("/v1/chat/completions"))
            .inScenario("tool-call")
            .whenScenarioStateIs("answered")
            .willReturn(json(answer())));
  }

  @DynamicPropertySource
  static void openai(DynamicPropertyRegistry registry) {
    registry.add("spring.ai.openai.base-url", SERVER::baseUrl);
    registry.add("spring.ai.openai.api-key", () -> "test-key");
    registry.add("spring.ai.openai.chat.options.model", () -> "gpt-6-luna");
    registry.add("spring.ai.openai.chat.options.reasoning-effort", () -> "none");
    registry.add("spring.ai.retry.max-attempts", () -> "1");
    registry.add("spring.http.client.factory", () -> "simple");
  }

  @Autowired ChatClient chatClient;

  @AfterAll
  static void stop() {
    SERVER.stop();
  }

  @Test
  void toolCallSendsReasoningEffortNoneAndNoRejectedParameters() throws Exception {
    assertEquals("72", chatClient.prompt().user("risco de Sapopemba").call().content());
    ObjectMapper mapper = new ObjectMapper();
    JsonNode body = null;
    var events = SERVER.getAllServeEvents();
    for (int i = events.size() - 1; i >= 0; i--) {
      JsonNode candidate = mapper.readTree(events.get(i).getRequest().getBody());
      if (candidate.has("tools")) {
        body = candidate;
        break;
      }
    }
    assertTrue(body != null, "expected a chat completion that offered tools");
    assertEquals("gpt-6-luna", body.path("model").asText());
    assertEquals("none", body.path("reasoning_effort").asText());
    assertTrue(body.path("tools").isArray());
    assertTrue(body.path("tools").size() > 0);
    assertFalse(body.has("logprobs"));
    assertFalse(body.has("top_logprobs"));
    assertFalse(body.has("top_p"));
    assertTrue(body.path("temperature").isNumber());
  }

  private static String toolCall() {
    return """
        {"id":"chatcmpl-1","object":"chat.completion","created":1,"model":"gpt-6-luna",
         "choices":[{"index":0,"finish_reason":"tool_calls","message":{"role":"assistant",
         "tool_calls":[{"id":"call_1","type":"function","function":{
         "name":"getDistrictRisk","arguments":"{\\"district\\":\\"Sapopemba\\"}"}}]}}],
         "usage":{"prompt_tokens":10,"completion_tokens":5,"total_tokens":15}}
        """;
  }

  private static String answer() {
    return """
        {"id":"chatcmpl-2","object":"chat.completion","created":1,"model":"gpt-6-luna",
         "choices":[{"index":0,"finish_reason":"stop","message":{"role":"assistant","content":"72"}}],
         "usage":{"prompt_tokens":20,"completion_tokens":2,"total_tokens":22}}
        """;
  }

  private static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder json(
      String body) {
    return aResponse().withHeader("Content-Type", "application/json").withBody(body);
  }

  @Configuration
  @EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
  static class App {
    @Bean
    @Primary
    RestClient.Builder openAiRestClientBuilder() {
      SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
      factory.setConnectTimeout(2_000);
      factory.setReadTimeout(2_000);
      return RestClient.builder().requestFactory(factory);
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
      return builder.defaultTools(new DistrictLookup()).build();
    }

    static class DistrictLookup {
      @Tool(description = "Risk score for one district")
      public String getDistrictRisk(String district) {
        return "72";
      }
    }
  }
}
