import { copyFileSync } from "node:fs";
import { createRequire } from "node:module";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const require = createRequire(import.meta.url);
const worker = require.resolve("maplibre-gl/dist/maplibre-gl-worker.mjs");
const root = dirname(fileURLToPath(import.meta.url));
copyFileSync(worker, join(root, "../public/maplibre-gl-worker.mjs"));
