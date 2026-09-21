import { notFound } from 'next/navigation';

import { buscarServicoPorId } from '@/api/servicos';
import { ErroApi } from '@/api/erros';
import { BotaoContratar } from '@/components/BotaoContratar';
import { formatarPreco } from '@/components/formatarPreco';
import { Card } from '@/components/ui/Card';

export default async function ServicoDetalhePage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;

  const servico = await buscarServicoPorId(id).catch((erro) => {
    if (erro instanceof ErroApi && (erro.status === 404 || erro.status === 400)) {
      notFound();
    }
    throw erro;
  });

  return (
    <div className="flex flex-col gap-6">
      <Card className="flex flex-col gap-4">
        <header className="flex flex-col gap-1">
          <h1 className="font-heading text-3xl font-bold">{servico.titulo}</h1>
          <p className="text-lg font-medium text-primary">
            {formatarPreco(servico.precoPrevio, servico.unidadePreco)}
          </p>
        </header>

        <p className="whitespace-pre-line text-texto-suave">{servico.descricao}</p>

        {!servico.ativo && (
          <p className="text-sm font-medium text-erro">Este serviço não está mais ativo.</p>
        )}
      </Card>

      {servico.ativo && <BotaoContratar servicoId={servico.id} />}
    </div>
  );
}
