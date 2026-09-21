'use client';

import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';

import type { DenunciaContraMim, StatusDenuncia } from '@/api/contratos/denuncia';
import { listarDenunciasContraMim } from '@/api/denuncias';
import { ErroApi } from '@/api/erros';
import { Card } from '@/components/ui/Card';
import { useSessao } from '@/hooks/useSessao';

const LABEL_STATUS: Record<StatusDenuncia, string> = {
  PENDENTE: 'Em análise',
  PROCEDENTE: 'Procedente',
  IMPROCEDENTE: 'Improcedente',
};

const CLASSE_STATUS: Record<StatusDenuncia, string> = {
  PENDENTE: 'bg-borda/40 text-texto-suave',
  PROCEDENTE: 'bg-erro/10 text-erro',
  IMPROCEDENTE: 'bg-sucesso/10 text-sucesso',
};

const LABEL_ALVO = {
  PERFIL: 'Sobre o seu perfil',
  SERVICO: 'Sobre um serviço seu',
  AVALIACAO: 'Sobre uma avaliação sua',
} as const;

function mensagemDeErro(causa: unknown, padrao: string): string {
  if (causa instanceof ErroApi) {
    return causa.semConexao
      ? 'Não foi possível falar com o servidor. Tente de novo.'
      : (causa.detail ?? padrao);
  }
  return padrao;
}

function formatarData(iso: string): string {
  return new Date(iso).toLocaleDateString('pt-BR');
}

export default function DenunciasContraMimPage() {
  const { accessToken, status } = useSessao();
  const router = useRouter();
  const [denuncias, setDenuncias] = useState<DenunciaContraMim[] | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    if (status === 'anonimo') {
      router.push('/login');
    }
  }, [status, router]);

  useEffect(() => {
    if (!accessToken) return;

    listarDenunciasContraMim(accessToken)
      .then((pagina) => setDenuncias(pagina.conteudo))
      .catch((causa) =>
        setErro(mensagemDeErro(causa, 'Não foi possível carregar suas denúncias.')),
      );
  }, [accessToken]);

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-1">
        <h1 className="font-heading text-3xl font-bold">Denúncias contra mim</h1>
        <p className="text-sm text-texto-suave">
          Reclamações abertas sobre você. A identidade de quem denunciou não é revelada.
        </p>
      </div>

      {erro && (
        <p role="alert" className="rounded-lg bg-erro/10 px-3 py-2 text-sm text-erro">
          {erro}
        </p>
      )}

      {denuncias === null ? (
        <p className="text-texto-suave">Carregando…</p>
      ) : denuncias.length === 0 ? (
        <p className="text-texto-suave">Nenhuma denúncia registrada contra você.</p>
      ) : (
        <ul className="flex flex-col gap-3">
          {denuncias.map((denuncia) => (
            <li key={denuncia.id}>
              <Card className="flex flex-col gap-3">
                <div className="flex flex-col gap-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <h2 className="text-lg font-semibold">{denuncia.motivo}</h2>
                    <span
                      className={`rounded-full px-2 py-0.5 text-xs font-medium ${CLASSE_STATUS[denuncia.status]}`}
                    >
                      {LABEL_STATUS[denuncia.status]}
                    </span>
                  </div>
                  <p className="text-sm text-texto-suave">
                    {LABEL_ALVO[denuncia.alvoTipo]} · aberta em {formatarData(denuncia.criadoEm)}
                  </p>
                </div>

                {denuncia.descricao && <p className="text-sm">{denuncia.descricao}</p>}

                {denuncia.parecer && (
                  <div className="flex flex-col gap-1 rounded-lg bg-borda/25 px-3 py-2">
                    <p className="text-xs font-medium text-texto-suave">
                      Decisão da moderação
                      {denuncia.analisadoEm && ` · ${formatarData(denuncia.analisadoEm)}`}
                    </p>
                    <p className="text-sm">{denuncia.parecer}</p>
                  </div>
                )}
              </Card>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
