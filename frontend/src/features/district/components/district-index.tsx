"use client";

import { EmptyState, ErrorState, LoadingState } from "@/components/ui/states";
import { useDistricts } from "../hooks/use-districts";
import { DistrictTable } from "./district-table";

export function DistrictIndex() {
  const { state, reload } = useDistricts();
  return (
    <>
      <h1>Districts</h1>
      {state.status === "loading" ? <LoadingState label="Loading districts…" /> : null}
      {state.status === "error" ? <ErrorState message={state.message} onRetry={reload} /> : null}
      {state.status === "empty" ? (
        <EmptyState
          title="No districts loaded"
          detail="Run the GeoSampa collector before opening a district."
        />
      ) : null}
      {state.status === "ready" ? <DistrictTable districts={state.data} /> : null}
    </>
  );
}
