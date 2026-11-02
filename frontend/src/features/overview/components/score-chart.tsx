export function ScoreChart({ districtCount }: { districtCount: number }) {
  return (
    <figure className="kpi">
      <figcaption>
        {districtCount} districts are loaded from GeoSampa. Score series stay empty until the score job runs.
      </figcaption>
      <svg className="empty-chart" role="img" aria-label="No score series" viewBox="0 0 640 120">
        <line x1="16" y1="96" x2="624" y2="96" stroke="currentColor" />
        <text x="16" y="56" fill="currentColor" fontSize="14">
          No data
        </text>
      </svg>
    </figure>
  );
}
