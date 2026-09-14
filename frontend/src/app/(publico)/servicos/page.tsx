import Link from 'next/link';

import { listarServicos } from '@/api/servicos';
import { CardServico } from '@/components/CardServico';
import { classesDoBotao } from '@/components/ui/Botao';

const TAMANHO_PAGINA = 20;

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
              <CardServico servico={servico} />
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
