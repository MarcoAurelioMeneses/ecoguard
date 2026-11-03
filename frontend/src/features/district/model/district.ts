export type RiskClass = "baixo" | "moderado" | "alto" | "critico" | "nodata";

export type DistrictScore = {
  scoreDate: string;
  environmental: number | null;
  health: number | null;
  social: number | null;
  overall: number | null;
  heat: number | null;
  rain: number | null;
  dengue: number | null;
  environmentalCoverage: number | null;
  healthCoverage: number | null;
  environmentalClass: RiskClass;
  healthClass: RiskClass;
  socialClass: RiskClass;
  overallClass: RiskClass;
  environmentalComplete: boolean;
  healthComplete: boolean;
  socialComplete: boolean;
  grainNote: string | null;
};

export type ScoreSeriesPoint = {
  scoreDate: string;
  heat: number | null;
  rain: number | null;
  dengue: number | null;
  environmental: number | null;
  health: number | null;
};

export type District = {
  code: string;
  name: string;
  abbreviation: string | null;
  subprefectureId: number | null;
  areaKm2: number | null;
  source: string;
  sourceUpdatedAt: string | null;
  geometry: GeoJSON.Geometry;
  score: DistrictScore | null;
};
