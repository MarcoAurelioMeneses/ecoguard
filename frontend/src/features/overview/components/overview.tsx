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
        <h2>Indicadores de {state.status === "ready" ? (state.data[0]?.name ?? "a cidade") : "a cidade"}</h2>
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
              <p className="meta">Publicada quando cada pilar chega a 50% de cobertura</p>
            </article>
            <article className="kpi">
              <h3>Ambiental</h3>
              <p className="num kpi-value">{formatScore(state.data[0]?.score?.environmental)}</p>
              <RiskMark level={state.data[0]?.score?.environmentalClass ?? "nodata"} />
              <p className="meta">
                Cobertura {formatScore(percent(state.data[0]?.score?.environmentalCoverage))}%
              </p>
            </article>
            <article className="kpi">
              <h3>Saúde</h3>
              <p className="num kpi-value">{formatScore(state.data[0]?.score?.health)}</p>
              <RiskMark level={state.data[0]?.score?.healthClass ?? "nodata"} />
              <p className="meta">
                Cobertura {formatScore(percent(state.data[0]?.score?.healthCoverage))}%. Dengue municipal.
                Internações respiratórias não têm API anônima.
              </p>
            </article>
            <article className="kpi">
              <h3>Social</h3>
              <p className="num kpi-value">{formatScore(state.data[0]?.score?.social)}</p>
              <RiskMark level={state.data[0]?.score?.socialClass ?? "nodata"} />
              <p className="meta">
                Cobertura {formatScore(percent(state.data[0]?.score?.socialCoverage))}%. Densidade e rede de
                esgoto. A renda do censo não está nesta versão.
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
