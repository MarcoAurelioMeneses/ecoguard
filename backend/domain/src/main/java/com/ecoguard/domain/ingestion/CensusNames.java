package com.ecoguard.domain.ingestion;

import java.text.Normalizer;
import java.util.Locale;

/** Folds IBGE and GeoSampa district names onto the same key. */
public final class CensusNames {
  private CensusNames() {}

  public static String normalize(String name) {
    if (name == null) {
      return "";
    }
    String stripped = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    return stripped.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
  }
}
