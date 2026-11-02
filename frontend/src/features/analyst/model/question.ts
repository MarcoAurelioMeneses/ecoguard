export type LengthBucket = "1-40" | "41-120" | "121-400" | "401+";

export function lengthBucket(question: string): LengthBucket {
  const length = question.trim().length;
  if (length <= 40) {
    return "1-40";
  }
  if (length <= 120) {
    return "41-120";
  }
  if (length <= 400) {
    return "121-400";
  }
  return "401+";
}
