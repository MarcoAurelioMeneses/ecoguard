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
