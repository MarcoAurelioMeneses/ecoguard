import { render } from "@testing-library/react";
import { axe } from "jest-axe";
import { describe, expect, it } from "vitest";
import { EmptyState, ErrorState, LoadingState } from "./states";

describe("states", () => {
  it("exposes loading, empty, and error without axe violations", async () => {
    const view = render(
      <div>
        <LoadingState label="Loading districts" />
        <EmptyState title="No districts loaded" detail="Run the collector." />
        <ErrorState message="The API did not answer" onRetry={() => undefined} />
      </div>,
    );
    expect(await axe(view.container)).toHaveNoViolations();
    expect(view.getByRole("status")).toHaveTextContent("Loading districts");
    expect(view.getByRole("alert")).toHaveTextContent("The API did not answer");
  });
});
