import { http } from "@/lib/api/client";
import type { RiskWeights } from "../model/risk-weights";

export async function loadRiskWeights(): Promise<RiskWeights> {
  const dto = await http.riskConfig();
  return {
    environmental: dto.environmental,
    health: dto.health,
    social: dto.social,
    status: dto.status,
  };
}
