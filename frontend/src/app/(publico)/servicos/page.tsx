import Link from 'next/link';

import type { Perfil } from '@/api/contratos/perfil';
import { buscarPerfilPorId } from '@/api/perfil';
import { listarServicos } from '@/api/servicos';
import { listarTags } from '@/api/tags';
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

  const [{ conteudo: servicos, totalPaginas }, tags] = await Promise.all([
    listarServicos(pagina, TAMANHO_PAGINA),
    listarTags(),
  ]);

  const tagsPorId = new Map(tags.map((tag) => [tag.id, tag]));

  const perfilIds = [...new Set(servicos.map((servico) => servico.perfilId))];
  const perfis = await Promise.all(
    perfilIds.map((id) => buscarPerfilPorId(id).catch(() => null)),
  );
  const perfilPorId = new Map<string, Perfil | null>(
    perfilIds.map((id, indice) => [id, perfis[indice] ?? null]),
  );

  return (
    <div className="flex flex-col gap-8">
      <header className="flex flex-col gap-2">
        <h1 className="font-heading text-3xl font-bold">Serviços disponíveis</h1>
        <p className="text-texto-suave">Encontre um profissional para o que você precisa.</p>
      </header>

      {servicos.length === 0 ? (
        <p className="text-texto-suave">Nenhum serviço ativo no momento.</p>
      ) : (
        <ul className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {servicos.map((servico) => (
            <li key={servico.id}>
              <CardServico
                servico={servico}
                tags={servico.tagIds.map((id) => tagsPorId.get(id)).filter((tag) => tag !== undefined)}
                perfil={perfilPorId.get(servico.perfilId) ?? null}
              />
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
