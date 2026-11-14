export function ScoreChart({ districtCount }: { districtCount: number }) {
  return (
    <figure className="kpi">
      <figcaption>
        {districtCount} distritos carregados do GeoSampa. A série de notas fica vazia até o job de notas
        rodar.
      </figcaption>
      <svg className="empty-chart" role="img" aria-label="Sem série de notas" viewBox="0 0 640 120">
        <line x1="16" y1="96" x2="624" y2="96" stroke="currentColor" />
        <text x="16" y="56" fill="currentColor" fontSize="14">
          Sem dado
        </text>
      </svg>
    </figure>
  );
}
