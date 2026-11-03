import { NextResponse, type NextRequest } from "next/server";
import { contentSecurityPolicy } from "@/lib/security/content-security-policy";

export function middleware(request: NextRequest) {
  const nonce = btoa(crypto.randomUUID());
  const dev = process.env.NODE_ENV === "development";
  const policy = contentSecurityPolicy({
    nonce,
    dev,
    apiOrigin: process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8000",
    posthogOrigin: process.env.NEXT_PUBLIC_POSTHOG_HOST,
  });
  const requestHeaders = new Headers(request.headers);
  requestHeaders.set("x-nonce", nonce);
  requestHeaders.set("Content-Security-Policy", policy);
  const response = NextResponse.next({ request: { headers: requestHeaders } });
  response.headers.set("Content-Security-Policy", policy);
  response.headers.set("X-Content-Type-Options", "nosniff");
  response.headers.set("X-Frame-Options", "DENY");
  response.headers.set("Referrer-Policy", "no-referrer");
  response.headers.set("Permissions-Policy", "geolocation=(), camera=(), microphone=()");
  return response;
}

export const config = {
  matcher: ["/((?!_next/static|_next/image|favicon.ico|favicon.svg|logo/|maplibre-gl-worker.mjs).*)"],
};
