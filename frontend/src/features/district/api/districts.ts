import { http, type DistrictFeatureWire } from "@/lib/api/client";
import type { District, DistrictScore, RiskClass } from "../model/district";

type ScoreWire = NonNullable<DistrictFeatureWire["properties"]["score"]>;

const geometryTypes = [
  "Point",
  "MultiPoint",
  "LineString",
  "MultiLineString",
  "Polygon",
  "MultiPolygon",
  "GeometryCollection",
] as const;

function riskClass(value: string | undefined): RiskClass {
  if (value === "baixo" || value === "moderado" || value === "alto" || value === "critico") {
    return value;
  }
  return "nodata";
}

function toGeometry(value: DistrictFeatureWire["geometry"]): GeoJSON.Geometry {
  const type = value?.type;
  if (typeof type === "string" && geometryTypes.some((item) => item === type) && value) {
    return { type, ...value } as GeoJSON.Geometry;
  }
  return { type: "Polygon", coordinates: [] };
}

function toScore(score: ScoreWire | null | undefined): DistrictScore | null {
  if (!score?.scoreDate) {
    return null;
  }
  return {
    scoreDate: score.scoreDate,
    environmental: score.environmental ?? null,
    health: score.health ?? null,
    social: score.social ?? null,
    overall: score.overall ?? null,
    heat: score.heat ?? null,
    rain: score.rain ?? null,
    dengue: score.dengue ?? null,
    environmentalCoverage: score.environmentalCoverage ?? null,
    healthCoverage: score.healthCoverage ?? null,
    socialCoverage: score.socialCoverage ?? null,
    environmentalClass: riskClass(score.environmentalClass),
    healthClass: riskClass(score.healthClass),
    socialClass: riskClass(score.socialClass),
    overallClass: riskClass(score.overallClass),
    environmentalComplete: score.environmentalComplete ?? false,
    healthComplete: score.healthComplete ?? false,
    socialComplete: score.socialComplete ?? false,
    grainNote: score.grainNote ?? null,
  };
}

function toDistrict(feature: DistrictFeatureWire): District {
  return {
    code: feature.properties.code,
    name: feature.properties.name,
    abbreviation: feature.properties.abbreviation ?? null,
    subprefectureId: feature.properties.subprefectureId ?? null,
    areaKm2: feature.properties.areaKm2 ?? null,
    source: feature.properties.source,
    sourceUpdatedAt: feature.properties.sourceUpdatedAt ?? null,
    geometry: toGeometry(feature.geometry),
    score: toScore(feature.properties.score),
  };
}

export async function listDistricts(): Promise<District[]> {
  const collection = await http.listDistricts();
  return collection.features.map(toDistrict);
}

export async function getDistrict(code: string): Promise<District> {
  return toDistrict(await http.getDistrict(code));
}
