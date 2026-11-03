import type { Metadata } from "next";
import { Inter, JetBrains_Mono } from "next/font/google";
import { headers } from "next/headers";
import { AppShell } from "@/features/shell/components/app-shell";
import "./globals.css";

const inter = Inter({ subsets: ["latin"], variable: "--font-inter", display: "swap" });
const mono = JetBrains_Mono({ subsets: ["latin"], variable: "--font-mono", display: "swap" });

const themeBoot = `(function(){try{var s=localStorage.getItem("ecoguard-theme");var t=s||(window.matchMedia("(prefers-color-scheme: light)").matches?"light":"dark");if(t==="light")document.documentElement.classList.add("light");}catch(e){}})();`;

export const metadata: Metadata = {
  title: "EcoGuard",
  description: "City risk intelligence for São Paulo.",
  icons: {
    icon: [
      { url: "/favicon.svg", type: "image/svg+xml" },
      { url: "/favicon.ico", sizes: "any" },
      { url: "/logo/v2/png/favicon-16.png", sizes: "16x16", type: "image/png" },
      { url: "/logo/v2/favicon-32.png", sizes: "32x32", type: "image/png" },
    ],
    apple: [{ url: "/logo/v2/png/favicon-180-apple-touch.png", sizes: "180x180" }],
  },
};

export default async function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  const nonce = (await headers()).get("x-nonce") ?? undefined;
  return (
    <html lang="en" className={`${inter.variable} ${mono.variable}`} suppressHydrationWarning>
      <head>
        <script nonce={nonce} dangerouslySetInnerHTML={{ __html: themeBoot }} />
      </head>
      <body>
        <AppShell>{children}</AppShell>
      </body>
    </html>
  );
}
