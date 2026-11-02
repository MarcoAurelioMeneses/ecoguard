import Link from "next/link";
import { PageView } from "@/features/shell/ui/page-view";
import { screenById, type ScreenId } from "@/shared/routing/routes";

export function ScreenPlaceholder({ screen }: { screen: Exclude<ScreenId, "00"> }) {
  const current = screenById(screen);

  return (
    <>
      <PageView screen={screen} />
      <p className="lede">
        {current.title} is not built yet. The entry screen is What is EcoGuard.
      </p>
      <p className="meta">
        No measurements are shown. Figures in the design mockups are examples and are not loaded.
      </p>
      <p className="meta">
        <Link href="/">Back to What is EcoGuard</Link>
      </p>
    </>
  );
}
