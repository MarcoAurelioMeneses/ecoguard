# Multi-stage build. The runtime image is Node 22 and does not run as root.
FROM node:22-bookworm-slim AS build

WORKDIR /src
RUN corepack enable && corepack prepare pnpm@10.33.4 --activate
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
COPY frontend/package.json frontend/package.json
RUN pnpm install --frozen-lockfile
COPY frontend frontend
ARG NEXT_PUBLIC_API_BASE_URL
ARG NEXT_PUBLIC_POSTHOG_KEY
ARG NEXT_PUBLIC_POSTHOG_HOST
ENV NEXT_PUBLIC_API_BASE_URL=$NEXT_PUBLIC_API_BASE_URL \
    NEXT_PUBLIC_POSTHOG_KEY=$NEXT_PUBLIC_POSTHOG_KEY \
    NEXT_PUBLIC_POSTHOG_HOST=$NEXT_PUBLIC_POSTHOG_HOST \
    NEXT_TELEMETRY_DISABLED=1
RUN pnpm --filter @ecoguard/web build

FROM node:22-bookworm-slim

WORKDIR /app
ENV NODE_ENV=production \
    HOSTNAME=0.0.0.0 \
    NEXT_TELEMETRY_DISABLED=1
COPY --from=build --chown=node:node /src/frontend/.next/standalone ./
COPY --from=build --chown=node:node /src/frontend/.next/static ./frontend/.next/static
COPY --from=build --chown=node:node /src/frontend/public ./frontend/public
USER node

EXPOSE 3000
CMD ["node", "frontend/server.js"]
