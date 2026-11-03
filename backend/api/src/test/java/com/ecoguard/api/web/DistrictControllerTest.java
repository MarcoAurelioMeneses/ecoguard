package com.ecoguard.api.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecoguard.api.district.DistrictReadModel;
import com.ecoguard.api.security.ApiSecurity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DistrictController.class)
@Import(ApiSecurity.class)
class DistrictControllerTest {
  @Autowired MockMvc mvc;

  @MockitoBean DistrictReadModel districts;

  @Test
  void listsDistrictsAsGeoJson() throws Exception {
    when(districts.list())
        .thenReturn(
            List.of(
                new DistrictReadModel.DistrictView(
                    "67",
                    "RIO PEQUENO",
                    "RPE",
                    21,
                    9.6,
                    "{\"type\":\"Polygon\",\"coordinates\":[]}",
                    "geosampa:wfs:geoportal:distrito_municipal",
                    Instant.parse("2026-10-27T22:04:13.456Z"),
                    null)));
    mvc.perform(get("/districts"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.type").value("FeatureCollection"))
        .andExpect(jsonPath("$.features[0].properties.code").value("67"))
        .andExpect(jsonPath("$.features[0].geometry.type").value("Polygon"));
  }

  @Test
  void missingDistrictIs404() throws Exception {
    when(districts.find("0")).thenReturn(Optional.empty());
    mvc.perform(get("/districts/0")).andExpect(status().isNotFound());
  }

  @Test
  void riskConfigIsTheConfirmedRow() throws Exception {
    when(districts.riskConfig())
        .thenReturn(
            new DistrictReadModel.RiskConfigView(
                new BigDecimal("0.400"),
                new BigDecimal("0.350"),
                new BigDecimal("0.250"),
                "confirmed"));
    mvc.perform(get("/risk-config"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("confirmed"))
        .andExpect(jsonPath("$.environmental").value(0.4));
  }
}
