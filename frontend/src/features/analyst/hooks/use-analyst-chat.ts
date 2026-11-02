"use client";

import { useState } from "react";
import { events, track } from "@/lib/analytics/tracker";
import { messageFrom } from "@/lib/load-state";
import { askAnalyst } from "../api/analyst";
import { lengthBucket } from "../model/question";

export function useAnalystChat() {
  const [question, setQuestion] = useState("");
  const [answer, setAnswer] = useState<string | null>(null);
  const [status, setStatus] = useState<"idle" | "loading" | "error">("idle");
  const [error, setError] = useState("");
  const [feedback, setFeedback] = useState<"up" | "down" | null>(null);

  async function submit(text: string) {
    const trimmed = text.trim();
    if (!trimmed) {
      return;
    }
    setStatus("loading");
    setError("");
    setFeedback(null);
    track(events.analystQuestionAsked, { screen: "04", length_bucket: lengthBucket(trimmed) });
    try {
      const result = await askAnalyst(trimmed);
      setAnswer(result.answer);
      setStatus("idle");
    } catch (cause) {
      setStatus("error");
      setError(messageFrom(cause, "The analyst could not answer"));
    }
  }

  return { question, setQuestion, answer, status, error, feedback, setFeedback, submit };
}
