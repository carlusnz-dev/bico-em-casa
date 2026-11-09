import Link from 'next/link';

import { buscarMediaPorServico } from '@/api/avaliacoes';
import type { Perfil } from '@/api/contratos/perfil';
import { buscarPerfilPorId } from '@/api/perfil';
import { listarPrecosMedios, listarServicos } from '@/api/servicos';
import { listarTags } from '@/api/tags';
import { CardServico } from '@/components/CardServico';
import { FiltroCidade } from '@/components/FiltroCidade';
import { ListaValoresMedios } from '@/components/ListaValoresMedios';
import { classesDoBotao } from '@/components/ui/Botao';

const TAMANHO_PAGINA = 20;

function valorMonetario(valor: unknown): string {
  return typeof valor === 'string' && /^\d{1,8}(\.\d{1,2})?$/.test(valor) ? valor : '';
}

export default async function ServicosPage({
  searchParams,
}: {
  searchParams: Promise<{
    pagina?: string;
    busca?: string;
    cidade?: string;
    ordenar?: string;
    categoria?: string;
    precoMinimo?: string;
    precoMaximo?: string;
  }>;
}) {
  const {
    pagina: paginaParam,
    busca: buscaParam,
    cidade: cidadeParam,
    ordenar: ordenarParam,
    categoria: categoriaParam,
    precoMinimo: minimoParam,
    precoMaximo: maximoParam,
  } = await searchParams;
  const busca = typeof buscaParam === 'string' ? buscaParam.trim().slice(0, 100) : '';
  const cidade = typeof cidadeParam === 'string' ? cidadeParam.trim().slice(0, 100) : '';
  const categoria = typeof categoriaParam === 'string' && /^[a-z0-9-]{1,100}$/.test(categoriaParam)
    ? categoriaParam : '';
  const precoMinimo = valorMonetario(minimoParam);
  const precoMaximo = valorMonetario(maximoParam);
  const faixaInvalida = Boolean(precoMinimo && precoMaximo && Number(precoMinimo) > Number(precoMaximo));
  const ordenar = ordenarParam === 'nota' ? ordenarParam : '';
  const numeroPagina = Number(paginaParam ?? '0');
  const paginaSolicitada = Number.isSafeInteger(numeroPagina) && numeroPagina >= 0 && numeroPagina <= 2147483647
    ? numeroPagina : 0;
  const filtros = {
    busca, categoria,
    precoMinimo: faixaInvalida ? '' : precoMinimo,
    precoMaximo: faixaInvalida ? '' : precoMaximo,
  };

  const [paginaConsultada, tags, precosMedios] = await Promise.all([
    listarServicos(ordenar ? 0 : paginaSolicitada, TAMANHO_PAGINA, cidade, filtros),
    listarTags(),
    categoria ? listarPrecosMedios(categoria).catch(() => null) : null,
  ]);

  let paginaInicial = paginaConsultada;
  if (
    !ordenar
    && paginaInicial.totalPaginas > 0
    && paginaSolicitada >= paginaInicial.totalPaginas
  ) {
    paginaInicial = await listarServicos(
      paginaInicial.totalPaginas - 1,
      TAMANHO_PAGINA,
      cidade,
      filtros,
    );
  }

  let servicosFiltrados = paginaInicial.conteudo;

  if (ordenar && paginaInicial.totalPaginas > 1) {
    const demaisPaginas = await Promise.all(
      Array.from(
        { length: paginaInicial.totalPaginas - 1 },
        (_, indice) => listarServicos(indice + 1, TAMANHO_PAGINA, cidade, filtros),
      ),
    );

    servicosFiltrados = [
      ...paginaInicial.conteudo,
      ...demaisPaginas.flatMap((pagina) => pagina.conteudo),
    ];
  }

  const tagsPorId = new Map(tags.map((tag) => [tag.id, tag]));
  const categoriaSelecionada = tags.find((tag) => tag.slug === categoria);

  const notasPorServico = new Map<string, number>();
  if (ordenar) {
    const medias = await Promise.all(
      servicosFiltrados.map(async (servico) => [
        servico.id,
        await buscarMediaPorServico(servico.id),
      ] as const),
    );
    for (const [servicoId, media] of medias) {
      notasPorServico.set(servicoId, media);
    }
    servicosFiltrados.sort(
      (servicoA, servicoB) =>
        (notasPorServico.get(servicoB.id) ?? 0) - (notasPorServico.get(servicoA.id) ?? 0),
    );
  }

  const totalPaginas = ordenar
    ? Math.max(1, Math.ceil(servicosFiltrados.length / TAMANHO_PAGINA))
    : paginaInicial.totalPaginas;
  const pagina = ordenar
    ? Math.min(paginaSolicitada, totalPaginas - 1)
    : paginaInicial.pagina;

  const servicos = ordenar
    ? servicosFiltrados.slice(
      pagina * TAMANHO_PAGINA,
      (pagina + 1) * TAMANHO_PAGINA,
    )
    : paginaInicial.conteudo;

  const totalServicos = paginaInicial.totalElementos;

  const perfilIds = [...new Set(servicos.map((servico) => servico.perfilId))];
  const perfis = await Promise.all(
    perfilIds.map((id) => buscarPerfilPorId(id).catch(() => null)),
  );
  const perfilPorId = new Map<string, Perfil | null>(
    perfilIds.map((id, indice) => [id, perfis[indice] ?? null]),
  );

  function hrefPagina(numeroPagina: number): string {
    const parametros = new URLSearchParams();
    if (busca) parametros.set('busca', busca);
    if (cidade) parametros.set('cidade', cidade);
    if (ordenar) parametros.set('ordenar', ordenar);
    if (categoria) parametros.set('categoria', categoria);
    if (precoMinimo) parametros.set('precoMinimo', precoMinimo);
    if (precoMaximo) parametros.set('precoMaximo', precoMaximo);
    parametros.set('pagina', String(numeroPagina));
    return `/servicos?${parametros.toString()}`;
  }

  return (
    <div className="flex flex-col gap-8">
      <header className="flex flex-col gap-2">
        <h1 className="font-heading text-3xl font-bold">
          {busca
            ? `Resultados para “${busca}”`
            : cidade
              ? `Serviços em cidades com “${cidade}”`
              : 'Serviços disponíveis'}
        </h1>

        <p className="text-texto-suave">
          {busca
            ? `${totalServicos} serviço(s) encontrado(s).`
            : cidade
              ? `${totalServicos} serviço(s) encontrado(s) para essa cidade.`
              : 'Encontre um profissional para o que você precisa.'}
        </p>

        {(busca || cidade || ordenar || categoria || precoMinimo || precoMaximo) && (
          <div>
            <Link href="/servicos" className={classesDoBotao('secundario')}>
              Limpar filtros
            </Link>
          </div>
        )}
      </header>

      <FiltroCidade acao="/servicos" cidade={cidade} busca={busca} ordenar={ordenar}
        categoria={categoria} categorias={tags} precoMinimo={precoMinimo} precoMaximo={precoMaximo} />
      {faixaInvalida && (
        <p role="alert" className="text-erro">O preço mínimo precisa ser menor ou igual ao máximo.</p>
      )}
      {categoriaSelecionada && (
        <section aria-label="Preços médios da categoria" className="flex flex-col gap-2">
          <h2 className="text-lg font-semibold">Preço médio em {categoriaSelecionada.nome}</h2>
          {precosMedios === null
            ? <p>Preços médios indisponíveis.</p>
            : precosMedios.length > 0
              ? <ListaValoresMedios valores={precosMedios} />
              : <p>Nenhuma oferta ativa nesta categoria.</p>}
        </section>
      )}

      {servicos.length === 0 ? (
        <p className="text-texto-suave">
          {busca
            ? 'Nenhum serviço corresponde à sua pesquisa. Tente outros termos.'
            : cidade
              ? 'Nenhum serviço encontrado para essa cidade.'
              : 'Nenhum serviço encontrado com os filtros selecionados.'}
        </p>
      ) : (
        <ul className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {servicos.map((servico) => (
            <li key={servico.id}>
              <CardServico
                servico={servico}
                tags={servico.tagIds
                  .map((id) => tagsPorId.get(id))
                  .filter((tag) => tag !== undefined)}
                perfil={perfilPorId.get(servico.perfilId) ?? null}
                notaMedia={notasPorServico.get(servico.id)}
              />
            </li>
          ))}
        </ul>
      )}

      {totalPaginas > 1 && (
        <nav
          className="flex justify-center gap-2"
          aria-label="Paginação dos serviços"
        >
          {pagina > 0 && (
            <Link href={hrefPagina(pagina - 1)} className={classesDoBotao('secundario')}>
              Anterior
            </Link>
          )}

          {pagina + 1 < totalPaginas && (
            <Link href={hrefPagina(pagina + 1)} className={classesDoBotao('secundario')}>
              Próxima
            </Link>
          )}
        </nav>
      )}
    </div>
  );
}
