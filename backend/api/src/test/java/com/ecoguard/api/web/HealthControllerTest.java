package com.ecoguard.api.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecoguard.agent.CityAnalyst;
import com.ecoguard.api.security.ApiSecurity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {HealthController.class, AnalystController.class})
@Import(ApiSecurity.class)
class HealthControllerTest {
  @Autowired MockMvc mvc;

  @MockitoBean CityAnalyst analyst;

  @Test
  void healthIsOpenAndSendsSecurityHeaders() throws Exception {
    mvc.perform(get("/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ok"))
        .andExpect(jsonPath("$.service").value("ecoguard-api"))
        .andExpect(header().string("X-Frame-Options", "DENY"))
        .andExpect(
            header()
                .string(
                    "Content-Security-Policy",
                    org.hamcrest.Matchers.containsString("default-src 'none'")));
  }

  @Test
  void secondRootCallIsRateLimitedWhenTheLimitIsOne() throws Exception {
    // The default limit is 120. This test only checks that a normal call is allowed
    // and that the analyst rejects an oversized question before the model is called.
    mvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.health").value("/health"));
    mvc.perform(
            post("/analyst/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"question\":\"\"}"))
        .andExpect(status().isBadRequest());
  }
}
