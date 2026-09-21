'use client';

import Link from 'next/link';
import { useParams, useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';

import {
  arquivarContratacao,
  buscarContratacaoPorId,
  desarquivarContratacao,
} from '@/api/contratacoes';
import type { Contratacao } from '@/api/contratos/contratacao';
import type { Perfil } from '@/api/contratos/perfil';
import { ErroApi } from '@/api/erros';
import { buscarPerfilPorId } from '@/api/perfil';
import { AvatarIniciais } from '@/components/AvatarIniciais';
import { Botao, classesDoBotao } from '@/components/ui/Botao';
import { Card } from '@/components/ui/Card';
import { useSessao } from '@/hooks/useSessao';

function mensagemDeErro(causa: unknown, padrao: string): string {
  if (causa instanceof ErroApi) {
    return causa.semConexao ? 'Não foi possível falar com o servidor. Tente de novo.' : (causa.detail ?? padrao);
  }
  return padrao;
}

function formatarData(data: string): string {
  return new Date(data).toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: 'long',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function formatarPreco(preco: number): string {
  return preco.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}

export default function DetalheContratacaoPage() {
  const { id } = useParams<{ id: string }>();
  const { accessToken, status } = useSessao();
  const router = useRouter();
  const [contratacao, setContratacao] = useState<Contratacao | null>(null);
  const [profissional, setProfissional] = useState<Perfil | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [carregandoAcao, setCarregandoAcao] = useState(false);

  useEffect(() => {
    if (status === 'anonimo') {
      router.push('/login');
    }
  }, [status, router]);

  useEffect(() => {
    if (!accessToken) return;

    buscarContratacaoPorId(id, accessToken)
      .then(setContratacao)
      .catch((causa) => setErro(mensagemDeErro(causa, 'Não foi possível carregar esta contratação.')));
  }, [accessToken, id]);

  useEffect(() => {
    if (!contratacao) return;

    buscarPerfilPorId(contratacao.profissionalId)
      .then(setProfissional)
      .catch(() => setProfissional(null));
  }, [contratacao]);

  async function alternarStatus() {
    if (!accessToken || !contratacao) return;

    setCarregandoAcao(true);
    setErro(null);

    try {
      const atualizada =
        contratacao.status === 'ATIVA'
          ? await arquivarContratacao(contratacao.id, accessToken)
          : await desarquivarContratacao(contratacao.id, accessToken);
      setContratacao(atualizada);
    } catch (causa) {
      setErro(mensagemDeErro(causa, 'Não foi possível atualizar esta contratação.'));
    } finally {
      setCarregandoAcao(false);
    }
  }

  if (erro && !contratacao) {
    return (
      <p role="alert" className="rounded-lg bg-erro/10 px-3 py-2 text-sm text-erro">
        {erro}
      </p>
    );
  }

  if (!contratacao) {
    return <p className="text-texto-suave">Carregando…</p>;
  }

  return (
    <div className="mx-auto flex max-w-xl flex-col gap-6">
      <Card className="flex flex-col gap-4">
        <header className="flex items-start justify-between gap-4">
          <div className="flex flex-col gap-1">
            <h1 className="font-heading text-2xl font-bold">{contratacao.tituloServico}</h1>
            <p className="text-sm text-texto-suave">Contratado em {formatarData(contratacao.criadoEm)}</p>
          </div>
          <span
            className={
              contratacao.status === 'ATIVA'
                ? 'rounded-full bg-primary/10 px-3 py-1 text-xs font-medium text-primary'
                : 'rounded-full bg-borda/40 px-3 py-1 text-xs font-medium text-texto-suave'
            }
          >
            {contratacao.status === 'ATIVA' ? 'Ativa' : 'Arquivada'}
          </span>
        </header>

        <p className="text-lg font-medium text-primary">{formatarPreco(contratacao.precoServico)}</p>

        {contratacao.observacao && (
          <div className="flex flex-col gap-1">
            <h2 className="text-sm font-medium text-texto">Observação</h2>
            <p className="whitespace-pre-line text-texto-suave">{contratacao.observacao}</p>
          </div>
        )}
      </Card>

      <Card className="flex flex-col gap-2">
        <h2 className="text-sm font-medium text-texto">Profissional contratado</h2>
        {profissional ? (
          <div className="flex items-center justify-between gap-3">
            <div className="flex items-center gap-3">
              <AvatarIniciais nome={profissional.nomeExibicao} />
              <div className="flex flex-col">
                <span className="font-medium">{profissional.nomeExibicao}</span>
                {profissional.bio && <span className="text-sm text-texto-suave">{profissional.bio}</span>}
              </div>
            </div>
            <Link
              href={`/perfil/${profissional.id}/avaliacoes`}
              className="text-sm font-medium text-primary hover:underline"
            >
              Ver avaliações
            </Link>
          </div>
        ) : (
          <p className="text-sm text-texto-suave">Não foi possível carregar os dados do profissional.</p>
        )}
      </Card>

      {erro && (
        <p role="alert" className="rounded-lg bg-erro/10 px-3 py-2 text-sm text-erro">
          {erro}
        </p>
      )}

      <div className="flex gap-2">
        <Link href={`/contratacoes/${contratacao.id}/editar`} className={classesDoBotao('secundario')}>
          Editar
        </Link>
        <Link href={`/contratacoes/${contratacao.id}/avaliar`} className={classesDoBotao('secundario')}>
          Avaliar
        </Link>
        <Link href={`/contratacoes/${contratacao.id}/denunciar`} className={classesDoBotao('fantasma')}>
          Denunciar
        </Link>
        <Botao variante="fantasma" carregando={carregandoAcao} onClick={alternarStatus}>
          {contratacao.status === 'ATIVA' ? 'Arquivar' : 'Desarquivar'}
        </Botao>
      </div>
    </div>
  );
}
