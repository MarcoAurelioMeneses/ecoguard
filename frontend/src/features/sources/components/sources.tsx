"use client";

import { ErrorState, LoadingState } from "@/components/ui/states";
import { useRiskConfig } from "../hooks/use-risk-config";

const catalog = [
  ["COVISA", "Dengue cumulativa por distrito, seção 4. use with source citation; licence not verified."],
  ["Open-Meteo", "Previsão de referência da cidade. Não é uma grade por distrito."],
  ["GeoSampa", "96 distritos municipais, WFS geoportal:distrito_municipal."],
  ["INMET, CETESB, CEMADEN, DataSUS, INPE, IBGE", "Catalogadas. Não carregadas nesta versão."],
];

export function Sources() {
  const config = useRiskConfig();
  return (
    <section className="stack">
      <h2>Pesos e catálogo</h2>
      {config.status === "loading" ? <LoadingState label="Carregando os pesos da nota…" /> : null}
      {config.status === "error" ? <ErrorState message={config.message} /> : null}
      {config.status === "ready" ? (
        <p className="formula">
          Geral = {config.data.environmental} ambiental + {config.data.health} saúde + {config.data.social}{" "}
          social ({config.data.status})
        </p>
      ) : null}
      <h2>Atribuição</h2>
      <p>
        Weather data by Open-Meteo.com. Air quality data by Open-Meteo.com. Licença CC BY 4.0. Focos de
        queimada: INPE Programa Queimadas, fora dos indicadores. Censo: IBGE. Dengue: COVISA. use with source
        citation; licence not verified.
      </p>
      <table className="data-table">
        <caption className="sr-only">Fontes de dados</caption>
        <thead>
          <tr>
            <th scope="col">Fonte</th>
            <th scope="col">Situação</th>
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
