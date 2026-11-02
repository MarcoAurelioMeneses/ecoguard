/**
 * Privacy-respecting product analytics.
 *
 * This build validates the event and keeps it in memory. It does not set a
 * cookie, does not read an identifier, and does not call PostHog.
 * The taxonomy is docs/analytics.md. Banned keys match the backend list.
 */

export const events = {
  screenViewed: "screen_viewed",
  mapLayerViewed: "map_layer_viewed",
  districtViewed: "district_viewed",
  analystQuestionAsked: "analyst_question_asked",
} as const;

const allowed = new Set<string>(Object.values(events));

const bannedKeys = new Set([
  "email",
  "name",
  "ip",
  "user_id",
  "question",
  "prompt",
  "sql",
  "address",
]);

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
}

export function capturedEvents(): readonly { event: string; properties: AnalyticsProperties }[] {
  return captured;
}
