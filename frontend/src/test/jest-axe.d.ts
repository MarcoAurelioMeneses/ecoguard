declare module "jest-axe" {
  export function axe(node: Element, options?: unknown): Promise<unknown>;
  export const toHaveNoViolations: {
    toHaveNoViolations(results: unknown): { actual?: unknown; message(): string; pass: boolean };
  };
}
