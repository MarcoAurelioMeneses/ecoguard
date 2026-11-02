import { describe, expect, it } from "vitest";
import { lengthBucket } from "./question";

describe("lengthBucket", () => {
  it("buckets trimmed length without keeping the text", () => {
    expect(lengthBucket("  short  ")).toBe("1-40");
    expect(lengthBucket("x".repeat(80))).toBe("41-120");
    expect(lengthBucket("x".repeat(200))).toBe("121-400");
    expect(lengthBucket("x".repeat(401))).toBe("401+");
  });
});
