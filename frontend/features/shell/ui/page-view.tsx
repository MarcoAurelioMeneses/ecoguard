"use client";

import { useEffect } from "react";
import { events, track } from "@/shared/analytics/tracker";
import type { ScreenId } from "@/shared/routing/routes";

export function PageView({ screen }: { screen: ScreenId }) {
  useEffect(() => {
    track(events.screenViewed, { screen });
  }, [screen]);
  return null;
}
