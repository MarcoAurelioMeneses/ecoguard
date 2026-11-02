"use client";

import { ErrorState } from "@/components/ui/states";
import { useAnalystChat } from "../hooks/use-analyst-chat";

export function AnalystChat() {
  const chat = useAnalystChat();

  return (
    <section className="stack chat">
      <p className="ai-tag">AI</p>
      <p className="disclaimer">
        Answers may contain errors. Check the cited source and date before you rely on a number.
      </p>
      {chat.status === "loading" ? <p role="status">Asking the analyst…</p> : null}
      {chat.status === "error" ? <ErrorState message={chat.error} /> : null}
      {chat.answer ? (
        <article className="answer">
          <p>{chat.answer}</p>
          <div className="toolbar">
            <button
              type="button"
              className="control"
              onClick={() => chat.setFeedback("up")}
              aria-pressed={chat.feedback === "up"}
            >
              Useful
            </button>
            <button
              type="button"
              className="control"
              onClick={() => chat.setFeedback("down")}
              aria-pressed={chat.feedback === "down"}
            >
              Not useful
            </button>
            <button type="button" className="control" onClick={() => void chat.submit(chat.question)}>
              Regenerate
            </button>
          </div>
        </article>
      ) : chat.status === "idle" ? (
        <p className="meta">No answer yet. Ask about a district, a source, or the score formula.</p>
      ) : null}
      <form
        className="ask"
        onSubmit={(event) => {
          event.preventDefault();
          void chat.submit(chat.question);
        }}
      >
        <label htmlFor="question">Question</label>
        <textarea
          id="question"
          value={chat.question}
          maxLength={2000}
          onChange={(event) => chat.setQuestion(event.target.value)}
        />
        <button type="submit" className="control is-on">
          Send
        </button>
      </form>
    </section>
  );
}
