import Link from "next/link";
import { RiskMark } from "@/components/ui/risk-mark";
import type { District } from "../model/district";

export function DistrictTable({ districts }: { districts: District[] }) {
  if (districts.length === 0) {
    return <p className="state">Nenhum distrito nesta tabela.</p>;
  }
  return (
    <table className="data-table">
      <caption className="sr-only">Distritos e disponibilidade da nota</caption>
      <thead>
        <tr>
          <th scope="col">Distrito</th>
          <th scope="col">Código</th>
          <th scope="col">Área km²</th>
          <th scope="col">Nota</th>
        </tr>
      </thead>
      <tbody>
        {districts.map((district) => (
          <tr key={district.code}>
            <td>
              <Link href={`/districts/${district.code}`}>{district.name}</Link>
            </td>
            <td className="num">{district.code}</td>
            <td className="num">{district.areaKm2 ?? "—"}</td>
            <td>
              <RiskMark level={district.score?.overallClass ?? "nodata"} />{" "}
              <span className="num">{district.score?.overall?.toFixed(1) ?? "—"}</span>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
