import Link from 'next/link';

import type { Perfil } from '@/api/contratos/perfil';
import { buscarPerfilPorId } from '@/api/perfil';
import { listarServicos } from '@/api/servicos';
import { listarTags } from '@/api/tags';
import { CardServico } from '@/components/CardServico';
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
  searchParams: Promise<{ pagina?: string; busca?: string }>;
}) {
  const { pagina: paginaParam, busca: buscaParam } = await searchParams;
  const busca = buscaParam?.trim() ?? '';
  const termoNormalizado = normalizarTexto(busca);
  const paginaSolicitada = Math.max(0, Number(paginaParam ?? '0') || 0);

  const [paginaInicial, tags] = await Promise.all([
    listarServicos(0, TAMANHO_PAGINA),
    listarTags(),
  ]);

  let servicosDisponiveis = paginaInicial.conteudo;

  if (termoNormalizado && paginaInicial.totalPaginas > 1) {
    const demaisPaginas = await Promise.all(
      Array.from(
        { length: paginaInicial.totalPaginas - 1 },
        (_, indice) => listarServicos(indice + 1, TAMANHO_PAGINA),
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
          ...servico.tagIds.map(
            (id) => tagsPorId.get(id)?.nome ?? '',
          ),
        ].join(' '),
      );

      return textoServico.includes(termoNormalizado);
    })
    : servicosDisponiveis;

  const totalPaginas = termoNormalizado
    ? Math.max(
      1,
      Math.ceil(servicosFiltrados.length / TAMANHO_PAGINA),
    )
    : paginaInicial.totalPaginas;

  const pagina = Math.min(paginaSolicitada, totalPaginas - 1);

  const servicos = termoNormalizado
    ? servicosFiltrados.slice(
      pagina * TAMANHO_PAGINA,
      (pagina + 1) * TAMANHO_PAGINA,
    )
    : paginaInicial.conteudo;

  const perfilIds = [
    ...new Set(servicos.map((servico) => servico.perfilId)),
  ];

  const perfis = await Promise.all(
    perfilIds.map((id) =>
      buscarPerfilPorId(id).catch(() => null),
    ),
  );

  const perfilPorId = new Map<string, Perfil | null>(
    perfilIds.map((id, indice) => [
      id,
      perfis[indice] ?? null,
    ]),
  );

  const queryBusca = busca
    ? `&busca=${encodeURIComponent(busca)}`
    : '';

  return (
    <div className="flex flex-col gap-8">
      <header className="flex flex-col gap-2">
        <h1 className="font-heading text-3xl font-bold">
          {busca
            ? `Resultados para “${busca}”`
            : 'Serviços disponíveis'}
        </h1>

        <p className="text-texto-suave">
          {busca
            ? `${servicosFiltrados.length} serviço(s) encontrado(s).`
            : 'Encontre um profissional para o que você precisa.'}
        </p>

        {busca && (
          <div>
            <Link
              href="/servicos"
              className={classesDoBotao('secundario')}
            >
              Limpar pesquisa
            </Link>
          </div>
        )}
      </header>

      {servicos.length === 0 ? (
        <p className="text-texto-suave">
          {busca
            ? 'Nenhum serviço corresponde à sua pesquisa. Tente outros termos.'
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
            <Link
              href={`/servicos?pagina=${pagina - 1}${queryBusca}`}
              className={classesDoBotao('secundario')}
            >
              Anterior
            </Link>
          )}

          {pagina + 1 < totalPaginas && (
            <Link
              href={`/servicos?pagina=${pagina + 1}${queryBusca}`}
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
