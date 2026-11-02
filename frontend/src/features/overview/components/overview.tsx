"use client";

import { useState } from "react";
import { RiskMark } from "@/components/ui/risk-mark";
import { EmptyState, ErrorState, LoadingState } from "@/components/ui/states";
import { ViewToggle } from "@/components/ui/view-toggle";
import { DistrictTable } from "@/features/district/components/district-table";
import { useDistricts } from "@/features/district/hooks/use-districts";
import { ScoreChart } from "./score-chart";

const kpis = ["Overall risk", "Heat", "Air quality", "Dengue incidence", "Active alerts"];

export function Overview() {
  const { state, reload } = useDistricts();
  const [table, setTable] = useState(false);

  return (
    <section className="stack">
      <div className="toolbar">
        <h1>City overview</h1>
        <ViewToggle table={table} onChange={setTable} />
      </div>
      {state.status === "loading" ? <LoadingState label="Loading districts…" /> : null}
      {state.status === "error" ? <ErrorState message={state.message} onRetry={reload} /> : null}
      {state.status === "empty" ? (
        <EmptyState
          title="No districts loaded"
          detail="Run the GeoSampa collector to fill the 96 municipal districts."
        />
      ) : null}
      {state.status === "ready" ? (
        <>
          <div className="kpi-row">
            {kpis.map((label) => (
              <article key={label} className="kpi">
                <h3>{label}</h3>
                <RiskMark level="nodata" />
                <p className="meta">No measurement yet</p>
              </article>
            ))}
          </div>
          {table ? (
            <DistrictTable districts={state.data} />
          ) : (
            <ScoreChart districtCount={state.data.length} />
          )}
        </>
      ) : null}
    </section>
  );
}
