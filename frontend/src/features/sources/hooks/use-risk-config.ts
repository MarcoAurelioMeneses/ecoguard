"use client";

import { useEffect, useState } from "react";
import { messageFrom, type LoadState } from "@/lib/load-state";
import { loadRiskWeights } from "../api/risk-config";
import type { RiskWeights } from "../model/risk-weights";

export function useRiskConfig() {
  const [state, setState] = useState<LoadState<RiskWeights>>({ status: "loading" });

  useEffect(() => {
    loadRiskWeights()
      .then((data) => setState({ status: "ready", data }))
      .catch((error: unknown) => {
        setState({
          status: "error",
          message: messageFrom(error, "Could not load the score configuration"),
        });
      });
  }, []);

  return state;
}
