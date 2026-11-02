export type RiskClass = "baixo" | "moderado" | "alto" | "critico" | "nodata";

const label: Record<RiskClass, string> = {
  baixo: "Low",
  moderado: "Moderate",
  alto: "High",
  critico: "Critical",
  nodata: "No data",
};

export function RiskMark({ level }: { level: RiskClass }) {
  return (
    <span className={`risk risk-${level}`}>
      <span className={`risk-shape shape-${level}`} aria-hidden="true" />
      {label[level]}
    </span>
  );
}
