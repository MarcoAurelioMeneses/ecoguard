"use client";

import { ErrorState } from "@/components/ui/states";
import { useAnalystChat } from "../hooks/use-analyst-chat";

export function AnalystChat() {
  const chat = useAnalystChat();

  return (
    <section className="stack chat">
      <p className="ai-tag">IA</p>
      <p className="disclaimer">
        As respostas podem conter erros. Confira a fonte citada e a data antes de confiar em um número.
      </p>
      {chat.status === "loading" ? <p role="status">Consultando o analista…</p> : null}
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
              Útil
            </button>
            <button
              type="button"
              className="control"
              onClick={() => chat.setFeedback("down")}
              aria-pressed={chat.feedback === "down"}
            >
              Não útil
            </button>
            <button type="button" className="control" onClick={() => void chat.submit(chat.question)}>
              Regenerar
            </button>
          </div>
        </article>
      ) : chat.status === "idle" ? (
        <p className="meta">
          Ainda não há resposta. Pergunte sobre um distrito, uma fonte ou a fórmula da nota.
        </p>
      ) : null}
      <form
        className="ask"
        onSubmit={(event) => {
          event.preventDefault();
          void chat.submit(chat.question);
        }}
      >
        <label htmlFor="question">Pergunta</label>
        <textarea
          id="question"
          value={chat.question}
          maxLength={2000}
          onChange={(event) => chat.setQuestion(event.target.value)}
        />
        <button type="submit" className="control is-on">
          Enviar
        </button>
      </form>
    </section>
  );
}
