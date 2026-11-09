'use client';

import { Bell, ChevronDown, ChevronUp } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';

import {
  enviarMensagemContratacao,
  listarMensagensContratacao,
} from '@/api/contratacoes';
import type { MensagemContratacao } from '@/api/contratos/contratacao';
import { ErroApi } from '@/api/erros';
import { AvatarIniciais } from '@/components/AvatarIniciais';
import { Botao } from '@/components/ui/Botao';
import { Card } from '@/components/ui/Card';
import { useSessao } from '@/hooks/useSessao';

function formatarHora(data?: string): string {
  const alvo = data ? new Date(data) : new Date();
  return alvo.toLocaleTimeString('pt-BR', {
    hour: '2-digit',
    minute: '2-digit',
  });
}

function mensagemDeErro(causa: unknown, padrao: string): string {
  if (causa instanceof ErroApi) {
    return causa.semConexao
      ? 'Não foi possível falar com o servidor. Tente de novo.'
      : (causa.detail ?? padrao);
  }
  return padrao;
}

interface AutorLocal {
  autorPerfilId: string;
  autorNome?: string;
}

function chaveAutoresLocais(contratacaoId: string): string {
  return `bico_chat_autores_${contratacaoId}`;
}

function lerAutoresLocais(contratacaoId: string): Record<string, AutorLocal> {
  if (typeof window === 'undefined') return {};
  try {
    const bruto = window.localStorage.getItem(chaveAutoresLocais(contratacaoId));
    return bruto ? (JSON.parse(bruto) as Record<string, AutorLocal>) : {};
  } catch {
    return {};
  }
}

function salvarAutorLocal(contratacaoId: string, mensagemId: string, autor: AutorLocal) {
  if (typeof window === 'undefined') return;
  try {
    const atual = lerAutoresLocais(contratacaoId);
    atual[mensagemId] = autor;
    window.localStorage.setItem(chaveAutoresLocais(contratacaoId), JSON.stringify(atual));
  } catch {
    // ignora falha de armazenamento local
  }
}

function reconciliarMensagens(
  contratacaoId: string,
  lista: MensagemContratacao[],
): MensagemContratacao[] {
  const mapa = lerAutoresLocais(contratacaoId);
  return lista.map((msg) => {
    const sobrescrito = mapa[msg.id];
    if (!sobrescrito) return msg;
    return {
      ...msg,
      autorPerfilId: sobrescrito.autorPerfilId,
      autorNome: sobrescrito.autorNome ?? msg.autorNome,
    };
  });
}

interface ChatContratacaoProps {
  contratacaoId: string;
  meuPerfilId: string;
  meuNome?: string;
  outroNome?: string;
  criadoEm?: string;
}

export function ChatContratacao({
  contratacaoId,
  meuPerfilId,
  meuNome,
  outroNome,
  criadoEm,
}: ChatContratacaoProps) {
  const { accessToken } = useSessao();
  const [mensagens, setMensagens] = useState<MensagemContratacao[]>([]);
  const [carregandoInicial, setCarregandoInicial] = useState(true);
  const [texto, setTexto] = useState('');
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [avisoExpandido, setAvisoExpandido] = useState(true);
  const listaRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (!accessToken) return;

    let ativo = true;

    async function carregar() {
      if (!accessToken) return;
      try {
        const dados = await listarMensagensContratacao(contratacaoId, accessToken);
        if (ativo) {
          setMensagens(reconciliarMensagens(contratacaoId, dados));
          setCarregandoInicial(false);
        }
      } catch (causa) {
        if (ativo && carregandoInicial) {
          setErro(mensagemDeErro(causa, 'Não foi possível carregar o chat.'));
          setCarregandoInicial(false);
        }
      }
    }

    carregar();
    const intervalo = setInterval(carregar, 2500);

    return () => {
      ativo = false;
      clearInterval(intervalo);
    };
  }, [accessToken, contratacaoId, carregandoInicial]);

  useEffect(() => {
    if (listaRef.current) {
      listaRef.current.scrollTop = listaRef.current.scrollHeight;
    }
  }, [mensagens.length]);

  async function aoEnviar(evento: React.FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    const conteudo = texto.trim();
    if (!conteudo || !accessToken || enviando) return;

    setEnviando(true);
    setErro(null);

    try {
      const nova = await enviarMensagemContratacao(
        contratacaoId,
        { conteudo, autorPerfilId: meuPerfilId },
        accessToken,
      );

      salvarAutorLocal(contratacaoId, nova.id, {
        autorPerfilId: meuPerfilId,
        autorNome: meuNome,
      });

      const mensagemFinal: MensagemContratacao = {
        ...nova,
        autorPerfilId: meuPerfilId,
        autorNome: meuNome ?? nova.autorNome,
      };

      setMensagens((atual) => [...atual, mensagemFinal]);
      setTexto('');
    } catch (causa) {
      setErro(mensagemDeErro(causa, 'Não foi possível enviar a mensagem.'));
    } finally {
      setEnviando(false);
    }
  }

  return (
    <Card className="flex flex-col gap-3">
      <div className="flex items-center justify-between border-b border-borda/60 pb-2">
        <h2 className="text-sm font-medium text-texto">Chat com o prestador de serviço</h2>
        <span className="text-xs text-texto-suave">Atualização automática</span>
      </div>

      <div
        ref={listaRef}
        className="flex max-h-96 min-h-64 flex-col gap-3.5 overflow-y-auto rounded-xl bg-fundo p-3.5"
      >
        <div className="rounded-lg border-l-4 border-blue-600 bg-slate-100 p-3.5 text-xs leading-relaxed text-slate-700 shadow-2xs dark:bg-slate-800/90 dark:text-slate-200">
          <div className="mb-2 inline-flex items-center gap-1.5 rounded-md bg-blue-100 px-2 py-0.5 text-[11px] font-bold tracking-wide text-blue-700 uppercase dark:bg-blue-500/20 dark:text-blue-300">
            <Bell className="h-3 w-3" />
            <span>Sistema</span>
          </div>

          <p className={avisoExpandido ? '' : 'line-clamp-2'}>
            <strong className="font-semibold text-slate-900 dark:text-white">
              Atenção: Este serviço é de responsabilidade exclusiva do prestador contratado.
            </strong>{' '}
            O Bico em Casa atua apenas como intermediador da conexão entre cliente e profissional.
            Utilize este chat para combinar detalhes e horários do atendimento, porém{' '}
            <strong className="font-semibold text-slate-900 dark:text-white">
              a execução, manutenção e garantia do serviço, como dito acima, são de dever do
              prestador.
            </strong>{' '}
            Caso não concorde com as condições combinadas, solicite o cancelamento da contratação.
          </p>

          <div className="mt-2.5 flex items-center justify-between text-[11px] text-slate-500 dark:text-slate-400">
            <button
              type="button"
              onClick={() => setAvisoExpandido((atual) => !atual)}
              className="inline-flex items-center gap-1 font-medium text-slate-600 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white"
            >
              {avisoExpandido ? (
                <>
                  <span>Ver menos</span>
                  <ChevronUp className="h-3.5 w-3.5" />
                </>
              ) : (
                <>
                  <span>Ver mais</span>
                  <ChevronDown className="h-3.5 w-3.5" />
                </>
              )}
            </button>
            <span>{formatarHora(criadoEm)}</span>
          </div>
        </div>

        {carregandoInicial ? (
          <p className="my-auto text-center text-sm text-texto-suave">Carregando conversa…</p>
        ) : mensagens.length === 0 ? (
          <p className="my-auto py-4 text-center text-xs text-texto-suave">
            Nenhuma mensagem enviada ainda. Use o campo abaixo para iniciar a conversa.
          </p>
        ) : (
          <div className="flex flex-col gap-3">
            {mensagens.map((msg) => {
              const minha =
                String(msg.autorPerfilId).toLowerCase() === String(meuPerfilId).toLowerCase();
              const nomeExibido = minha
                ? (meuNome ?? 'Você')
                : (outroNome ?? msg.autorNome);

              return (
                <div
                  key={msg.id}
                  className={`flex w-full items-end gap-2.5 ${
                    minha ? 'justify-end' : 'justify-start'
                  }`}
                >
                  {!minha && <AvatarIniciais nome={nomeExibido} />}

                  <div
                    className={`flex max-w-[75%] flex-col gap-1 rounded-2xl px-4 py-2.5 text-sm shadow-xs ${
                      minha
                        ? 'rounded-br-none bg-primary text-white'
                        : 'rounded-bl-none border border-borda bg-superficie text-texto'
                    }`}
                  >
                    <span
                      className={`text-[11px] font-semibold ${
                        minha ? 'text-white/90' : 'text-primary'
                      }`}
                    >
                      {minha ? 'Você' : nomeExibido}
                    </span>

                    <p className="whitespace-pre-wrap break-words leading-snug">{msg.conteudo}</p>

                    <span
                      className={`self-end text-[10px] ${
                        minha ? 'text-white/80' : 'text-texto-suave'
                      }`}
                    >
                      {formatarHora(msg.criadoEm)}
                    </span>
                  </div>

                  {minha && <AvatarIniciais nome={nomeExibido} />}
                </div>
              );
            })}
          </div>
        )}
      </div>

      {erro && (
        <p role="alert" className="rounded-lg bg-erro/10 px-3 py-1.5 text-xs text-erro">
          {erro}
        </p>
      )}

      <form onSubmit={aoEnviar} className="flex items-center gap-2">
        <input
          type="text"
          value={texto}
          onChange={(e) => setTexto(e.target.value)}
          placeholder="Digite sua mensagem…"
          maxLength={1000}
          className="h-10 flex-1 rounded-lg border border-borda bg-fundo px-3 text-sm text-texto placeholder:text-texto-suave focus:border-primary focus:outline-2 focus:outline-offset-1 focus:outline-primary"
        />
        <Botao type="submit" variante="primario" tamanho="medio" carregando={enviando}>
          Enviar
        </Botao>
      </form>
    </Card>
  );
}
