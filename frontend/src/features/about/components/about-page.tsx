import Link from "next/link";

export function AboutPage() {
  return (
    <section className="stack">
      <p>
        Inteligência de risco urbano para São Paulo. Reúne dados públicos ambientais, de saúde e sociais no
        grão do distrito, quando a fonte tem esse grão, com um analista de IA que precisa citar as fontes.
      </p>
      <p className="meta">
        O problema são boletins espalhados. O público é um morador ou um analista que precisa de um mapa e de
        uma resposta citada. Os números dos desenhos de interface são exemplos e não são carregados.
      </p>
      <p className="segment">
        <Link className="control is-on" href="/map">
          Mapa de risco
        </Link>
        <Link className="control" href="/analyst">
          Analista de IA
        </Link>
      </p>
    </section>
  );
}
