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
        <h2>Indicadores</h2>
        <ViewToggle table={table} onChange={setTable} />
      </div>
      {state.status === "loading" ? <LoadingState label="Carregando distritos…" /> : null}
      {state.status === "error" ? <ErrorState message={state.message} onRetry={reload} /> : null}
      {state.status === "empty" ? (
        <EmptyState
          title="Nenhum distrito carregado"
          detail="Rode o coletor do GeoSampa para preencher os 96 distritos municipais."
        />
      ) : null}
      {state.status === "ready" ? (
        <>
          <div className="kpi-row">
            <article className="kpi">
              <h3>Geral</h3>
              <p className="num kpi-value">{formatScore(state.data[0]?.score?.overall)}</p>
              <RiskMark level={state.data[0]?.score?.overallClass ?? "nodata"} />
            </article>
            <article className="kpi">
              <h3>Previsão de calor</h3>
              <p className="num kpi-value">{formatScore(state.data[0]?.score?.heat)}</p>
              <p className="meta">Previsão Open-Meteo da cidade, não uma observação de estação</p>
            </article>
            <article className="kpi">
              <h3>Previsão de chuva</h3>
              <p className="num kpi-value">{formatScore(state.data[0]?.score?.rain)}</p>
              <p className="meta">A mesma referência da cidade para todos os distritos</p>
            </article>
            <article className="kpi">
              <h3>Dengue</h3>
              <p className="num kpi-value">{formatScore(state.data[0]?.score?.dengue)}</p>
              <RiskMark level={state.data[0]?.score?.healthClass ?? "nodata"} />
              <p className="meta">InfoDengue municipal, não uma taxa por distrito</p>
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
