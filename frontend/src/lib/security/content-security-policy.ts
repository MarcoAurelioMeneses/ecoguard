export type CspInput = {
  nonce: string;
  dev: boolean;
  apiOrigin: string;
  posthogOrigin?: string;
};

/** Production scripts and styles use a nonce and strict-dynamic. Dev keeps the Next compiler exceptions. */
export function contentSecurityPolicy(input: CspInput): string {
  const connect = ["'self'", input.apiOrigin, input.posthogOrigin].filter(Boolean).join(" ");
  const script = input.dev
    ? "script-src 'self' 'unsafe-inline' 'unsafe-eval'"
    : `script-src 'self' 'nonce-${input.nonce}' 'strict-dynamic'`;
  const style = input.dev ? "style-src 'self' 'unsafe-inline'" : `style-src 'self' 'nonce-${input.nonce}'`;
  return [
    "default-src 'self'",
    "img-src 'self' data: blob:",
    style,
    "font-src 'self'",
    script,
    "worker-src 'self' blob:",
    `connect-src ${connect}`,
    "frame-ancestors 'none'",
    "base-uri 'self'",
    "form-action 'self'",
  ].join("; ");
}
