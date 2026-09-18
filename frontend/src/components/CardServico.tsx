import Link from 'next/link';

import type { Servico, UnidadePreco } from '@/api/contratos/servico';
import { Card } from '@/components/ui/Card';

const LABEL_UNIDADE_PRECO: Record<UnidadePreco, string> = {
  SERVICO: 'por serviço',
  HORA: 'por hora',
  METRO_QUADRADO: 'por m²',
};

export function formatarPreco(precoPrevio: number, unidadePreco: UnidadePreco): string {
  const preco = precoPrevio.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  return `${preco} ${LABEL_UNIDADE_PRECO[unidadePreco]}`;
}

export function CardServico({ servico }: { servico: Servico }) {
  return (
    <Link href={`/servicos/${servico.id}`}>
      <Card className="flex h-full flex-col gap-2 transition-shadow hover:shadow-md">
        <h2 className="text-lg font-semibold">{servico.titulo}</h2>
        <p className="line-clamp-2 flex-1 text-sm text-texto-suave">{servico.descricao}</p>
        <p className="font-medium">{formatarPreco(servico.precoPrevio, servico.unidadePreco)}</p>
      </Card>
    </Link>
  );
}
