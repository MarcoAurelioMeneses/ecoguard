import { renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("../api/districts", () => ({
  listDistricts: vi.fn(),
}));

import { listDistricts } from "../api/districts";
import type { District } from "../model/district";
import { useDistricts } from "./use-districts";

const sample: District = {
  code: "1",
  name: "AGUA RASA",
  abbreviation: null,
  subprefectureId: null,
  areaKm2: 7.1,
  source: "geosampa:wfs:geoportal:distrito_municipal",
  sourceUpdatedAt: null,
  geometry: { type: "Polygon", coordinates: [] },
  score: null,
};

describe("useDistricts", () => {
  beforeEach(() => {
    vi.mocked(listDistricts).mockReset();
  });

  it("returns districts from the feature API", async () => {
    vi.mocked(listDistricts).mockResolvedValue([sample]);
    const { result } = renderHook(() => useDistricts());
    await waitFor(() => expect(result.current.state.status).toBe("ready"));
    if (result.current.state.status === "ready") {
      expect(result.current.state.data[0]?.name).toBe("AGUA RASA");
    }
  });

  it("treats an empty collection as empty, not as zero scores", async () => {
    vi.mocked(listDistricts).mockResolvedValue([]);
    const { result } = renderHook(() => useDistricts());
    await waitFor(() => expect(result.current.state.status).toBe("empty"));
  });
});
