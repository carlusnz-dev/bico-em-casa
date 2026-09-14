import Link from 'next/link';

import type { Portfolio } from '@/api/contratos/portfolio';
import { Card } from '@/components/ui/Card';

export function CardPortfolio({ portfolio }: { portfolio: Portfolio }) {
  return (
    <Link href={`/profissionais/${portfolio.id}`}>
      <Card className="flex h-full flex-col gap-3 overflow-hidden p-0 transition-shadow hover:shadow-md">
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
        <div className="flex flex-1 flex-col gap-2 p-4 pt-0">
          <h2 className="text-lg font-semibold">{portfolio.titulo}</h2>
          {portfolio.descricao && (
            <p className="line-clamp-2 flex-1 text-sm text-texto-suave">{portfolio.descricao}</p>
          )}
        </div>
      </Card>
    </Link>
  );
}
