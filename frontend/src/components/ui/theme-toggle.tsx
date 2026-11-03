"use client";

import { useSyncExternalStore } from "react";

type Theme = "dark" | "light";

const listeners = new Set<() => void>();

function emit() {
  listeners.forEach((listener) => listener());
}

function subscribe(listener: () => void) {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

function snapshot(): Theme {
  return document.documentElement.classList.contains("light") ? "light" : "dark";
}

export function ThemeToggle() {
  const theme = useSyncExternalStore(subscribe, snapshot, () => "dark" as const);

  function toggle() {
    const next: Theme = snapshot() === "light" ? "dark" : "light";
    document.documentElement.classList.toggle("light", next === "light");
    localStorage.setItem("ecoguard-theme", next);
    emit();
  }

  return (
    <button type="button" className="theme-toggle" onClick={toggle}>
      {theme === "light" ? "Dark theme" : "Light theme"}
    </button>
  );
}
