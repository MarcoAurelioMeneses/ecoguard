import { DistrictDetail } from "@/features/district/components/district-detail";
import { PageView } from "@/features/shell/components/page-view";

export default async function DistrictPage({ params }: { params: Promise<{ code: string }> }) {
  const { code } = await params;
  return (
    <>
      <PageView screen="03" />
      <DistrictDetail code={code} />
    </>
  );
}
