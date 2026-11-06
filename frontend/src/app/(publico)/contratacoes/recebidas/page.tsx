'use client';

import { X } from 'lucide-react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';

import {
  alterarStatusContratacao,
  listarContratacoesRecebidas,
} from '@/api/contratacoes';
import type { Contratacao, StatusContratacao } from '@/api/contratos/contratacao';
import type { Perfil } from '@/api/contratos/perfil';
import { ErroApi } from '@/api/erros';
import { buscarPerfilPorId } from '@/api/perfil';
import { AvatarIniciais } from '@/components/AvatarIniciais';
import { ChatContratacao } from '@/components/ChatContratacao';
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

const CONFIG_STATUS: Record<StatusContratacao, { rotulo: string; classes: string }> = {
  EM_ANDAMENTO: {
    rotulo: 'Em andamento',
    classes: 'bg-blue-500/10 text-blue-600',
  },
  CONCLUIDO: {
    rotulo: 'Concluído',
    classes: 'bg-green-600/10 text-green-700',
  },
  CANCELADO: {
    rotulo: 'Cancelado',
    classes: 'bg-erro/10 text-erro',
  },
};

function obterInfoStatus(status: StatusContratacao) {
  return (
    CONFIG_STATUS[status] ?? {
      rotulo: status,
      classes: 'bg-borda/40 text-texto-suave',
    }
  );
}

export default function ContratacoesRecebidasPage() {
  const { accessToken, status, perfil } = useSessao();
  const router = useRouter();
  const [contratacoes, setContratacoes] = useState<Contratacao[] | null>(null);
  const [perfisClientes, setPerfisClientes] = useState<Record<string, Perfil>>({});
  const [erro, setErro] = useState<string | null>(null);
  const [idEmAcao, setIdEmAcao] = useState<string | null>(null);
  const [abertaId, setAbertaId] = useState<string | null>(null);

  const contratacaoAberta = contratacoes?.find((c) => c.id === abertaId) ?? null;
  const clienteAberto = contratacaoAberta ? perfisClientes[contratacaoAberta.clienteId] : undefined;

  useEffect(() => {
    if (status === 'anonimo') {
      router.push('/login');
    }
  }, [status, router]);

  useEffect(() => {
    if (!accessToken) return;

    listarContratacoesRecebidas(accessToken)
      .then((pagina) => setContratacoes(pagina.conteudo))
      .catch((causa) =>
        setErro(mensagemDeErro(causa, 'Não foi possível carregar as contratações recebidas.')),
      );
  }, [accessToken]);

  useEffect(() => {
    if (!contratacoes || contratacoes.length === 0) return;

    const idsUnicos = Array.from(new Set(contratacoes.map((c) => c.clienteId)));
    Promise.all(
      idsUnicos.map(async (clienteId) => {
        try {
          const p = await buscarPerfilPorId(clienteId);
          return [clienteId, p] as const;
        } catch {
          return null;
        }
      }),
    ).then((resultados) => {
      const mapa: Record<string, Perfil> = {};
      for (const item of resultados) {
        if (item) {
          mapa[item[0]] = item[1];
        }
      }
      setPerfisClientes(mapa);
    });
  }, [contratacoes]);

  useEffect(() => {
    if (!abertaId) return;

    function aoTeclar(evento: KeyboardEvent) {
      if (evento.key === 'Escape') {
        setAbertaId(null);
      }
    }

    window.addEventListener('keydown', aoTeclar);
    return () => window.removeEventListener('keydown', aoTeclar);
  }, [abertaId]);

  async function mudarStatus(id: string, novoStatus: StatusContratacao) {
    if (!accessToken) return;

    setIdEmAcao(id);
    setErro(null);

    try {
      const atualizada = await alterarStatusContratacao(id, { status: novoStatus }, accessToken);
      setContratacoes((atual) =>
        atual ? atual.map((item) => (item.id === atualizada.id ? atualizada : item)) : atual,
      );
    } catch (causa) {
      setErro(mensagemDeErro(causa, 'Não foi possível atualizar o status da contratação.'));
    } finally {
      setIdEmAcao(null);
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <h1 className="font-heading text-3xl font-bold">Minhas contratações</h1>

      {erro && (
        <p role="alert" className="rounded-lg bg-erro/10 px-3 py-2 text-sm text-erro">
          {erro}
        </p>
      )}

      {contratacoes === null ? (
        <p className="text-texto-suave">Carregando…</p>
      ) : contratacoes.length === 0 ? (
        <p className="text-texto-suave">Você ainda não recebeu nenhuma contratação.</p>
      ) : (
        <>
          <ul className="flex flex-col gap-3">
            {contratacoes.map((contratacao) => {
              const infoStatus = obterInfoStatus(contratacao.status);
              const cliente = perfisClientes[contratacao.clienteId];

              return (
                <li key={contratacao.id}>
                  <button
                    type="button"
                    onClick={() => setAbertaId(contratacao.id)}
                    className="w-full text-left"
                  >
                    <Card className="flex items-center justify-between gap-4 transition-shadow hover:shadow-md">
                      <div className="flex flex-col gap-1">
                        <div className="flex flex-wrap items-center gap-2">
                          <h2 className="text-lg font-semibold text-texto">{contratacao.tituloServico}</h2>
                          <span className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${infoStatus.classes}`}>
                            {infoStatus.rotulo}
                          </span>
                        </div>
                        <p className="text-sm text-texto-suave">
                          Contratado por{' '}
                          <span className="font-medium text-texto">
                            {cliente ? cliente.nomeExibicao : 'Carregando…'}
                          </span>
                        </p>
                      </div>

                      <span className="shrink-0 text-xs font-medium text-primary">
                        Abrir contratação
                      </span>
                    </Card>
                  </button>
                </li>
              );
            })}
          </ul>

          {contratacaoAberta && (
            <div
              role="dialog"
              aria-modal="true"
              aria-labelledby="titulo-modal-recebida"
              onClick={() => setAbertaId(null)}
              className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-3 backdrop-blur-xs sm:p-6"
            >
              <div
                onClick={(e) => e.stopPropagation()}
                className="relative flex max-h-[92vh] w-full max-w-4xl flex-col gap-5 overflow-y-auto rounded-2xl border border-borda bg-superficie p-6 shadow-2xl"
              >
                <header className="flex items-start justify-between gap-4 border-b border-borda pb-4">
                  <div className="flex flex-col gap-1">
                    <div className="flex flex-wrap items-center gap-2.5">
                      <h2 id="titulo-modal-recebida" className="font-heading text-2xl font-bold text-texto">
                        {contratacaoAberta.tituloServico}
                      </h2>
                      <span
                        className={`rounded-full px-3 py-1 text-xs font-medium ${
                          obterInfoStatus(contratacaoAberta.status).classes
                        }`}
                      >
                        {obterInfoStatus(contratacaoAberta.status).rotulo}
                      </span>
                    </div>
                    <p className="text-sm text-texto-suave">
                      Contratado em {formatarData(contratacaoAberta.criadoEm)}
                    </p>
                  </div>

                  <button
                    type="button"
                    onClick={() => setAbertaId(null)}
                    aria-label="Fechar card da contratação"
                    className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg text-texto-suave transition-colors hover:bg-borda/40 hover:text-texto"
                  >
                    <X size={20} />
                  </button>
                </header>

                <div className="flex flex-col gap-3 border-b border-borda pb-4">
                  <p className="text-xl font-semibold text-primary">
                    {formatarPreco(contratacaoAberta.precoServico)}
                  </p>

                  {contratacaoAberta.observacao && (
                    <div className="flex flex-col gap-1">
                      <h3 className="text-sm font-medium text-texto">Observação do cliente</h3>
                      <p className="whitespace-pre-line text-sm text-texto-suave">
                        {contratacaoAberta.observacao}
                      </p>
                    </div>
                  )}
                </div>

                <div className="flex flex-col gap-2 border-b border-borda pb-4">
                  <h3 className="text-sm font-medium text-texto">Cliente contratante</h3>
                  {clienteAberto ? (
                    <div className="flex items-center justify-between gap-3">
                      <div className="flex items-center gap-3">
                        <AvatarIniciais nome={clienteAberto.nomeExibicao} />
                        <div className="flex flex-col">
                          <span className="font-medium">
                            {clienteAberto.nomeExibicao}
                          </span>
                          {clienteAberto.bio && (
                            <span className="text-sm text-texto-suave">
                              {clienteAberto.bio}
                            </span>
                          )}
                        </div>
                      </div>
                      <div className="flex flex-col items-end gap-1">
                        <Link
                          href={`/perfil/${clienteAberto.id}`}
                          className="text-sm font-medium text-primary hover:underline"
                        >
                          Ver perfil
                        </Link>
                        <Link
                          href={`/perfil/${clienteAberto.id}/avaliacoes`}
                          className="text-sm font-medium text-primary hover:underline"
                        >
                          Ver avaliações
                        </Link>
                      </div>
                    </div>
                  ) : (
                    <p className="text-sm text-texto-suave">Carregando dados do cliente…</p>
                  )}
                </div>

                <ChatContratacao
                  contratacaoId={contratacaoAberta.id}
                  meuPerfilId={contratacaoAberta.profissionalId}
                  meuNome={perfil?.nomeExibicao}
                  outroNome={clienteAberto?.nomeExibicao}
                  criadoEm={contratacaoAberta.criadoEm}
                />

                <div className="flex flex-wrap items-center justify-between gap-2 border-t border-borda pt-4">
                  <div className="flex flex-wrap gap-2">
                    {contratacaoAberta.status === 'EM_ANDAMENTO' && (
                      <>
                        <Botao
                          variante="primario"
                          carregando={idEmAcao === contratacaoAberta.id}
                          onClick={() => mudarStatus(contratacaoAberta.id, 'CONCLUIDO')}
                        >
                          Concluir serviço
                        </Botao>
                        <Botao
                          variante="secundario"
                          className="text-erro hover:bg-erro/10"
                          carregando={idEmAcao === contratacaoAberta.id}
                          onClick={() => mudarStatus(contratacaoAberta.id, 'CANCELADO')}
                        >
                          Cancelar contratação
                        </Botao>
                      </>
                    )}
                    <Link
                      href={`/contratacoes/${contratacaoAberta.id}/denunciar`}
                      className={classesDoBotao('fantasma')}
                    >
                      Denunciar
                    </Link>
                  </div>

                  <Botao variante="fantasma" onClick={() => setAbertaId(null)}>
                    Fechar
                  </Botao>
                </div>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}
