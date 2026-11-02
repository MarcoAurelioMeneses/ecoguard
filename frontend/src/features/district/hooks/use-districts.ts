"use client";

import { useCallback, useEffect, useState } from "react";
import { messageFrom, type LoadState } from "@/lib/load-state";
import { listDistricts } from "../api/districts";
import type { District } from "../model/district";

export function useDistricts() {
  const [state, setState] = useState<LoadState<District[]>>({ status: "loading" });

  const load = useCallback(() => {
    setState({ status: "loading" });
    listDistricts()
      .then((districts) => {
        setState(districts.length === 0 ? { status: "empty" } : { status: "ready", data: districts });
      })
      .catch((error: unknown) => {
        setState({ status: "error", message: messageFrom(error, "Could not load districts") });
      });
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  return { state, reload: load };
}
