"use client";

import { EmptyState, ErrorState, LoadingState } from "@/components/ui/states";
import { useDistricts } from "@/features/district/hooks/use-districts";
import { RiskMap } from "./risk-map";

export function MapScreen() {
  const { state, reload } = useDistricts();
  return (
    <>
      {state.status === "loading" ? <LoadingState label="Carregando a camada de distritos…" /> : null}
      {state.status === "error" ? <ErrorState message={state.message} onRetry={reload} /> : null}
      {state.status === "empty" ? (
        <EmptyState
          title="Nenhum distrito carregado"
          detail="O coletor do GeoSampa ainda não gravou os 96 distritos."
        />
      ) : null}
      {state.status === "ready" ? <RiskMap districts={state.data} /> : null}
    </>
  );
}
