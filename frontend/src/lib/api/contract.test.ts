import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";
import { describe, it } from "vitest";

const spec = JSON.parse(
  readFileSync(join(dirname(fileURLToPath(import.meta.url)), "../../../openapi/openapi.json"), "utf8"),
) as { paths: Record<string, { get?: { operationId?: string }; post?: unknown }> };

describe("openapi contract", () => {
  it("exposes the district and analyst operations", () => {
    assert.equal(spec.paths["/districts"]?.get?.operationId, "listDistricts");
    assert.equal(spec.paths["/risk-config"]?.get?.operationId, "riskConfig");
    assert.ok(spec.paths["/analyst/questions"]?.post);
  });
});
