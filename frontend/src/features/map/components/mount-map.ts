import { setWorkerUrl, type Map as MapLibreMap } from "maplibre-gl";
import { boundsOf } from "@/features/district/model/bounds";
import type { District } from "@/features/district/model/district";

let workerConfigured = false;

function token(styles: CSSStyleDeclaration, name: string): string {
  const value = styles.getPropertyValue(name).trim();
  if (!value) {
    throw new Error(`missing design token ${name}`);
  }
  return value;
}

export async function mountDistrictMap(container: HTMLElement, districts: District[]): Promise<MapLibreMap> {
  const maplibre = await import("maplibre-gl");
  if (!workerConfigured) {
    setWorkerUrl("/maplibre-gl-worker.mjs");
    workerConfigured = true;
  }
  const styles = getComputedStyle(container);
  const map = new maplibre.Map({
    container,
    style: {
      version: 8,
      sources: {},
      layers: [{ id: "bg", type: "background", paint: { "background-color": token(styles, "--bg") } }],
    },
    center: [-46.63, -23.55],
    zoom: 9,
    attributionControl: false,
  });
  map.addControl(new maplibre.NavigationControl({ showCompass: false }), "top-right");
  map.on("load", () => {
    map.addSource("districts", {
      type: "geojson",
      data: {
        type: "FeatureCollection",
        features: districts.map((district) => ({
          type: "Feature" as const,
          properties: { code: district.code, name: district.name },
          geometry: district.geometry,
        })),
      },
    });
    map.addLayer({
      id: "district-fill",
      type: "fill",
      source: "districts",
      paint: { "fill-color": token(styles, "--nodata"), "fill-opacity": 0.95 },
    });
    map.addLayer({
      id: "district-line",
      type: "line",
      source: "districts",
      paint: { "line-color": token(styles, "--muted"), "line-width": 1.25, "line-dasharray": [2, 1] },
    });
    const bounds = boundsOf(districts);
    if (bounds) {
      map.fitBounds(bounds, { padding: 28, animate: false });
    }
  });
  return map;
}
