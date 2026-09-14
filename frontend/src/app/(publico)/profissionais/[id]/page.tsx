import { notFound } from 'next/navigation';

import { buscarPortfolioPorId } from '@/api/portfolio';
import { ErroApi } from '@/api/erros';
import { Card } from '@/components/ui/Card';

export default async function PortfolioDetalhePage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;

  const portfolio = await buscarPortfolioPorId(Number(id)).catch((erro) => {
    if (erro instanceof ErroApi && (erro.status === 404 || erro.status === 400)) {
      notFound();
    }
    throw erro;
  });

  return (
    <div className="flex flex-col gap-6">
      <Card className="flex flex-col gap-4 overflow-hidden p-0">
        <div className="aspect-video w-full bg-borda/30">
          {portfolio.fotoCapaUrl && (
            // eslint-disable-next-line @next/next/no-img-element
            <img
              src={portfolio.fotoCapaUrl}
              alt={portfolio.titulo}
              className="h-full w-full object-cover"
            />
          )}
        </div>

        <div className="flex flex-col gap-2 p-6 pt-0">
          <h1 className="font-heading text-3xl font-bold">{portfolio.titulo}</h1>
          {portfolio.descricao && (
            <p className="whitespace-pre-line text-texto-suave">{portfolio.descricao}</p>
          )}
        </div>
      </Card>
    </div>
  );
}
