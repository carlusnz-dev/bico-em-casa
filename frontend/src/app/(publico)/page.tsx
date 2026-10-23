import Link from 'next/link';

import type { Perfil } from '@/api/contratos/perfil';
import { buscarMediaPorServico } from '@/api/avaliacoes';
import { buscarPerfilPorId } from '@/api/perfil';
import { listarServicos } from '@/api/servicos';
import { listarTags } from '@/api/tags';
import { BannerHome } from '@/components/BannerHome';
import { CardServico } from '@/components/CardServico';
import { MiniMenuCategorias } from '@/components/MiniMenuCategorias';
import { classesDoBotao } from '@/components/ui/Botao';

const QUANTIDADE_DESTAQUE = 6;

export const dynamic = 'force-dynamic';

export default async function HomePage({
  searchParams,
}: {
  searchParams: Promise<{ cidade?: string; ordenar?: string }>;
}) {
  const { cidade: cidadeParam, ordenar: ordenarParam } = await searchParams;
  const cidade = cidadeParam?.trim() ?? '';
  const ordenar = ordenarParam === 'nota' ? ordenarParam : '';
  const [paginaInicial, tags] = await Promise.all([
    listarServicos(0, ordenar ? 100 : QUANTIDADE_DESTAQUE, cidade),
    listarTags(),
  ]);
  let servicosDisponiveis = paginaInicial.conteudo;

  if (ordenar && paginaInicial.totalPaginas > 1) {
    const demaisPaginas = await Promise.all(
      Array.from(
        { length: paginaInicial.totalPaginas - 1 },
        (_, indice) => listarServicos(indice + 1, 100, cidade),
      ),
    );
    servicosDisponiveis = [
      ...paginaInicial.conteudo,
      ...demaisPaginas.flatMap((pagina) => pagina.conteudo),
    ];
  }

  const notasPorServico = new Map<string, number>();
  if (ordenar) {
    const medias = await Promise.all(
      servicosDisponiveis.map(async (servico) => [
        servico.id,
        await buscarMediaPorServico(servico.id),
      ] as const),
    );
    for (const [servicoId, media] of medias) {
      notasPorServico.set(servicoId, media);
    }
    servicosDisponiveis.sort(
      (servicoA, servicoB) =>
        (notasPorServico.get(servicoB.id) ?? 0) - (notasPorServico.get(servicoA.id) ?? 0),
    );
  }
  const servicos = ordenar
    ? servicosDisponiveis.slice(0, QUANTIDADE_DESTAQUE)
    : servicosDisponiveis;

  const tagsPorId = new Map(tags.map((tag) => [tag.id, tag]));

  const perfilIds = [...new Set(servicos.map((servico) => servico.perfilId))];
  const perfis = await Promise.all(
    perfilIds.map((id) => buscarPerfilPorId(id).catch(() => null)),
  );
  const perfilPorId = new Map<string, Perfil | null>(
    perfilIds.map((id, indice) => [id, perfis[indice] ?? null]),
  );

  return (
    <div className="flex flex-col gap-14">
      <BannerHome cidade={cidade} ordenar={ordenar} />

      <MiniMenuCategorias tags={tags} />

      <section className="flex flex-col gap-4">
        <div className="flex items-center justify-between gap-4">
          <h2 className="font-heading text-2xl font-bold">
            {cidade ? `Serviços em cidades com “${cidade}”` : 'Serviços em destaque'}
          </h2>
          <Link
            href={cidade ? `/servicos?cidade=${encodeURIComponent(cidade)}` : '/servicos'}
            className={classesDoBotao('secundario')}
          >
            Ver todos
          </Link>
        </div>

        {servicos.length === 0 ? (
          <p className="text-texto-suave">
            {cidade
              ? 'Nenhum serviço encontrado para essa cidade.'
              : 'Nenhum serviço ativo no momento.'}
          </p>
        ) : (
          <ul className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {servicos.map((servico) => (
              <li key={servico.id}>
                <CardServico
                  servico={servico}
                  tags={servico.tagIds.map((id) => tagsPorId.get(id)).filter((tag) => tag !== undefined)}
                  perfil={perfilPorId.get(servico.perfilId) ?? null}
                  notaMedia={notasPorServico.get(servico.id)}
                />
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
