import { http } from "@/lib/api/client";
import type { DistrictFeatureDto } from "@/lib/api/dto";
import type { District } from "../model/district";

function toDistrict(feature: DistrictFeatureDto): District {
  return {
    code: feature.properties.code,
    name: feature.properties.name,
    abbreviation: feature.properties.abbreviation ?? null,
    subprefectureId: feature.properties.subprefectureId ?? null,
    areaKm2: feature.properties.areaKm2 ?? null,
    source: feature.properties.source,
    sourceUpdatedAt: feature.properties.sourceUpdatedAt ?? null,
    geometry: feature.geometry,
  };
}

export async function listDistricts(): Promise<District[]> {
  const collection = await http.listDistricts();
  return collection.features.map(toDistrict);
}

export async function getDistrict(code: string): Promise<District> {
  return toDistrict(await http.getDistrict(code));
}
