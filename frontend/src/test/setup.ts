import "@testing-library/jest-dom/vitest";
import { cleanup } from "@testing-library/react";
import { toHaveNoViolations } from "jest-axe";
import { afterEach, expect } from "vitest";

afterEach(() => {
  cleanup();
});

expect.extend(toHaveNoViolations);
