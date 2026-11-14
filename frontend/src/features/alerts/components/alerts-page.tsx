import { EmptyState } from "@/components/ui/states";

export function AlertsPage() {
  return (
    <>
      <p className="state" role="status">
        CEMADEN: sem atualização. Sem feed anônimo, nenhum alerta hidrológico é publicado.
      </p>
      <EmptyState
        title="Nenhum alerta ativo"
        detail="Focos do INPE ficam fora dos indicadores. Os tipos são chuva, calor, frio, qualidade do ar e hidrológico."
      />
    </>
  );
}
