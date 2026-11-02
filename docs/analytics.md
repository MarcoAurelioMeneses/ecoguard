# Product analytics taxonomy

Privacy rules first. Events describe what was opened, not who opened it. No account id, no cookie id, no IP, no email, no name, no street address, and no free text. Analyst questions are counted, not stored. The legal note is `security/lgpd.md`. The decision is [ADR 0010](adr/0010-product-analytics.md).

The running tracker (`frontend/shared/analytics/tracker.ts` and `ecoguard.adapters.analytics.NullAnalytics`) rejects unknown event names and the property keys `email`, `name`, `ip`, `user_id`, `question`, `prompt`, `sql`, and `address`. It does not transmit anything. PostHog is the planned sink: self-hosted, cookieless or behind consent. It is not deployed.

OpenTelemetry covers traces, metrics, and logs for the API and workers. Langfuse will cover model calls. Neither receives this taxonomy, and this taxonomy does not receive prompts.

## Events

| Event | When | Properties | Not included |
| --- | --- | --- | --- |
| `screen_viewed` | A screen route mounts | `screen`: `00`–`06` | URL query, referrer |
| `map_layer_viewed` | The risk map layer changes | `layer`: `environmental`, `health`, `social`, `overall` | District scores |
| `district_viewed` | A district detail opens | `district_code`: GeoSampa `cd_distrito_municipal` | District name, geometry |
| `analyst_question_asked` | A question is submitted | `screen`: `04`, `length_bucket`: `1-40`, `41-120`, `121-400`, `401+` | The question text, the answer, tool SQL |

`length_bucket` is the character length of the question after trimming, in those four buckets. The text is discarded before the event is built.

## Identity

There is no `distinct_id` tied to a person in this foundation. When PostHog is enabled, use a random id kept in memory for the tab, not in `localStorage`, unless a consent banner has been accepted. Do not enable session replay. Do not capture clicks automatically. Call `track` at the points in the table only.

## Where the call sits

| Layer | Responsibility |
| --- | --- |
| `features/<screen>/ui` | Calls `track` when the screen mounts or the user changes a layer |
| `shared/analytics` | Validates the event. Later, the PostHog adapter |
| Backend use cases | May emit the same names for server-confirmed actions (a completed analyst turn). They call `AnalyticsSink`, not PostHog |

The web placeholder emits `screen_viewed` only.
