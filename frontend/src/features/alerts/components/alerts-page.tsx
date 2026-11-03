import { EmptyState } from "@/components/ui/states";

export function AlertsPage() {
  return (
    <>
      <EmptyState
        title="No active alerts"
        detail="Weather, flood, and fire feeds are not loaded yet. This list stays empty instead of showing example alerts."
      />
    </>
  );
}
