export type District = {
  code: string;
  name: string;
  abbreviation: string | null;
  subprefectureId: number | null;
  areaKm2: number | null;
  source: string;
  sourceUpdatedAt: string | null;
  geometry: GeoJSON.Geometry;
};
