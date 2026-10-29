'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';

import { listarMinhasContratacoes } from '@/api/contratacoes';
import type { Contratacao, StatusContratacao } from '@/api/contratos/contratacao';
import { ErroApi } from '@/api/erros';
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
    month: 'short',
    year: 'numeric',
  });
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

export default function MinhasContratacoesPage() {
  const { accessToken, status } = useSessao();
  const router = useRouter();
  const [contratacoes, setContratacoes] = useState<Contratacao[] | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    if (status === 'anonimo') {
      router.push('/login');
    }
  }, [status, router]);

  useEffect(() => {
    if (!accessToken) return;

    listarMinhasContratacoes(accessToken)
      .then((pagina) => setContratacoes(pagina.conteudo))
      .catch((causa) => setErro(mensagemDeErro(causa, 'Não foi possível carregar suas contratações.')));
  }, [accessToken]);

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
        <p className="text-texto-suave">Você ainda não contratou nenhum serviço.</p>
      ) : (
        <ul className="flex flex-col gap-3">
          {contratacoes.map((contratacao) => {
            const infoStatus = obterInfoStatus(contratacao.status);
            return (
              <li key={contratacao.id}>
                <Link href={`/contratacoes/${contratacao.id}`}>
                  <Card className="flex flex-col gap-2 transition-shadow hover:shadow-md sm:flex-row sm:items-center sm:justify-between">
                    <div className="flex flex-col gap-1">
                      <div className="flex items-center gap-2">
                        <h2 className="text-lg font-semibold">{contratacao.tituloServico}</h2>
                        <span className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${infoStatus.classes}`}>
                          {infoStatus.rotulo}
                        </span>
                        {contratacao.arquivada && (
                          <span className="rounded-full bg-borda/40 px-2 py-0.5 text-xs font-medium text-texto-suave">
                            Arquivada
                          </span>
                        )}
                      </div>
                      <p className="text-sm text-texto-suave">
                        Contratado em {formatarData(contratacao.criadoEm)}
                      </p>
                    </div>
                  </Card>
                </Link>
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
}
