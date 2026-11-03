import { describe, expect, it } from "vitest";
import { contentSecurityPolicy } from "./content-security-policy";

describe("contentSecurityPolicy", () => {
  it("drops unsafe-inline in production and keeps a nonce with strict-dynamic", () => {
    const policy = contentSecurityPolicy({
      nonce: "abc",
      dev: false,
      apiOrigin: "http://localhost:8000",
    });
    expect(policy).toContain("'nonce-abc'");
    expect(policy).toContain("'strict-dynamic'");
    expect(policy).not.toContain("unsafe-inline");
    expect(policy).not.toContain("unsafe-eval");
  });

  it("keeps the Next dev exceptions only in development", () => {
    const policy = contentSecurityPolicy({
      nonce: "abc",
      dev: true,
      apiOrigin: "http://localhost:8000",
    });
    expect(policy).toContain("unsafe-inline");
    expect(policy).toContain("unsafe-eval");
    expect(policy).not.toContain("strict-dynamic");
  });
});
