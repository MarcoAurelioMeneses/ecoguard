import type { Metadata } from "next";
import { Inter, JetBrains_Mono } from "next/font/google";
import Image from "next/image";
import { ThemeToggle } from "@/features/shell/ui/theme-toggle";
import "./globals.css";

const inter = Inter({ subsets: ["latin"], variable: "--font-inter", display: "swap" });
const mono = JetBrains_Mono({ subsets: ["latin"], variable: "--font-mono", display: "swap" });

const themeBoot = `
(function () {
  try {
    var stored = localStorage.getItem("ecoguard-theme");
    var theme = stored || (window.matchMedia("(prefers-color-scheme: light)").matches ? "light" : "dark");
    if (theme === "light") document.documentElement.classList.add("light");
  } catch (e) {}
})();
`;

export const metadata: Metadata = {
  title: "EcoGuard",
  description: "City risk intelligence for São Paulo.",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en" className={`${inter.variable} ${mono.variable}`} suppressHydrationWarning>
      <head>
        <script dangerouslySetInnerHTML={{ __html: themeBoot }} />
      </head>
      <body>
        <div className="shell">
          <main className="card">
            <div className="brand">
              <Image className="logo-dark" src="/logo.svg" alt="" width={48} height={48} unoptimized />
              <Image
                className="logo-light"
                src="/logo-light.svg"
                alt=""
                width={48}
                height={48}
                unoptimized
              />
              <h1 className="wordmark">
                Eco<span className="wordmark-accent">Guard</span>
              </h1>
            </div>
            {children}
            <ThemeToggle />
          </main>
        </div>
      </body>
    </html>
  );
}
