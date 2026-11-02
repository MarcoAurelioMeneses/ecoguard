"use client";

import { useEffect, useState } from "react";
import { events, track } from "@/lib/analytics/tracker";
import { messageFrom } from "@/lib/load-state";
import { getDistrict } from "../api/districts";
import type { District } from "../model/district";

export type DistrictState =
  | { status: "loading" }
  | { status: "missing" }
  | { status: "error"; message: string }
  | { status: "ready"; data: District };

export function useDistrict(code: string) {
  const [state, setState] = useState<DistrictState>({ status: "loading" });

  useEffect(() => {
    track(events.districtViewed, { district_code: code });
    getDistrict(code)
      .then((data) => setState({ status: "ready", data }))
      .catch((error: unknown) => {
        const message = messageFrom(error, "Could not load the district");
        setState(message.includes("404") ? { status: "missing" } : { status: "error", message });
      });
  }, [code]);

  return state;
}
