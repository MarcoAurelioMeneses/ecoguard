import { describe, expect, it } from "vitest";
import { boundsOf } from "./bounds";
import type { District } from "./district";

const district = (coordinates: number[][]): District => ({
  code: "1",
  name: "Sample",
  abbreviation: null,
  subprefectureId: null,
  areaKm2: null,
  source: "geosampa:wfs:geoportal:distrito_municipal",
  sourceUpdatedAt: null,
  geometry: { type: "Polygon", coordinates: [coordinates] },
  score: null,
});

describe("boundsOf", () => {
  it("returns the outer longitude and latitude", () => {
    expect(
      boundsOf([
        district([
          [-46.8, -23.6],
          [-46.5, -23.6],
          [-46.5, -23.4],
          [-46.8, -23.6],
        ]),
      ]),
    ).toEqual([
      [-46.8, -23.6],
      [-46.5, -23.4],
    ]);
  });
});
