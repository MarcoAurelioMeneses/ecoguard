# ADR 0012 — Analyst default model is gpt-6-luna

- Status: accepted
- Date: 2026-11-14

## Context

ADR 0006 pinned the analyst to `gpt-4o-mini`. OpenAI now shuts that snapshot down on 14 January 2027 and names `gpt-6-luna` as the replacement ([deprecations](https://developers.openai.com/api/docs/deprecations)).

The interactive analyst uses the Standard speed tier and stays under the short-context threshold (272,000 input tokens). Those rates, in USD per 1,000,000 tokens, are the `gpt-6-luna` row on the [pricing page](https://developers.openai.com/api/docs/pricing):

| | Input | Output | Cached input | 30-minute cache write | 12-hour cache write |
| --- | --- | --- | --- | --- | --- |
| Short context | 0.10 | 0.50 | 0.01 | 0.125 | 0.25 |
| Long context | 0.20 | 0.75 | 0.02 | 0.25 | 0.50 |

The running process prices calls from `ecoguard.analyst.pricing.<model>.{input,cached-input,output}`. The default entry is the short-context Standard row: input 0.10, cached input 0.01, output 0.50. A prompt over 272,000 input tokens is the long-context row for the whole request and is not that property.

Chat Completions accepts function calling on this model only with `reasoning_effort` set to `none` ([model page](https://developers.openai.com/api/docs/models/gpt-6-luna), [migration guide](https://developers.openai.com/api/docs/guides/latest-model)). `temperature`, `top_p`, and `logprobs` are rejected when the effort is anything else. They are accepted when the effort is `none`. This service sets the effort to `none` and does not set `top_p` or `logprobs`. Spring AI's default `temperature` of 0.7 stays, because that value is legal at this effort.

## Decision

`ecoguard.analyst.model` defaults to `gpt-6-luna` (`ECOGUARD_ANALYST_MODEL` overrides it). `ecoguard.analyst.reasoning-effort` defaults to `none`. Both are copied onto the Spring AI chat options. Token prices live under `ecoguard.analyst.pricing`.

## Consequences

- The analyst contract besides the model id is a separate ADR.
- Batch, Flex, Fast, and Ultrafast prices are not the analyst default and are not in the pricing properties.
