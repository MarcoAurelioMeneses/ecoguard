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

function percent(value: number | null | undefined): number | null {
  return value == null ? null : value * 100;
}

export function Overview() {
  const { state, reload } = useDistricts();
  const [table, setTable] = useState(false);

  return (
    <section className="stack">
      <div className="toolbar">
        <h2>Indicators for {state.data[0]?.name ?? "the city"}</h2>
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
              <p className="meta">Published when every pillar reaches 50% coverage</p>
            </article>
            <article className="kpi">
              <h3>Environmental</h3>
              <p className="num kpi-value">{formatScore(state.data[0]?.score?.environmental)}</p>
              <RiskMark level={state.data[0]?.score?.environmentalClass ?? "nodata"} />
              <p className="meta">
                Coverage {formatScore(percent(state.data[0]?.score?.environmentalCoverage))}%
              </p>
            </article>
            <article className="kpi">
              <h3>Health</h3>
              <p className="num kpi-value">{formatScore(state.data[0]?.score?.health)}</p>
              <RiskMark level={state.data[0]?.score?.healthClass ?? "nodata"} />
              <p className="meta">
                Coverage {formatScore(percent(state.data[0]?.score?.healthCoverage))}%. Municipal dengue.
                Respiratory data has no anonymous API.
              </p>
            </article>
            <article className="kpi">
              <h3>Social</h3>
              <p className="num kpi-value">{formatScore(state.data[0]?.score?.social)}</p>
              <RiskMark level={state.data[0]?.score?.socialClass ?? "nodata"} />
              <p className="meta">
                Coverage {formatScore(percent(state.data[0]?.score?.socialCoverage))}%. Density and sewer
                network. Census income is not in this release.
              </p>
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
