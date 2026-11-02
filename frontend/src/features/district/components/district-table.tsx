import Link from "next/link";
import { RiskMark } from "@/components/ui/risk-mark";
import type { District } from "../model/district";

export function DistrictTable({ districts }: { districts: District[] }) {
  if (districts.length === 0) {
    return <p className="state">No districts in this table.</p>;
  }
  return (
    <table className="data-table">
      <caption className="sr-only">Districts and score availability</caption>
      <thead>
        <tr>
          <th scope="col">District</th>
          <th scope="col">Code</th>
          <th scope="col">Area km²</th>
          <th scope="col">Score</th>
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
              <RiskMark level="nodata" />
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
