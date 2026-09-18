import Link from 'next/link';

import { listarServicos } from '@/api/servicos';
import { CardServico } from '@/components/CardServico';
import { classesDoBotao } from '@/components/ui/Botao';
import { SaudacaoSessao } from '@/components/SaudacaoSessao';

const QUANTIDADE_DESTAQUE = 6;

export const dynamic = 'force-dynamic';

export default async function HomePage() {
  const { conteudo: servicos } = await listarServicos(0, QUANTIDADE_DESTAQUE);

  return (
    <div className="flex flex-col gap-10">
      <SaudacaoSessao />

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
                <CardServico servico={servico} />
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
