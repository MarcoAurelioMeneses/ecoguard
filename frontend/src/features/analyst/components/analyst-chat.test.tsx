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
    expect(screen.getByText("IA")).toBeInTheDocument();
    expect(screen.getByText(/podem conter erros/i)).toBeInTheDocument();
    expect(await axe(view.container)).toHaveNoViolations();
  });

  it("asks through the feature API and offers regenerate", async () => {
    vi.mocked(askAnalyst).mockResolvedValue({ answer: "Cited answer." });
    const user = userEvent.setup();
    const view = render(<AnalystChat />);
    await user.type(view.getByLabelText("Pergunta"), "Calor na Sé");
    await user.click(view.getByRole("button", { name: "Enviar" }));
    expect(await view.findByText("Cited answer.")).toBeInTheDocument();
    expect(askAnalyst).toHaveBeenCalledWith("Calor na Sé");
    await user.click(screen.getByRole("button", { name: "Regenerar" }));
    expect(askAnalyst).toHaveBeenCalledTimes(2);
    expect(screen.getByRole("button", { name: "Útil" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Não útil" })).toBeInTheDocument();
  });
});
