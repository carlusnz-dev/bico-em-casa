import { FormPortfolio } from '@/components/forms/FormPortfolio';
import { Card } from '@/components/ui/Card';

export default function NovoPortfolioPage() {
  return (
    <div className="mx-auto flex max-w-xl flex-col gap-6">
      <header className="flex flex-col gap-2">
        <h1 className="font-heading text-3xl font-bold">Criar portfólio</h1>
        <p className="text-texto-suave">Monte a vitrine do seu trabalho para os clientes.</p>
      </header>

      <Card>
        <FormPortfolio />
      </Card>
    </div>
  );
}
