/**
 * Screen plan. `/` is screen 00, the product entry.
 * Routes render the app, not the design mockups and not mockup numbers.
 */
export const screens = [
  { id: "00", href: "/", title: "What is EcoGuard" },
  { id: "01", href: "/overview", title: "Overview" },
  { id: "02", href: "/map", title: "Risk map" },
  { id: "03", href: "/districts", title: "District detail" },
  { id: "04", href: "/analyst", title: "AI City Analyst" },
  { id: "05", href: "/alerts", title: "Alerts" },
  { id: "06", href: "/sources", title: "Sources and methodology" },
] as const;

export type ScreenId = (typeof screens)[number]["id"];

export function screenById(id: ScreenId) {
  const screen = screens.find((item) => item.id === id);
  if (!screen) {
    throw new Error(`unknown screen ${id}`);
  }
  return screen;
}
