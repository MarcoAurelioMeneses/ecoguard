"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const titles: Record<string, string> = {
  "/": "O que é o EcoGuard",
  "/overview": "Visão geral",
  "/map": "Mapa de risco",
  "/districts": "Distritos",
  "/analyst": "Analista de IA",
  "/alerts": "Alertas",
  "/sources": "Fontes e metodologia",
};

export function Topbar() {
  const pathname = usePathname();
  const district = pathname.match(/^\/districts\/(.+)$/);
  const title = district ? "Distrito" : (titles[pathname] ?? "EcoGuard");
  return (
    <header className="topbar">
      <div>
        {district ? (
          <p className="crumb">
            <Link href="/map">Mapa de risco</Link>
            <span aria-hidden="true">/</span>
            <span>{decodeURIComponent(district[1] ?? "")}</span>
          </p>
        ) : null}
        <h1>{title}</h1>
      </div>
    </header>
  );
}
