"use client";

import { EmptyState, ErrorState, LoadingState } from "@/components/ui/states";
import { useDistricts } from "../hooks/use-districts";
import { DistrictTable } from "./district-table";

export function DistrictIndex() {
  const { state, reload } = useDistricts();
  return (
    <>
      {state.status === "loading" ? <LoadingState label="Carregando distritos…" /> : null}
      {state.status === "error" ? <ErrorState message={state.message} onRetry={reload} /> : null}
      {state.status === "empty" ? (
        <EmptyState
          title="Nenhum distrito carregado"
          detail="Rode o coletor do GeoSampa antes de abrir um distrito."
        />
      ) : null}
      {state.status === "ready" ? <DistrictTable districts={state.data} /> : null}
    </>
  );
}
