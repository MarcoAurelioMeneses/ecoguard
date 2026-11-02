"use client";

import { useEffect } from "react";
import { events, track } from "@/lib/analytics/tracker";
import type { ScreenId } from "@/lib/routing/routes";

export function PageView({ screen }: { screen: ScreenId }) {
  useEffect(() => {
    track(events.screenViewed, { screen });
  }, [screen]);
  return null;
}
