import Link from 'next/link';

import { listarPortfolios } from '@/api/portfolio';
import { CardPortfolio } from '@/components/CardPortfolio';
import { classesDoBotao } from '@/components/ui/Botao';

const TAMANHO_PAGINA = 20;

export default async function ProfissionaisPage({
  searchParams,
}: {
  searchParams: Promise<{ pagina?: string }>;
}) {
  const { pagina: paginaParam } = await searchParams;
  const pagina = Number(paginaParam ?? '0');
  const { conteudo: portfolios, totalPaginas } = await listarPortfolios(pagina, TAMANHO_PAGINA);

  return (
    <div className="flex flex-col gap-8">
      <header className="flex flex-col gap-2">
        <h1 className="font-heading text-3xl font-bold">Profissionais</h1>
        <p className="text-texto-suave">Conheça o portfólio de quem já atende no Bico em Casa.</p>
      </header>

      {portfolios.length === 0 ? (
        <p className="text-texto-suave">Nenhum portfólio publicado ainda.</p>
      ) : (
        <ul className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          {portfolios.map((portfolio) => (
            <li key={portfolio.id}>
              <CardPortfolio portfolio={portfolio} />
            </li>
          ))}
        </ul>
      )}

      {totalPaginas > 1 && (
        <nav className="flex justify-center gap-2">
          {pagina > 0 && (
            <Link
              href={`/profissionais?pagina=${pagina - 1}`}
              className={classesDoBotao('secundario')}
            >
              Anterior
            </Link>
          )}
          {pagina + 1 < totalPaginas && (
            <Link
              href={`/profissionais?pagina=${pagina + 1}`}
              className={classesDoBotao('secundario')}
            >
              Próxima
            </Link>
          )}
        </nav>
      )}
    </div>
  );
}
