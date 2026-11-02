import Link from "next/link";
import { PageView } from "@/features/shell/ui/page-view";

/** Screen 00. Root route. The designed layout ships in a later change. */
export function AboutPage() {
  return (
    <>
      <PageView screen="00" />
      <p className="lede">
        What EcoGuard is: city risk intelligence for São Paulo, combining public environmental,
        health, and social data with an AI City Analyst. This is the entry screen. The designed
        layout is not built yet.
      </p>
      <p className="meta">
        São Paulo, 96 districts. No measurements are shown. Figures in the design mockups are
        examples and are not loaded. District boundaries come from the current GeoSampa layer,
        not from the 2016 file in the design package.
      </p>
      <p className="meta">
        <Link href="/map">Risk map</Link>
        {" · "}
        <Link href="/analyst">AI City Analyst</Link>
      </p>
    </>
  );
}
