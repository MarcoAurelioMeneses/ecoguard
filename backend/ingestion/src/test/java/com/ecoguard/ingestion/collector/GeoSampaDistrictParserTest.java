package com.ecoguard.ingestion.collector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class GeoSampaDistrictParserTest {
  @Test
  void readsOfficialPropertiesAndDefaultsToSirgas() throws Exception {
    var districts =
        GeoSampaDistrictParser.parse(
            new ObjectMapper()
                .readTree(
                    """
                    {"type":"FeatureCollection","features":[{
                      "type":"Feature",
                      "properties":{
                        "cd_distrito_municipal":"67",
                        "nm_distrito_municipal":"RIO PEQUENO",
                        "sg_distrito_municipal":"RPE",
                        "cd_identificador_subprefeitura":21,
                        "qt_area_quilometro":9.6,
                        "dt_atualizacao":"2026-10-27T22:04:13.456Z"
                      },
                      "geometry":{"type":"Polygon","coordinates":[[[0,0],[1,0],[1,1],[0,0]]]}
                    }]}
                    """));
    assertEquals(1, districts.size());
    assertEquals("67", districts.get(0).code());
    assertEquals(31983, districts.get(0).sourceSrid());
    assertTrue(districts.get(0).wkt().startsWith("MULTIPOLYGON"));
  }
}
