import { http } from "@/lib/api/client";
import type { AnalystReply } from "../model/reply";

export async function askAnalyst(question: string): Promise<AnalystReply> {
  const dto = await http.askAnalyst(question);
  return { answer: dto.answer };
}
