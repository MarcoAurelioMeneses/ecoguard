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
    return <LoadingState label="Carregando o distrito…" />;
  }
  if (state.status === "missing") {
    return (
      <div className="state">
        <p>Este código não está na camada carregada.</p>
        <Link href="/map">Voltar ao mapa</Link>
      </div>
    );
  }
  if (state.status === "error") {
    return <ErrorState message={state.message} />;
  }
  return (
    <article className="stack">
      <h2>{state.data.name}</h2>
      <p className="num">Código {state.data.code}</p>
      <RiskMark level={state.data.score?.overallClass ?? "nodata"} />
      <p className="meta">
        Área {state.data.areaKm2 ?? "desconhecida"} km². Fonte {state.data.source}. Atualizado em{" "}
        {state.data.sourceUpdatedAt ?? "data desconhecida"}.
      </p>
      {state.data.score?.overall == null ? (
        <p className="meta">As notas deste distrito ainda não foram calculadas.</p>
      ) : (
        <div className="kpi-row">
          <article className="kpi">
            <h3>Geral</h3>
            <p className="num kpi-value">{formatScore(state.data.score.overall)}</p>
            <p className="meta">Publicada quando cada pilar chega a 50% de cobertura</p>
          </article>
          <article className="kpi">
            <h3>Ambiental</h3>
            <p className="num kpi-value">{formatScore(state.data.score.environmental)}</p>
            <p className="meta">Cobertura {coverage(state.data.score.environmentalCoverage)}</p>
          </article>
          <article className="kpi">
            <h3>Saúde</h3>
            <p className="num kpi-value">{formatScore(state.data.score.health)}</p>
            <p className="meta">Cobertura {coverage(state.data.score.healthCoverage)}. Dengue municipal.</p>
          </article>
          <article className="kpi">
            <h3>Social</h3>
            <p className="num kpi-value">{formatScore(state.data.score.social)}</p>
            <p className="meta">Cobertura {coverage(state.data.score.socialCoverage)}</p>
          </article>
        </div>
      )}
      <Link href="/map">Voltar ao mapa</Link>
    </article>
  );
}
