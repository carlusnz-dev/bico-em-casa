import { notFound } from 'next/navigation';

import { listarAvaliacoesRecebidas } from '@/api/avaliacoes';
import { ErroApi } from '@/api/erros';
import { buscarPerfilPorId } from '@/api/perfil';
import { buscarPortfolioPorId } from '@/api/portfolio';
import { listarServicos } from '@/api/servicos';
import { AvaliacaoMedia } from '@/components/AvaliacaoMedia';
import { AvatarIniciais } from '@/components/AvatarIniciais';
import { formatarPreco } from '@/components/formatarPreco';
import { DataFormatada } from '@/components/DataFormatada';
import { Card } from '@/components/ui/Card';

const QUANTIDADE_SERVICOS_PRINCIPAIS = 6;
// Não há endpoint de "serviços por profissional" — buscamos uma página larga
// da listagem pública e filtramos por perfilId, que já é dado real.
const TAMANHO_PAGINA_PARA_FILTRAR = 100;

export default async function PortfolioDetalhePage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;

  const portfolio = await buscarPortfolioPorId(Number(id)).catch((erro) => {
    if (erro instanceof ErroApi && (erro.status === 404 || erro.status === 400)) {
      notFound();
    }
    throw erro;
  });

  const [perfil, avaliacoes, { conteudo: todosServicos }] = await Promise.all([
    buscarPerfilPorId(portfolio.perfilId).catch(() => null),
    listarAvaliacoesRecebidas(portfolio.perfilId).catch(() => []),
    listarServicos(0, TAMANHO_PAGINA_PARA_FILTRAR).catch(() => ({ conteudo: [] })),
  ]);

  const servicosDoProfissional = todosServicos
    .filter((servico) => servico.perfilId === portfolio.perfilId && servico.ativo)
    .slice(0, QUANTIDADE_SERVICOS_PRINCIPAIS);

  const notaMedia = avaliacoes.length
    ? avaliacoes.reduce((soma, avaliacao) => soma + avaliacao.nota, 0) / avaliacoes.length
    : null;

  const comentarios = avaliacoes.filter((avaliacao) => avaliacao.comentario);

  return (
    <div className="flex flex-col gap-6">
      <Card className="flex flex-col gap-4">
        <div className="flex items-start gap-4">
          {perfil?.fotoUrl ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img
              src={perfil.fotoUrl}
              alt=""
              className="h-16 w-16 shrink-0 rounded-full object-cover"
            />
          ) : (
            <AvatarIniciais nome={perfil?.nomeExibicao ?? portfolio.titulo} tamanho="grande" />
          )}

          <div className="flex flex-col gap-1">
            <h1 className="font-heading text-2xl font-bold">
              {perfil?.nomeExibicao ?? portfolio.titulo}
            </h1>
            {perfil && (
              <p className="text-texto-suave text-sm">
                Desde <DataFormatada dataIso={perfil.criadoEm} />
              </p>
            )}
            {notaMedia !== null && (
              <AvaliacaoMedia nota={notaMedia} quantidade={avaliacoes.length} />
            )}
          </div>
        </div>

        {perfil?.bio && <p className="text-texto-suave whitespace-pre-line">{perfil.bio}</p>}
      </Card>

      <Card className="flex flex-col gap-4 overflow-hidden p-0">
        <div className="bg-borda/30 aspect-video w-full">
          {portfolio.fotoCapaUrl && (
            // eslint-disable-next-line @next/next/no-img-element
            <img
              src={portfolio.fotoCapaUrl}
              alt={portfolio.titulo}
              className="h-full w-full object-cover"
            />
          )}
        </div>

        <div className="flex flex-col gap-2 p-6 pt-0">
          <h2 className="font-heading text-xl font-bold">{portfolio.titulo}</h2>
          {portfolio.descricao && (
            <p className="text-texto-suave whitespace-pre-line">{portfolio.descricao}</p>
          )}
        </div>
      </Card>

      {servicosDoProfissional.length > 0 && (
        <Card className="flex flex-col gap-3">
          <h2 className="text-lg font-semibold">Principais serviços</h2>
          <ul className="flex flex-col gap-2">
            {servicosDoProfissional.map((servico) => (
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

      {comentarios.length > 0 && (
        <Card className="flex flex-col gap-4">
          <h2 className="text-lg font-semibold">Comentários</h2>
          <ul className="flex flex-col gap-4">
            {comentarios.map((avaliacao) => (
              <li
                key={avaliacao.id}
                className="border-borda flex flex-col gap-1 border-b pb-4 last:border-0 last:pb-0"
              >
                <AvaliacaoMedia nota={avaliacao.nota} />
                <p className="text-texto-suave text-sm">{avaliacao.comentario}</p>
              </li>
            ))}
          </ul>
        </Card>
      )}
    </div>
  );
}
