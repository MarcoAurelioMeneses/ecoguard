"use client";

import { useEffect, useState } from "react";

type Theme = "dark" | "light";

function readTheme(): Theme {
  return document.documentElement.classList.contains("light") ? "light" : "dark";
}

export function ThemeToggle() {
  const [theme, setTheme] = useState<Theme>("dark");

  useEffect(() => {
    setTheme(readTheme());
  }, []);

  function toggle() {
    const next: Theme = readTheme() === "light" ? "dark" : "light";
    document.documentElement.classList.toggle("light", next === "light");
    localStorage.setItem("ecoguard-theme", next);
    setTheme(next);
  }

  return (
    <button type="button" className="theme-toggle" onClick={toggle}>
      {theme === "light" ? "Dark theme" : "Light theme"}
    </button>
  );
}
