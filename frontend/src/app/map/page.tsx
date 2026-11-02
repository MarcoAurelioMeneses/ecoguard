import { MapScreen } from "@/features/map/components/map-screen";
import { PageView } from "@/features/shell/components/page-view";

export default function MapPage() {
  return (
    <>
      <PageView screen="02" />
      <MapScreen />
    </>
  );
}
