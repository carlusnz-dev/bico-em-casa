import Link from 'next/link';

import { listarPortfolios } from '@/api/portfolio';
import { CardPortfolio } from '@/components/CardPortfolio';
import { classesDoBotao } from '@/components/ui/Botao';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';

const TAMANHO_PAGINA = 20;

export default async function ProfissionaisPage({
  searchParams,
}: {
  searchParams: Promise<{ pagina?: string; cidade?: string }>;
}) {
  const { pagina: paginaParam, cidade: cidadeParam } = await searchParams;
  const cidade = typeof cidadeParam === 'string' ? cidadeParam.trim().slice(0, 100) : '';
  const numeroPagina = Number(paginaParam ?? '0');
  const pagina = Number.isSafeInteger(numeroPagina) && numeroPagina >= 0 && numeroPagina <= 2147483647
    ? numeroPagina : 0;
  const { conteudo: portfolios, totalPaginas } = await listarPortfolios(pagina, TAMANHO_PAGINA, cidade);

  function hrefPagina(indice: number) {
    const parametros = new URLSearchParams({ pagina: String(indice) });
    if (cidade) parametros.set('cidade', cidade);
    return `/profissionais?${parametros}`;
  }

  return (
    <div className="flex flex-col gap-8">
      <header className="flex flex-col gap-2">
        <h1 className="font-heading text-3xl font-bold">Profissionais</h1>
        <p className="text-texto-suave">Conheça o portfólio de quem já atende no Bico em Casa.</p>
      </header>

      <form action="/profissionais" method="get" className="flex flex-wrap items-end gap-3">
        <div className="flex flex-col gap-1.5">
          <Label htmlFor="cidade-profissional">Filtrar profissionais por cidade</Label>
          <Input id="cidade-profissional" name="cidade" defaultValue={cidade}
            autoComplete="address-level2" maxLength={100} placeholder="Ex.: Curitiba" />
        </div>
        <button type="submit" className={classesDoBotao('primario')}>Aplicar filtro</button>
        {cidade && <Link href="/profissionais" className={classesDoBotao('secundario')}>Limpar filtro</Link>}
      </form>

      {portfolios.length === 0 ? (
        <p className="text-texto-suave">
          {cidade ? `Nenhum portfólio encontrado para “${cidade}”.` : 'Nenhum portfólio publicado ainda.'}
        </p>
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
        <nav aria-label="Paginação dos profissionais" className="flex justify-center gap-2">
          {pagina > 0 && (
            <Link
              href={hrefPagina(pagina - 1)}
              className={classesDoBotao('secundario')}
            >
              Anterior
            </Link>
          )}
          {pagina + 1 < totalPaginas && (
            <Link
              href={hrefPagina(pagina + 1)}
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
