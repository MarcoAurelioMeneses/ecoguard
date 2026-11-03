import type { operations } from "./generated";
import type { AnalystAnswerDto, CityScoresDto, FeatureCollectionDto, RiskConfigDto } from "./dto";

type Listed = operations["listDistricts"]["responses"][200]["content"]["application/json"];
type Configured = operations["riskConfig"]["responses"][200]["content"]["application/json"];

export function apiBaseUrl(): string {
  return process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8000";
}

async function getJson<T>(path: string): Promise<T> {
  const response = await fetch(`${apiBaseUrl()}${path}`, {
    headers: { Accept: "application/json" },
  });
  if (!response.ok) {
    throw new Error(`${path} returned ${response.status}`);
  }
  return (await response.json()) as T;
}

export const http = {
  listDistricts: () => getJson<Listed>("/districts") as unknown as Promise<FeatureCollectionDto>,
  getDistrict: (code: string) =>
    getJson<FeatureCollectionDto["features"][number]>(`/districts/${encodeURIComponent(code)}`),
  riskConfig: () => getJson<Configured>("/risk-config") as Promise<RiskConfigDto>,
  scoreSummary: () => getJson<CityScoresDto>("/scores/summary"),
  askAnalyst: async (question: string): Promise<AnalystAnswerDto> => {
    const response = await fetch(`${apiBaseUrl()}/analyst/questions`, {
      method: "POST",
      headers: { Accept: "application/json", "Content-Type": "application/json" },
      body: JSON.stringify({ question }),
    });
    if (!response.ok) {
      throw new Error(`analyst returned ${response.status}`);
    }
    return (await response.json()) as AnalystAnswerDto;
  },
};
