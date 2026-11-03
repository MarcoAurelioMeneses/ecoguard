"use client";

import Link from "next/link";
import { ErrorState, LoadingState } from "@/components/ui/states";
import { RiskMark } from "@/components/ui/risk-mark";
import { useDistrict } from "../hooks/use-district";

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
        {state.data.sourceUpdatedAt ?? "unknown"}. Scores for this district have not been computed.
      </p>
      <Link href="/map">Back to the map</Link>
    </article>
  );
}
