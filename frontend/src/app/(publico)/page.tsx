import Link from 'next/link';

import type { Perfil } from '@/api/contratos/perfil';
import { buscarPerfilPorId } from '@/api/perfil';
import { listarServicos } from '@/api/servicos';
import { listarTags } from '@/api/tags';
import { BannerHome } from '@/components/BannerHome';
import { CardServico } from '@/components/CardServico';
import { MiniMenuCategorias } from '@/components/MiniMenuCategorias';
import { classesDoBotao } from '@/components/ui/Botao';

const QUANTIDADE_DESTAQUE = 6;

export const dynamic = 'force-dynamic';

export default async function HomePage() {
  const [{ conteudo: servicos }, tags] = await Promise.all([
    listarServicos(0, QUANTIDADE_DESTAQUE),
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
    <div className="flex flex-col gap-14">
      <BannerHome />

      <MiniMenuCategorias tags={tags} />

      <section className="flex flex-col gap-4">
        <div className="flex items-center justify-between gap-4">
          <h2 className="font-heading text-2xl font-bold">Serviços em destaque</h2>
          <Link href="/servicos" className={classesDoBotao('secundario')}>
            Ver todos
          </Link>
        </div>

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
      </section>
    </div>
  );
}
