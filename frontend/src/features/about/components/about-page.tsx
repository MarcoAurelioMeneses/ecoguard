import Link from "next/link";

export function AboutPage() {
  return (
    <section className="stack">
      <h1>What EcoGuard is</h1>
      <p>
        City risk intelligence for São Paulo. It combines public environmental, health, and social data at
        district grain, where the source actually has that grain, with an AI City Analyst that has to cite its
        sources.
      </p>
      <p className="meta">
        The problem is scattered bulletins. The audience is a resident or an analyst who needs one map and one
        cited answer. Figures in the design mockups are examples and are not loaded.
      </p>
      <p className="segment">
        <Link className="control is-on" href="/map">
          Risk map
        </Link>
        <Link className="control" href="/analyst">
          AI City Analyst
        </Link>
      </p>
    </section>
  );
}
