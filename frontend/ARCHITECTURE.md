# Frontend architecture

The web app is a Next.js App Router project. It does not import the Java backend. It calls the HTTP API through one typed client.

## Folders

| Path                                   | May contain                                           | Must not contain                  |
| -------------------------------------- | ----------------------------------------------------- | --------------------------------- |
| `src/app`                              | Route files that compose a screen                     | Fetch, business rules, large JSX  |
| `src/features/<feature>/components`    | Screen UI                                             | `fetch`, another feature's `api`  |
| `src/features/<feature>/hooks`         | View state that calls that feature's `api`            | Direct use of `src/lib/api`       |
| `src/features/<feature>/api`           | Mapping from DTOs to that feature's model             | React components                  |
| `src/features/<feature>/model`         | Domain types and pure functions                       | `fetch`, React                    |
| `src/components/ui`                    | Small accessible primitives styled with tokens        | Feature imports, hardcoded colors |
| `src/lib/api`                          | The only `fetch` calls, OpenAPI DTOs, generated types | Feature UI                        |
| `src/lib/analytics`, `src/lib/routing` | Shared non-visual helpers                             | Components                        |

`app/` routes stay server components unless a route has to read the URL in the browser. Client components are the islands under `features/*/components` and `hooks` that use state or the DOM.

## Types

`src/lib/api/dto.ts` is the wire shape. Feature `model` types are what the UI reads. `api` modules map one to the other. A component never imports a DTO.

## Checks

ESLint forbids `fetch` outside `src/lib/api`, caps a component at 200 lines, and restricts imports: UI primitives cannot reach features; feature components cannot reach `src/lib/api` or a feature `api` module; hooks cannot reach `src/lib/api` (they call their own feature `api`). Prettier formats `src`. TypeScript is `strict`. Vitest and Testing Library cover hooks and components. `jest-axe` checks the states and the analyst form.

## What the screens do not invent

Scores, alerts, and chart series stay in an explicit no-data state. District polygons come from `GET /districts` after the GeoSampa job. The 2016 design GeoJSON is not a map source. MapLibre reads colors from `--bg`, `--nodata`, and `--muted`.
