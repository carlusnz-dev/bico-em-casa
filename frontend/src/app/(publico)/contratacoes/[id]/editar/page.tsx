'use client';

import { useParams, useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';

import { buscarContratacaoPorId } from '@/api/contratacoes';
import type { Contratacao } from '@/api/contratos/contratacao';
import { ErroApi } from '@/api/erros';
import { FormContratacao } from '@/components/forms/FormContratacao';
import { Card } from '@/components/ui/Card';
import { useSessao } from '@/hooks/useSessao';

function mensagemDeErro(causa: unknown, padrao: string): string {
  if (causa instanceof ErroApi) {
    return causa.semConexao ? 'Não foi possível falar com o servidor. Tente de novo.' : (causa.detail ?? padrao);
  }
  return padrao;
}

export default function EditarContratacaoPage() {
  const { id } = useParams<{ id: string }>();
  const { accessToken, status } = useSessao();
  const router = useRouter();
  const [contratacao, setContratacao] = useState<Contratacao | null>(null);
  const [erro, setErro] = useState<string | null>(null);

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

  return (
    <div className="mx-auto flex max-w-xl flex-col gap-6">
      <header className="flex flex-col gap-2">
        <h1 className="font-heading text-3xl font-bold">Editar contratação</h1>
        {contratacao && (
          <p className="text-texto-suave">Atualize a observação de &quot;{contratacao.tituloServico}&quot;.</p>
        )}
      </header>

      {erro && (
        <p role="alert" className="rounded-lg bg-erro/10 px-3 py-2 text-sm text-erro">
          {erro}
        </p>
      )}

      {contratacao ? (
        <Card>
          <FormContratacao contratacao={contratacao} />
        </Card>
      ) : (
        !erro && <p className="text-texto-suave">Carregando…</p>
      )}
    </div>
  );
}
