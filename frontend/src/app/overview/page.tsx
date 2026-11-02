import { Overview } from "@/features/overview/components/overview";
import { PageView } from "@/features/shell/components/page-view";

export default function OverviewPage() {
  return (
    <>
      <PageView screen="01" />
      <Overview />
    </>
  );
}
