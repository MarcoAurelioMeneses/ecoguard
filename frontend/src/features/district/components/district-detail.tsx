"use client";

import Link from "next/link";
import { ErrorState, LoadingState } from "@/components/ui/states";
import { RiskMark } from "@/components/ui/risk-mark";
import { useDistrict } from "../hooks/use-district";

function formatScore(value: number | null | undefined): string {
  return value == null ? "—" : value.toFixed(1);
}

function coverage(value: number | null | undefined): string {
  return value == null ? "—" : `${Math.round(value * 100)}%`;
}

export function DistrictDetail({ code }: { code: string }) {
  const state = useDistrict(code);

  if (state.status === "loading") {
    return <LoadingState label="Loading district…" />;
  }
  if (state.status === "missing") {
    return (
      <div className="state">
        <p>This district code is not in the loaded layer.</p>
        <Link href="/map">Back to the map</Link>
      </div>
    );
  }
  if (state.status === "error") {
    return <ErrorState message={state.message} />;
  }
  return (
    <article className="stack">
      <h2>{state.data.name}</h2>
      <p className="num">Code {state.data.code}</p>
      <RiskMark level={state.data.score?.overallClass ?? "nodata"} />
      <p className="meta">
        Area {state.data.areaKm2 ?? "unknown"} km². Source {state.data.source}. Updated{" "}
        {state.data.sourceUpdatedAt ?? "unknown"}.
      </p>
      {state.data.score?.overall == null ? (
        <p className="meta">Scores for this district have not been computed.</p>
      ) : (
        <div className="kpi-row">
          <article className="kpi">
            <h3>Overall</h3>
            <p className="num kpi-value">{formatScore(state.data.score.overall)}</p>
            <p className="meta">Published when every pillar reaches 50% coverage</p>
          </article>
          <article className="kpi">
            <h3>Environmental</h3>
            <p className="num kpi-value">{formatScore(state.data.score.environmental)}</p>
            <p className="meta">Coverage {coverage(state.data.score.environmentalCoverage)}</p>
          </article>
          <article className="kpi">
            <h3>Health</h3>
            <p className="num kpi-value">{formatScore(state.data.score.health)}</p>
            <p className="meta">Coverage {coverage(state.data.score.healthCoverage)}. Municipal dengue.</p>
          </article>
          <article className="kpi">
            <h3>Social</h3>
            <p className="num kpi-value">{formatScore(state.data.score.social)}</p>
            <p className="meta">Coverage {coverage(state.data.score.socialCoverage)}</p>
          </article>
        </div>
      )}
      <Link href="/map">Back to the map</Link>
    </article>
  );
}
