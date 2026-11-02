import type { Metadata } from "next";
import { Inter, JetBrains_Mono } from "next/font/google";
import { AppShell } from "@/features/shell/components/app-shell";
import "./globals.css";

const inter = Inter({ subsets: ["latin"], variable: "--font-inter", display: "swap" });
const mono = JetBrains_Mono({ subsets: ["latin"], variable: "--font-mono", display: "swap" });

const themeBoot = `(function(){try{var s=localStorage.getItem("ecoguard-theme");var t=s||(window.matchMedia("(prefers-color-scheme: light)").matches?"light":"dark");if(t==="light")document.documentElement.classList.add("light");}catch(e){}})();`;

export const metadata: Metadata = {
  title: "EcoGuard",
  description: "City risk intelligence for São Paulo.",
  icons: { icon: "/logo.svg" },
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en" className={`${inter.variable} ${mono.variable}`} suppressHydrationWarning>
      <head>
        <script dangerouslySetInnerHTML={{ __html: themeBoot }} />
      </head>
      <body>
        <AppShell>{children}</AppShell>
      </body>
    </html>
  );
}
