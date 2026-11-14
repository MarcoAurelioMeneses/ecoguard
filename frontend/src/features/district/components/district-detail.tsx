"use client";

import Link from "next/link";
import { ErrorState, LoadingState } from "@/components/ui/states";
import { RiskMark } from "@/components/ui/risk-mark";
import { useDistrict } from "../hooks/use-district";

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
        {state.data.sourceUpdatedAt ?? "data desconhecida"}. As notas deste distrito ainda não foram
        calculadas.
      </p>
      <Link href="/map">Voltar ao mapa</Link>
    </article>
  );
}
