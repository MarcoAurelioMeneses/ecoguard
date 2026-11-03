import { AnalystChat } from "@/features/analyst/components/analyst-chat";
import { PageView } from "@/features/shell/components/page-view";

export default function AnalystPage() {
  return (
    <>
      <PageView screen="04" />
      <AnalystChat />
    </>
  );
}
