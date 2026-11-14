import Link from "next/link";
import { IconAbout, IconAlert, IconChat, IconMap, IconOverview, IconSources } from "@/components/ui/icons";
import { ThemeToggle } from "@/components/ui/theme-toggle";
import { BrandMark } from "./brand-mark";
import { NavCurrent } from "./nav-current";
import { Topbar } from "./topbar";

const groups = [
  {
    label: "Produto",
    links: [{ href: "/", label: "O que é o EcoGuard", icon: <IconAbout /> }],
  },
  {
    label: "Análise",
    links: [
      { href: "/overview", label: "Visão geral", icon: <IconOverview /> },
      { href: "/map", label: "Mapa de risco", icon: <IconMap /> },
      { href: "/analyst", label: "Analista de IA", icon: <IconChat /> },
      { href: "/alerts", label: "Alertas", icon: <IconAlert /> },
    ],
  },
  {
    label: "Transparência",
    links: [{ href: "/sources", label: "Fontes e metodologia", icon: <IconSources /> }],
  },
];

export function AppShell({ children }: { children: React.ReactNode }) {
  return (
    <div className="app">
      <a className="skip" href="#content">
        Pular para o conteúdo
      </a>
      <aside className="sidebar">
        <div className="brand">
          <BrandMark />
          <p className="wordmark">
            <span className="wordmark-eco">Eco</span>
            <span className="wordmark-guard">Guard</span>
          </p>
        </div>
        <nav className="nav" aria-label="Principal">
          {groups.map((group) => (
            <div key={group.label}>
              <p className="nav-group">{group.label}</p>
              {group.links.map((link) => (
                <Link key={link.href} href={link.href}>
                  {link.icon}
                  {link.label}
                </Link>
              ))}
            </div>
          ))}
        </nav>
        <p className="sidebar-foot">
          São Paulo · 96 distritos. Grão diário e semanal. Weather data by Open-Meteo.com. Air quality data by
          Open-Meteo.com (CC BY 4.0). INPE. IBGE. COVISA: use with source citation; licence not verified.
        </p>
        <ThemeToggle />
      </aside>
      <div className="workspace">
        <Topbar />
        <main id="content" className="main">
          <NavCurrent />
          {children}
        </main>
      </div>
    </div>
  );
}
