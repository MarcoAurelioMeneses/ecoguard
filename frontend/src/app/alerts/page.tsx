import { AlertsPage } from "@/features/alerts/components/alerts-page";
import { PageView } from "@/features/shell/components/page-view";

export default function AlertsRoute() {
  return (
    <>
      <PageView screen="05" />
      <AlertsPage />
    </>
  );
}
