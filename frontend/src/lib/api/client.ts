import type { operations } from "./generated";
import type { AnalystAnswerDto } from "./dto";

type Listed = operations["listDistricts"]["responses"][200]["content"]["application/json"];
type OneDistrict = operations["getDistrict"]["responses"][200]["content"]["application/json"];
type Configured = operations["riskConfig"]["responses"][200]["content"]["application/json"];
type Summary = operations["scoreSummary"]["responses"][200]["content"]["application/json"];

export type DistrictCollection = Listed;
export type DistrictFeatureWire = OneDistrict;

export function apiBaseUrl(): string {
  return process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8000";
}

async function getJson<T>(path: string): Promise<T> {
  const response = await fetch(`${apiBaseUrl()}${path}`, {
    headers: { Accept: "application/json" },
  });
  if (!response.ok) {
    throw new Error(`${path} respondeu ${response.status}`);
  }
  return (await response.json()) as T;
}

export const http = {
  listDistricts: () => getJson<Listed>("/districts"),
  getDistrict: (code: string) => getJson<OneDistrict>(`/districts/${encodeURIComponent(code)}`),
  riskConfig: () => getJson<Configured>("/risk-config"),
  scoreSummary: () => getJson<Summary>("/scores/summary"),
  askAnalyst: async (question: string): Promise<AnalystAnswerDto> => {
    const response = await fetch(`${apiBaseUrl()}/analyst/questions`, {
      method: "POST",
      headers: { Accept: "application/json", "Content-Type": "application/json" },
      body: JSON.stringify({ question }),
    });
    if (!response.ok) {
      throw new Error(`o analista respondeu ${response.status}`);
    }
    return (await response.json()) as AnalystAnswerDto;
  },
};
