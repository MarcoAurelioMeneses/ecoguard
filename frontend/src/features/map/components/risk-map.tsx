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
  { id: "environmental", label: "Ambiental" },
  { id: "health", label: "Saúde" },
  { id: "social", label: "Social" },
  { id: "overall", label: "Geral" },
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
    void mountDistrictMap(host.current, districts, layer).then((created) => {
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
  }, [districts, layer, table]);

  function choose(next: (typeof layers)[number]["id"]) {
    setLayer(next);
    track(events.mapLayerViewed, { layer: next });
  }

  return (
    <section className="stack">
      <div className="toolbar">
        <div className="segment" role="group" aria-label="Camada de risco">
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
        <ViewToggle table={table} graphicLabel="Mapa" onChange={setTable} />
      </div>
      <p className="meta">
        As notas ainda não foram calculadas. Cada distrito aparece como sem dado, com contorno tracejado para
        o estado não depender só da cor.
      </p>
      {table ? (
        <DistrictTable districts={districts} />
      ) : (
        <div className="map-frame">
          <div
            ref={host}
            className="map-canvas"
            role="application"
            aria-label="Distritos de São Paulo, sem nota"
          />
          <p className="map-legend">
            <Link href="/sources">Como as notas serão calculadas</Link>
          </p>
        </div>
      )}
    </section>
  );
}
