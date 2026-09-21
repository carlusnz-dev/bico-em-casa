'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';

import { listarMinhasContratacoes } from '@/api/contratacoes';
import type { Contratacao } from '@/api/contratos/contratacao';
import type { Perfil, PerfilTipo } from '@/api/contratos/perfil';
import type { Servico } from '@/api/contratos/servico';
import { ErroApi } from '@/api/erros';
import { buscarMeuPerfil } from '@/api/perfil';
import { listarMeusServicos } from '@/api/servicos';
import { AvatarIniciais } from '@/components/AvatarIniciais';
import { formatarPreco } from '@/components/formatarPreco';
import { Card } from '@/components/ui/Card';
import { useSessao } from '@/hooks/useSessao';

const LABEL_TIPO: Record<PerfilTipo, string> = {
  CLIENTE: 'Cliente',
  PROFISSIONAL: 'Profissional',
  ADMIN: 'Administrador',
};

function mensagemDeErro(causa: unknown, padrao: string): string {
  if (causa instanceof ErroApi) {
    return causa.semConexao ? 'Não foi possível falar com o servidor. Tente de novo.' : (causa.detail ?? padrao);
  }
  return padrao;
}

function formatarData(data: string): string {
  return new Date(data).toLocaleDateString('pt-BR', { day: '2-digit', month: 'long', year: 'numeric' });
}

export default function PerfilPage() {
  const { accessToken, usuario, status } = useSessao();
  const router = useRouter();
  const [perfil, setPerfil] = useState<Perfil | null>(null);
  const [servicos, setServicos] = useState<Servico[] | null>(null);
  const [contratacoes, setContratacoes] = useState<Contratacao[] | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    if (status === 'anonimo') {
      router.push('/login');
    }
  }, [status, router]);

  useEffect(() => {
    if (!accessToken) return;

    buscarMeuPerfil(accessToken)
      .then(setPerfil)
      .catch((causa) => setErro(mensagemDeErro(causa, 'Não foi possível carregar seu perfil.')));
  }, [accessToken]);

  useEffect(() => {
    if (!accessToken || !perfil) return;

    if (perfil.tipo === 'PROFISSIONAL') {
      listarMeusServicos(accessToken, 0, 5)
        .then((pagina) => setServicos(pagina.conteudo))
        .catch(() => setServicos([]));
    }

    if (perfil.tipo === 'CLIENTE') {
      listarMinhasContratacoes(accessToken, 0, 5)
        .then((pagina) => setContratacoes(pagina.conteudo))
        .catch(() => setContratacoes([]));
    }
  }, [accessToken, perfil]);

  return (
    <div className="mx-auto flex max-w-2xl flex-col gap-6">
      <h1 className="font-heading text-3xl font-bold">Meu perfil</h1>

      {erro && (
        <p role="alert" className="rounded-lg bg-erro/10 px-3 py-2 text-sm text-erro">
          {erro}
        </p>
      )}

      {!perfil ? (
        !erro && <p className="text-texto-suave">Carregando…</p>
      ) : (
        <>
          <Card className="flex flex-col gap-4">
            <div className="flex items-center gap-4">
              <AvatarIniciais nome={usuario?.nome ?? perfil.nomeExibicao} tamanho="grande" />
              <div className="flex flex-col gap-1">
                <h2 className="text-xl font-semibold">{usuario?.nome ?? perfil.nomeExibicao}</h2>
                <span className="w-fit rounded-full bg-primary/10 px-2 py-0.5 text-xs font-medium text-primary">
                  {LABEL_TIPO[perfil.tipo]}
                </span>
              </div>
            </div>

            <p className="text-sm text-texto-suave">Na plataforma desde {formatarData(perfil.criadoEm)}</p>

            {perfil.bio && <p className="whitespace-pre-line text-texto-suave">{perfil.bio}</p>}
          </Card>

          {perfil.tipo === 'PROFISSIONAL' && (
            <Card className="flex flex-col gap-3">
              <div className="flex items-center justify-between">
                <h3 className="text-lg font-semibold">Serviços</h3>
                <Link href="/servicos/meus" className="text-sm font-medium text-primary hover:underline">
                  Ver todos
                </Link>
              </div>
              {servicos === null ? (
                <p className="text-sm text-texto-suave">Carregando…</p>
              ) : servicos.length === 0 ? (
                <p className="text-sm text-texto-suave">Nenhum serviço anunciado ainda.</p>
              ) : (
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
              )}
            </Card>
          )}

          {perfil.tipo === 'CLIENTE' && (
            <Card className="flex flex-col gap-3">
              <div className="flex items-center justify-between">
                <h3 className="text-lg font-semibold">Contratações</h3>
                <Link href="/contratacoes" className="text-sm font-medium text-primary hover:underline">
                  Ver todas
                </Link>
              </div>
              {contratacoes === null ? (
                <p className="text-sm text-texto-suave">Carregando…</p>
              ) : contratacoes.length === 0 ? (
                <p className="text-sm text-texto-suave">Você ainda não contratou nenhum serviço.</p>
              ) : (
                <ul className="flex flex-col gap-2">
                  {contratacoes.map((contratacao) => (
                    <li key={contratacao.id} className="flex items-center justify-between text-sm">
                      <span>{contratacao.tituloServico}</span>
                      <span className="text-texto-suave">
                        {contratacao.status === 'ATIVA' ? 'Ativa' : 'Arquivada'}
                      </span>
                    </li>
                  ))}
                </ul>
              )}
            </Card>
          )}
        </>
      )}
    </div>
  );
}
