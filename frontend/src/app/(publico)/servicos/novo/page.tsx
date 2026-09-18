import { listarTags } from '@/api/tags';
import { FormServico } from '@/components/forms/FormServico';
import { Card } from '@/components/ui/Card';

export const dynamic = 'force-dynamic';

export default async function NovoServicoPage() {
  const tags = await listarTags();

  return (
    <div className="mx-auto flex max-w-xl flex-col gap-6">
      <header className="flex flex-col gap-2">
        <h1 className="font-heading text-3xl font-bold">Anunciar serviço</h1>
        <p className="text-texto-suave">Preencha os dados do serviço que você presta.</p>
      </header>

      <Card>
        <FormServico tags={tags} />
      </Card>
    </div>
  );
}
