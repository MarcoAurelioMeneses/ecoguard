# Frontend

Next.js app. Screens are features. This build is the theme, the logo, and the route plan.

| Route | Feature | Screen |
| --- | --- | --- |
| `/` | `features/about` | 00 What is EcoGuard, the entry |
| `/overview` | `features/shell` placeholder | 01 |
| `/map` | placeholder | 02, linked from 00 |
| `/districts` | placeholder | 03 |
| `/analyst` | placeholder | 04, linked from 00 |
| `/alerts` | placeholder | 05 |
| `/sources` | placeholder | 06 |

`shared/styles/tokens.css` and `public/logo*.svg` are copies of `design/`. `scripts/check-design-sync.sh` checks them. `shared/analytics` validates product events and does not send them. `shared/routing/routes.ts` is the route table.

Light-theme accent is for the wordmark only (large text). Links use the body text color.

Do not render numbers from `design/html` or polygons from `design/src/distritos-sp.geojson`.

```bash
pnpm --filter @ecoguard/web dev
```
