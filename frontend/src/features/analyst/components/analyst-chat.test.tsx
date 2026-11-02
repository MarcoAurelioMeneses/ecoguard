import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { axe } from "jest-axe";
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("../api/analyst", () => ({
  askAnalyst: vi.fn(),
}));

import { askAnalyst } from "../api/analyst";
import { AnalystChat } from "./analyst-chat";

describe("AnalystChat", () => {
  beforeEach(() => {
    vi.mocked(askAnalyst).mockReset();
  });

  it("shows the AI tag, the error disclaimer, and passes axe", async () => {
    const view = render(<AnalystChat />);
    expect(screen.getByText("AI")).toBeInTheDocument();
    expect(screen.getByText(/may contain errors/i)).toBeInTheDocument();
    expect(await axe(view.container)).toHaveNoViolations();
  });

  it("asks through the feature API and offers regenerate", async () => {
    vi.mocked(askAnalyst).mockResolvedValue({ answer: "Cited answer." });
    const user = userEvent.setup();
    const view = render(<AnalystChat />);
    await user.type(view.getByLabelText("Question"), "Heat in Sé");
    await user.click(view.getByRole("button", { name: "Send" }));
    expect(await view.findByText("Cited answer.")).toBeInTheDocument();
    expect(askAnalyst).toHaveBeenCalledWith("Heat in Sé");
    await user.click(screen.getByRole("button", { name: "Regenerate" }));
    expect(askAnalyst).toHaveBeenCalledTimes(2);
    expect(screen.getByRole("button", { name: "Useful" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Not useful" })).toBeInTheDocument();
  });
});
