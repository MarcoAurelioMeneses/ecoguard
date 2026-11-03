/**
 * Wire types for the Java API. Domain types live in feature model folders.
 * Regenerate generated.ts with `pnpm generate:api` when the OpenAPI snapshot changes.
 */
export type DistrictPropertiesDto = {
  code: string;
  name: string;
  abbreviation?: string | null;
  subprefectureId?: number | null;
  areaKm2?: number | null;
  source: string;
  sourceUpdatedAt?: string | null;
  score?: DistrictScoreDto | null;
};

export type DistrictScoreDto = {
  scoreDate?: string;
  environmental?: number | null;
  health?: number | null;
  social?: number | null;
  overall?: number | null;
  heat?: number | null;
  rain?: number | null;
  dengue?: number | null;
  environmentalCoverage?: number | null;
  healthCoverage?: number | null;
  environmentalClass?: string;
  healthClass?: string;
  socialClass?: string;
  overallClass?: string;
  environmentalComplete?: boolean;
  healthComplete?: boolean;
  socialComplete?: boolean;
  grainNote?: string | null;
};

export type ScoreSeriesDto = {
  scoreDate: string;
  heat?: number | null;
  rain?: number | null;
  dengue?: number | null;
  environmental?: number | null;
  health?: number | null;
};

export type CityScoresDto = {
  latest?: DistrictScoreDto | null;
  series: ScoreSeriesDto[];
};

export type DistrictFeatureDto = {
  type: "Feature";
  properties: DistrictPropertiesDto;
  geometry: GeoJSON.Geometry;
};

export type FeatureCollectionDto = {
  type: "FeatureCollection";
  features: DistrictFeatureDto[];
};

export type RiskConfigDto = {
  environmental: number;
  health: number;
  social: number;
  status: string;
};

export type AnalystAnswerDto = {
  answer: string;
};
