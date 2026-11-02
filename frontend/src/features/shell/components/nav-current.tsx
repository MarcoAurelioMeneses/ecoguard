"use client";

import { usePathname } from "next/navigation";
import { useEffect } from "react";

export function NavCurrent() {
  const pathname = usePathname();
  useEffect(() => {
    document.querySelectorAll<HTMLAnchorElement>(".nav a").forEach((link) => {
      const href = link.getAttribute("href");
      const current = href === pathname || (href !== "/" && pathname.startsWith(`${href}/`));
      if (current) {
        link.setAttribute("aria-current", "page");
      } else {
        link.removeAttribute("aria-current");
      }
    });
  }, [pathname]);
  return null;
}
