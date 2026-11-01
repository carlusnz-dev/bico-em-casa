import Link from 'next/link';
import { notFound } from 'next/navigation';

import { buscarMediaPorProfissional, listarAvaliacoesRecebidas } from '@/api/avaliacoes';
import type { PerfilTipo } from '@/api/contratos/perfil';
import { ErroApi } from '@/api/erros';
import { buscarPerfilPorId } from '@/api/perfil';
import { listarServicos } from '@/api/servicos';
import { AvaliacaoMedia } from '@/components/AvaliacaoMedia';
import { AvatarIniciais } from '@/components/AvatarIniciais';
import { DataFormatada } from '@/components/DataFormatada';
import { formatarPreco } from '@/components/formatarPreco';
import { Card } from '@/components/ui/Card';

const LABEL_TIPO: Record<PerfilTipo, string> = {
  CLIENTE: 'Cliente',
  PROFISSIONAL: 'Profissional',
  ADMIN: 'Administrador',
};

const QUANTIDADE_SERVICOS_PRINCIPAIS = 6;
// Não há endpoint de "serviços por profissional" — buscamos uma página larga
// da listagem pública e filtramos por perfilId, igual à vitrine do portfólio.
const TAMANHO_PAGINA_PARA_FILTRAR = 100;

export default async function PerfilPublicoPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;

  const perfil = await buscarPerfilPorId(id).catch((erro) => {
    if (erro instanceof ErroApi && (erro.status === 404 || erro.status === 400)) {
      notFound();
    }
    throw erro;
  });

  const ehProfissional = perfil.tipo === 'PROFISSIONAL';

  const [notaMedia, avaliacoes, { conteudo: todosServicos }] = ehProfissional
    ? await Promise.all([
        buscarMediaPorProfissional(perfil.id).catch(() => null),
        listarAvaliacoesRecebidas(perfil.id).catch(() => []),
        listarServicos(0, TAMANHO_PAGINA_PARA_FILTRAR).catch(() => ({ conteudo: [] })),
      ])
    : [null, [], { conteudo: [] }];

  const servicos = todosServicos
    .filter((servico) => servico.perfilId === perfil.id && servico.ativo)
    .slice(0, QUANTIDADE_SERVICOS_PRINCIPAIS);

  return (
    <div className="mx-auto flex max-w-2xl flex-col gap-6">
      <Card className="flex flex-col gap-4">
        <div className="flex items-start gap-4">
          {perfil.fotoUrl ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img src={perfil.fotoUrl} alt="" className="h-16 w-16 shrink-0 rounded-full object-cover" />
          ) : (
            <AvatarIniciais nome={perfil.nomeExibicao} tamanho="grande" />
          )}

          <div className="flex flex-col gap-1">
            <h1 className="font-heading text-2xl font-bold">{perfil.nomeExibicao}</h1>
            <span className="w-fit rounded-full bg-primary/10 px-2 py-0.5 text-xs font-medium text-primary">
              {LABEL_TIPO[perfil.tipo]}
            </span>
            <p className="text-sm text-texto-suave">
              No Bico em Casa desde <DataFormatada dataIso={perfil.criadoEm} />
            </p>
            {ehProfissional && notaMedia !== null && notaMedia > 0 && (
              <div className="flex flex-col gap-1">
                <span className="text-xs text-texto-suave">Nota média do profissional</span>
                <AvaliacaoMedia nota={notaMedia} quantidade={avaliacoes.length} />
              </div>
            )}
            {ehProfissional && notaMedia === 0 && (
              <span className="text-xs text-texto-suave">Profissional sem avaliações</span>
            )}
            {ehProfissional && notaMedia === null && (
              <span className="text-xs text-texto-suave" role="alert">
                Nota do profissional indisponível
              </span>
            )}
          </div>
        </div>

        {perfil.bio && <p className="whitespace-pre-line text-texto-suave">{perfil.bio}</p>}

        <Link
          href={`/perfil/${perfil.id}/avaliacoes`}
          className="w-fit text-sm font-medium text-primary hover:underline"
        >
          Ver avaliações
        </Link>
      </Card>

      {ehProfissional && servicos.length > 0 && (
        <Card className="flex flex-col gap-3">
          <h2 className="text-lg font-semibold">Principais serviços</h2>
          <ul className="flex flex-col gap-2">
            {servicos.map((servico) => (
              <li key={servico.id} className="flex items-center justify-between text-sm">
                <span>{servico.titulo}</span>
                <span className="text-texto-suave">
                  {formatarPreco(servico.precoPrevio, servico.unidadePreco)}
                </span>
              </li>
            ))}
          </ul>
        </Card>
      )}
    </div>
  );
}
