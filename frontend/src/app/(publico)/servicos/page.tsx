import Link from 'next/link';

import { buscarMediaPorServico } from '@/api/avaliacoes';
import type { Perfil } from '@/api/contratos/perfil';
import { buscarPerfilPorId } from '@/api/perfil';
import { listarServicos } from '@/api/servicos';
import { listarTags } from '@/api/tags';
import { CardServico } from '@/components/CardServico';
import { FiltroCidade } from '@/components/FiltroCidade';
import { classesDoBotao } from '@/components/ui/Botao';

const TAMANHO_PAGINA = 20;

function normalizarTexto(texto: string): string {
  return texto
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLocaleLowerCase('pt-BR')
    .trim();
}

export default async function ServicosPage({
  searchParams,
}: {
  searchParams: Promise<{
    pagina?: string;
    busca?: string;
    cidade?: string;
    ordenar?: string;
  }>;
}) {
  const {
    pagina: paginaParam,
    busca: buscaParam,
    cidade: cidadeParam,
    ordenar: ordenarParam,
  } = await searchParams;
  const busca = buscaParam?.trim() ?? '';
  const cidade = cidadeParam?.trim() ?? '';
  const ordenar = ordenarParam === 'nota' ? ordenarParam : '';
  const termoNormalizado = normalizarTexto(busca);
  const paginaSolicitada = Math.max(0, Number(paginaParam ?? '0') || 0);

  const [paginaConsultada, tags] = await Promise.all([
    listarServicos(termoNormalizado || ordenar ? 0 : paginaSolicitada, TAMANHO_PAGINA, cidade),
    listarTags(),
  ]);

  let paginaInicial = paginaConsultada;
  if (
    !termoNormalizado && !ordenar
    && paginaInicial.totalPaginas > 0
    && paginaSolicitada >= paginaInicial.totalPaginas
  ) {
    paginaInicial = await listarServicos(
      paginaInicial.totalPaginas - 1,
      TAMANHO_PAGINA,
      cidade,
    );
  }

  let servicosDisponiveis = paginaInicial.conteudo;

  if ((termoNormalizado || ordenar) && paginaInicial.totalPaginas > 1) {
    const demaisPaginas = await Promise.all(
      Array.from(
        { length: paginaInicial.totalPaginas - 1 },
        (_, indice) => listarServicos(indice + 1, TAMANHO_PAGINA, cidade),
      ),
    );

    servicosDisponiveis = [
      ...paginaInicial.conteudo,
      ...demaisPaginas.flatMap((pagina) => pagina.conteudo),
    ];
  }

  const tagsPorId = new Map(tags.map((tag) => [tag.id, tag]));

  const servicosFiltrados = termoNormalizado
    ? servicosDisponiveis.filter((servico) => {
      const textoServico = normalizarTexto(
        [
          servico.titulo,
          servico.descricao,
          ...servico.tagIds.map((id) => tagsPorId.get(id)?.nome ?? ''),
        ].join(' '),
      );

      return textoServico.includes(termoNormalizado);
    })
    : servicosDisponiveis;

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

  const totalPaginas = termoNormalizado || ordenar
    ? Math.max(1, Math.ceil(servicosFiltrados.length / TAMANHO_PAGINA))
    : paginaInicial.totalPaginas;
  const pagina = termoNormalizado || ordenar
    ? Math.min(paginaSolicitada, totalPaginas - 1)
    : paginaInicial.pagina;

  const servicos = termoNormalizado || ordenar
    ? servicosFiltrados.slice(
      pagina * TAMANHO_PAGINA,
      (pagina + 1) * TAMANHO_PAGINA,
    )
    : paginaInicial.conteudo;

  const totalServicos = termoNormalizado
    ? servicosFiltrados.length
    : paginaInicial.totalElementos;

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

        {(busca || cidade || ordenar) && (
          <div>
            <Link href="/servicos" className={classesDoBotao('secundario')}>
              Limpar filtros
            </Link>
          </div>
        )}
      </header>

      <FiltroCidade acao="/servicos" cidade={cidade} busca={busca} ordenar={ordenar} />

      {servicos.length === 0 ? (
        <p className="text-texto-suave">
          {busca
            ? 'Nenhum serviço corresponde à sua pesquisa. Tente outros termos.'
            : cidade
              ? 'Nenhum serviço encontrado para essa cidade.'
              : 'Nenhum serviço ativo no momento.'}
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
