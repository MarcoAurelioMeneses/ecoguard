"use client";

import Link from "next/link";
import { useEffect, useRef, useState } from "react";
import { ViewToggle } from "@/components/ui/view-toggle";
import { DistrictTable } from "@/features/district/components/district-table";
import type { District } from "@/features/district/model/district";
import { events, track } from "@/lib/analytics/tracker";
import { mountDistrictMap } from "./mount-map";
import "maplibre-gl/dist/maplibre-gl.css";

const layers = [
  { id: "environmental", label: "Environmental" },
  { id: "health", label: "Health" },
  { id: "social", label: "Social" },
  { id: "overall", label: "Overall" },
] as const;

export function RiskMap({ districts }: { districts: District[] }) {
  const [table, setTable] = useState(false);
  const [layer, setLayer] = useState<(typeof layers)[number]["id"]>("overall");
  const host = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (table || !host.current) {
      return;
    }
    let map: { remove: () => void } | undefined;
    let cancelled = false;
    void mountDistrictMap(host.current, districts).then((created) => {
      if (cancelled) {
        created.remove();
        return;
      }
      map = created;
    });
    return () => {
      cancelled = true;
      map?.remove();
    };
  }, [districts, table]);

  function choose(next: (typeof layers)[number]["id"]) {
    setLayer(next);
    track(events.mapLayerViewed, { layer: next });
  }

  return (
    <section className="stack">
      <div className="toolbar">
        <div className="segment" role="group" aria-label="Risk layer">
          {layers.map((item) => (
            <button
              key={item.id}
              type="button"
              className={layer === item.id ? "control is-on" : "control"}
              aria-pressed={layer === item.id}
              onClick={() => choose(item.id)}
            >
              {item.label}
            </button>
          ))}
        </div>
        <ViewToggle table={table} graphicLabel="Map" onChange={setTable} />
      </div>
      <p className="meta">
        Scores are not computed yet. Every district is shown as no data, with a dashed outline so the state is
        not color alone.
      </p>
      {table ? (
        <DistrictTable districts={districts} />
      ) : (
        <div className="map-frame">
          <div
            ref={host}
            className="map-canvas"
            role="application"
            aria-label="São Paulo districts, no score data"
          />
          <p className="map-legend">
            <Link href="/sources">How scores will be calculated</Link>
          </p>
        </div>
      )}
    </section>
  );
}
