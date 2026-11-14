export type RiskClass = "baixo" | "moderado" | "alto" | "critico" | "nodata";

const label: Record<RiskClass, string> = {
  baixo: "Baixo",
  moderado: "Moderado",
  alto: "Alto",
  critico: "Crítico",
  nodata: "Sem dado",
};

export function RiskMark({ level }: { level: RiskClass }) {
  return (
    <span className={`risk risk-${level}`}>
      <span className={`risk-shape shape-${level}`} aria-hidden="true" />
      {label[level]}
    </span>
  );
}
