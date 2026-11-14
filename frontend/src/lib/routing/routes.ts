/**
 * Screen plan. `/` is screen 00, the product entry.
 * Routes render the app, not the design mockups and not mockup numbers.
 */
export const screens = [
  { id: "00", href: "/", title: "O que é o EcoGuard" },
  { id: "01", href: "/overview", title: "Visão geral" },
  { id: "02", href: "/map", title: "Mapa de risco" },
  { id: "03", href: "/districts", title: "Detalhe do distrito" },
  { id: "04", href: "/analyst", title: "Analista de IA" },
  { id: "05", href: "/alerts", title: "Alertas" },
  { id: "06", href: "/sources", title: "Fontes e metodologia" },
] as const;

export type ScreenId = (typeof screens)[number]["id"];

export function screenById(id: ScreenId) {
  const screen = screens.find((item) => item.id === id);
  if (!screen) {
    throw new Error(`unknown screen ${id}`);
  }
  return screen;
}
