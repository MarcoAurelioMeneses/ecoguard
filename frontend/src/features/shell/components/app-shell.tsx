import Image from "next/image";
import Link from "next/link";
import { IconAbout, IconAlert, IconChat, IconMap, IconOverview, IconSources } from "@/components/ui/icons";
import { ThemeToggle } from "@/components/ui/theme-toggle";
import { NavCurrent } from "./nav-current";
import { Topbar } from "./topbar";

const groups = [
  {
    label: "Product",
    links: [{ href: "/", label: "What is EcoGuard", icon: <IconAbout /> }],
  },
  {
    label: "Analysis",
    links: [
      { href: "/overview", label: "Overview", icon: <IconOverview /> },
      { href: "/map", label: "Risk map", icon: <IconMap /> },
      { href: "/analyst", label: "AI City Analyst", icon: <IconChat /> },
      { href: "/alerts", label: "Alerts", icon: <IconAlert /> },
    ],
  },
  {
    label: "Transparency",
    links: [{ href: "/sources", label: "Sources and methodology", icon: <IconSources /> }],
  },
];

export function AppShell({ children }: { children: React.ReactNode }) {
  return (
    <div className="app">
      <a className="skip" href="#content">
        Skip to content
      </a>
      <aside className="sidebar">
        <div className="brand">
          <Image src="/logo.svg" alt="" width={32} height={32} unoptimized />
          <p className="wordmark">
            Eco<span className="wordmark-accent">Guard</span>
          </p>
        </div>
        <nav className="nav" aria-label="Primary">
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
        <p className="sidebar-foot">São Paulo · 96 districts. Daily and weekly grain.</p>
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
