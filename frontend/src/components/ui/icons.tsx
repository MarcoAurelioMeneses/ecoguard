type IconProps = { size?: number };

function svg(path: string, size = 22) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.75"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d={path} />
    </svg>
  );
}

export function IconAbout({ size }: IconProps) {
  return svg("M12 3l8 4v6c0 4.5-3.2 7.8-8 9-4.8-1.2-8-4.5-8-9V7l8-4z", size);
}
export function IconOverview({ size }: IconProps) {
  return svg("M4 13h6V4H4v9zm10 7h6V4h-6v16zM4 20h6v-5H4v5z", size);
}
export function IconMap({ size }: IconProps) {
  return svg("M9 4l-5 2v14l5-2 6 2 5-2V4l-5 2-6-2zm0 0v14m6-12v14", size);
}
export function IconChat({ size }: IconProps) {
  return svg("M5 6h14v9H8l-3 3V6z", size);
}
export function IconAlert({ size }: IconProps) {
  return svg("M12 4l8 14H4L12 4zm0 5v4m0 3h.01", size);
}
export function IconSources({ size }: IconProps) {
  return svg("M6 5h12v14H6V5zm3 4h6M9 13h6", size);
}
