"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const titles: Record<string, string> = {
  "/": "What EcoGuard is",
  "/overview": "Overview",
  "/map": "Risk map",
  "/districts": "Districts",
  "/analyst": "AI City Analyst",
  "/alerts": "Alerts",
  "/sources": "Sources and methodology",
};

export function Topbar() {
  const pathname = usePathname();
  const district = pathname.match(/^\/districts\/(.+)$/);
  const title = district ? "District" : (titles[pathname] ?? "EcoGuard");
  return (
    <header className="topbar">
      <div>
        {district ? (
          <p className="crumb">
            <Link href="/map">Risk map</Link>
            <span aria-hidden="true">/</span>
            <span>{decodeURIComponent(district[1] ?? "")}</span>
          </p>
        ) : null}
        <h1>{title}</h1>
      </div>
    </header>
  );
}
