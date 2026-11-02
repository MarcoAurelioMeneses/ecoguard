"use client";

import { ErrorState, LoadingState } from "@/components/ui/states";
import { useRiskConfig } from "../hooks/use-risk-config";

const catalog = [
  ["InfoDengue", "Weekly municipal dengue. Loaded by the Java collector."],
  ["Open-Meteo", "City reference forecast. Not a district grid."],
  ["GeoSampa", "96 municipal districts, WFS geoportal:distrito_municipal."],
  ["INMET, CETESB, CEMADEN, DataSUS, INPE, IBGE", "Catalogued. Not loaded in this build."],
];

export function Sources() {
  const config = useRiskConfig();
  return (
    <section className="stack">
      <h1>Sources and methodology</h1>
      {config.status === "loading" ? <LoadingState label="Loading score weights…" /> : null}
      {config.status === "error" ? <ErrorState message={config.message} /> : null}
      {config.status === "ready" ? (
        <p className="formula">
          Overall = {config.data.environmental} environmental + {config.data.health} health +{" "}
          {config.data.social} social ({config.data.status})
        </p>
      ) : null}
      <table className="data-table">
        <caption className="sr-only">Data sources</caption>
        <thead>
          <tr>
            <th scope="col">Source</th>
            <th scope="col">Status</th>
          </tr>
        </thead>
        <tbody>
          {catalog.map(([name, detail]) => (
            <tr key={name}>
              <td>{name}</td>
              <td>{detail}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
