/**
 * Product analytics. Events stay in memory always.
 * PostHog is used only when NEXT_PUBLIC_POSTHOG_KEY and NEXT_PUBLIC_POSTHOG_HOST are set.
 * No autocapture, no cookies, no session replay, no question text.
 */
export const events = {
  screenViewed: "screen_viewed",
  mapLayerViewed: "map_layer_viewed",
  districtViewed: "district_viewed",
  analystQuestionAsked: "analyst_question_asked",
} as const;

const allowed = new Set<string>(Object.values(events));
const bannedKeys = new Set(["email", "name", "ip", "user_id", "question", "prompt", "sql", "address"]);

export type AnalyticsProperties = Record<string, string | number>;

const captured: { event: string; properties: AnalyticsProperties }[] = [];

export function track(event: string, properties: AnalyticsProperties): void {
  if (!allowed.has(event)) {
    throw new Error(`unknown analytics event: ${event}`);
  }
  for (const key of Object.keys(properties)) {
    if (bannedKeys.has(key)) {
      throw new Error(`analytics property is not allowed: ${key}`);
    }
  }
  captured.push({ event, properties });
  void send(event, properties);
}

async function send(event: string, properties: AnalyticsProperties): Promise<void> {
  const key = process.env.NEXT_PUBLIC_POSTHOG_KEY;
  const host = process.env.NEXT_PUBLIC_POSTHOG_HOST;
  if (!key || !host || typeof window === "undefined") {
    return;
  }
  const posthog = (await import("posthog-js")).default;
  if (!posthog.__loaded) {
    posthog.init(key, {
      api_host: host,
      autocapture: false,
      capture_pageview: false,
      persistence: "memory",
      disable_session_recording: true,
    });
  }
  posthog.capture(event, properties);
}

export function capturedEvents(): readonly { event: string; properties: AnalyticsProperties }[] {
  return captured;
}
