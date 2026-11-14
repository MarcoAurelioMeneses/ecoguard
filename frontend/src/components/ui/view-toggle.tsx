"use client";

export function ViewToggle({
  table,
  onChange,
  graphicLabel = "Gráfico",
}: {
  table: boolean;
  onChange: (table: boolean) => void;
  graphicLabel?: string;
}) {
  return (
    <div className="segment" role="group" aria-label="Apresentação">
      <button
        type="button"
        className={table ? "control" : "control is-on"}
        aria-pressed={!table}
        onClick={() => onChange(false)}
      >
        {graphicLabel}
      </button>
      <button
        type="button"
        className={table ? "control is-on" : "control"}
        aria-pressed={table}
        onClick={() => onChange(true)}
      >
        Ver como tabela
      </button>
    </div>
  );
}
