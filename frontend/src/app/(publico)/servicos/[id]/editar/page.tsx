import { notFound } from 'next/navigation';

import { buscarServicoPorId } from '@/api/servicos';
import { listarTags } from '@/api/tags';
import { ErroApi } from '@/api/erros';
import { FormServico } from '@/components/forms/FormServico';
import { Card } from '@/components/ui/Card';

export const dynamic = 'force-dynamic';

export default async function EditarServicoPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;

  const [servico, tags] = await Promise.all([
    buscarServicoPorId(id).catch((erro) => {
      if (erro instanceof ErroApi && (erro.status === 404 || erro.status === 400)) {
        notFound();
      }
      throw erro;
    }),
    listarTags(),
  ]);

  return (
    <div className="mx-auto flex max-w-xl flex-col gap-6">
      <header className="flex flex-col gap-2">
        <h1 className="font-heading text-3xl font-bold">Editar serviço</h1>
        <p className="text-texto-suave">Atualize os dados de &quot;{servico.titulo}&quot;.</p>
      </header>

      <Card>
        <FormServico tags={tags} servico={servico} />
      </Card>
    </div>
  );
}
