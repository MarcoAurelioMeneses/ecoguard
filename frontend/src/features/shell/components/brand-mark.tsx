"use client";

import Image from "next/image";
import { useTheme } from "@/components/ui/theme-toggle";

/** 32px uses the full icon. Below 24px the favicon is the simplified mark. */
export function BrandMark() {
  const theme = useTheme();
  const src =
    theme === "light" ? "/logo/v2/transparente/icone-claro.svg" : "/logo/v2/transparente/icone-escuro.svg";
  return <Image className="brand-mark" src={src} alt="" width={32} height={32} unoptimized />;
}
