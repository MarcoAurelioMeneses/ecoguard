"use client";

import { useState } from "react";
import { RiskMark } from "@/components/ui/risk-mark";
import { EmptyState, ErrorState, LoadingState } from "@/components/ui/states";
import { ViewToggle } from "@/components/ui/view-toggle";
import { DistrictTable } from "@/features/district/components/district-table";
import { useDistricts } from "@/features/district/hooks/use-districts";
import { ScoreChart } from "./score-chart";

function formatScore(value: number | null | undefined): string {
  return value == null ? "—" : value.toFixed(0);
}

export function Overview() {
  const { state, reload } = useDistricts();
  const [table, setTable] = useState(false);

  return (
    <section className="stack">
      <div className="toolbar">
        <h2>Indicators</h2>
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
            <article className="kpi">
              <h3>Overall</h3>
              <p className="num kpi-value">{formatScore(state.data[0]?.score?.overall)}</p>
              <RiskMark level={state.data[0]?.score?.overallClass ?? "nodata"} />
            </article>
            <article className="kpi">
              <h3>Heat forecast</h3>
              <p className="num kpi-value">{formatScore(state.data[0]?.score?.heat)}</p>
              <p className="meta">City Open-Meteo forecast, not a station observation</p>
            </article>
            <article className="kpi">
              <h3>Rain forecast</h3>
              <p className="num kpi-value">{formatScore(state.data[0]?.score?.rain)}</p>
              <p className="meta">Same city reference for every district</p>
            </article>
            <article className="kpi">
              <h3>Dengue</h3>
              <p className="num kpi-value">{formatScore(state.data[0]?.score?.dengue)}</p>
              <RiskMark level={state.data[0]?.score?.healthClass ?? "nodata"} />
              <p className="meta">Municipal InfoDengue, not a district rate</p>
            </article>
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
