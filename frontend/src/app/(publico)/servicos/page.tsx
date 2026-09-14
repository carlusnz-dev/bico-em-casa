import Link from 'next/link';

import { listarServicos } from '@/api/servicos';
import { Card } from '@/components/ui/Card';
import { classesDoBotao } from '@/components/ui/Botao';
import type { UnidadePreco } from '@/api/contratos/servico';

const TAMANHO_PAGINA = 20;

const LABEL_UNIDADE_PRECO: Record<UnidadePreco, string> = {
  SERVICO: 'por serviço',
  HORA: 'por hora',
  METRO_QUADRADO: 'por m²',
};

function formatarPreco(precoPrevio: number, unidadePreco: UnidadePreco): string {
  const preco = precoPrevio.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  return `${preco} ${LABEL_UNIDADE_PRECO[unidadePreco]}`;
}

export default async function ServicosPage({
  searchParams,
}: {
  searchParams: Promise<{ pagina?: string }>;
}) {
  const { pagina: paginaParam } = await searchParams;
  const pagina = Number(paginaParam ?? '0');
  const { conteudo: servicos, totalPaginas } = await listarServicos(pagina, TAMANHO_PAGINA);

  return (
    <div className="flex flex-col gap-8">
      <header className="flex flex-col gap-2">
        <h1 className="font-heading text-3xl font-bold">Serviços disponíveis</h1>
        <p className="text-texto-suave">Encontre um profissional para o que você precisa.</p>
      </header>

      {servicos.length === 0 ? (
        <p className="text-texto-suave">Nenhum serviço ativo no momento.</p>
      ) : (
        <ul className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          {servicos.map((servico) => (
            <li key={servico.id}>
              <Card className="flex h-full flex-col gap-2">
                <h2 className="text-lg font-semibold">{servico.titulo}</h2>
                <p className="flex-1 text-sm text-texto-suave">{servico.descricao}</p>
                <p className="font-medium">
                  {formatarPreco(servico.precoPrevio, servico.unidadePreco)}
                </p>
              </Card>
            </li>
          ))}
        </ul>
      )}

      {totalPaginas > 1 && (
        <nav className="flex justify-center gap-2">
          {pagina > 0 && (
            <Link href={`/servicos?pagina=${pagina - 1}`} className={classesDoBotao('secundario')}>
              Anterior
            </Link>
          )}
          {pagina + 1 < totalPaginas && (
            <Link href={`/servicos?pagina=${pagina + 1}`} className={classesDoBotao('secundario')}>
              Próxima
            </Link>
          )}
        </nav>
      )}
    </div>
  );
}
