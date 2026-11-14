import { EmptyState } from "@/components/ui/states";

export function AlertsPage() {
  return (
    <>
      <EmptyState
        title="Nenhum alerta ativo"
        detail="Os dados de tempo, enchente e fogo ainda não foram carregados. Esta lista fica vazia em vez de mostrar alertas de exemplo."
      />
    </>
  );
}
