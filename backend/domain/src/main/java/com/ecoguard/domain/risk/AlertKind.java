package com.ecoguard.domain.risk;

/** Active-alert kinds. INPE hotspots are not an alert and do not enter a KPI. */
public enum AlertKind {
  CHUVA,
  CALOR,
  FRIO,
  QUALIDADE_DO_AR,
  HIDROLOGICO
}
