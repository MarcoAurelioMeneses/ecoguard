"use client";

import { EmptyState, ErrorState, LoadingState } from "@/components/ui/states";
import { useDistricts } from "@/features/district/hooks/use-districts";
import { RiskMap } from "./risk-map";

export function MapScreen() {
  const { state, reload } = useDistricts();
  return (
    <>
      {state.status === "loading" ? <LoadingState label="Loading the district layer…" /> : null}
      {state.status === "error" ? <ErrorState message={state.message} onRetry={reload} /> : null}
      {state.status === "empty" ? (
        <EmptyState
          title="No districts loaded"
          detail="The GeoSampa collector has not written the 96 districts yet."
        />
      ) : null}
      {state.status === "ready" ? <RiskMap districts={state.data} /> : null}
    </>
  );
}
