"use client";

export function ViewToggle({
  table,
  onChange,
  graphicLabel = "Chart",
}: {
  table: boolean;
  onChange: (table: boolean) => void;
  graphicLabel?: string;
}) {
  return (
    <div className="segment" role="group" aria-label="Presentation">
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
        View as table
      </button>
    </div>
  );
}
