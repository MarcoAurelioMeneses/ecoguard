export type LoadState<T> =
  | { status: "loading" }
  | { status: "error"; message: string }
  | { status: "empty" }
  | { status: "ready"; data: T };

export function messageFrom(error: unknown, fallback: string): string {
  return error instanceof Error ? error.message : fallback;
}
