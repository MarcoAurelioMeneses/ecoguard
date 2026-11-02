import type { District } from "./district";

export function boundsOf(districts: District[]): [[number, number], [number, number]] | null {
  let minX = Number.POSITIVE_INFINITY;
  let minY = Number.POSITIVE_INFINITY;
  let maxX = Number.NEGATIVE_INFINITY;
  let maxY = Number.NEGATIVE_INFINITY;

  const walk = (value: unknown): void => {
    if (!Array.isArray(value)) {
      return;
    }
    if (typeof value[0] === "number" && typeof value[1] === "number") {
      minX = Math.min(minX, value[0]);
      minY = Math.min(minY, value[1]);
      maxX = Math.max(maxX, value[0]);
      maxY = Math.max(maxY, value[1]);
      return;
    }
    for (const child of value) {
      walk(child);
    }
  };

  for (const district of districts) {
    if ("coordinates" in district.geometry) {
      walk(district.geometry.coordinates);
    }
  }
  if (!Number.isFinite(minX)) {
    return null;
  }
  return [
    [minX, minY],
    [maxX, maxY],
  ];
}
