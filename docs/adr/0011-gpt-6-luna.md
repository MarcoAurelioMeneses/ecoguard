# ADR 0011 — Analyst default model is gpt-6-luna

- Status: accepted
- Date: 2026-11-14

## Context

ADR 0006 pinned the analyst to `gpt-4o-mini`. OpenAI now shuts that snapshot down on 14 January 2027 and names `gpt-6-luna` as the replacement ([deprecations](https://developers.openai.com/api/docs/deprecations)).

The interactive analyst uses the Standard speed tier and stays under the short-context threshold (272,000 input tokens). Those rates, in USD per 1,000,000 tokens, are the `gpt-6-luna` row on the [pricing page](https://developers.openai.com/api/docs/pricing):

| | Input | Output | Cached input | 30-minute cache write | 12-hour cache write |
| --- | --- | --- | --- | --- | --- |
| Short context | 0.10 | 0.50 | 0.01 | 0.125 | 0.25 |
| Long context | 0.20 | 0.75 | 0.02 | 0.25 | 0.50 |

`CostGuard` holds that row. `standardUsd` prices a short-context call. A prompt over 272,000 input tokens is the long-context row for the whole request.

## Decision

The Spring AI chat model is `${OPENAI_MODEL:gpt-6-luna}`. Unset, it is `gpt-6-luna`. Set `OPENAI_MODEL` to pin another id. The cost-guard constants stay on the Standard `gpt-6-luna` row above until a later change updates both the constant and this table.

## Consequences

- ADR 0006 still describes the tool boundary. Its model name is superseded here.
- Batch, Flex, Fast, and Ultrafast prices are not the analyst default and are not in `CostGuard`.
