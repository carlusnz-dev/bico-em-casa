'use client';

import { useParams, useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';

import { buscarContratacaoPorId } from '@/api/contratacoes';
import type { Contratacao } from '@/api/contratos/contratacao';
import { ErroApi } from '@/api/erros';
import { FormDenuncia } from '@/components/forms/FormDenuncia';
import { Card } from '@/components/ui/Card';
import { useSessao } from '@/hooks/useSessao';

export default function DenunciarContratacaoPage() {
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
      .catch((causa) => {
        const mensagem =
          causa instanceof ErroApi
            ? (causa.detail ?? 'Não foi possível carregar esta contratação.')
            : 'Não foi possível carregar esta contratação.';
        setErro(mensagem);
      });
  }, [accessToken, id]);

  if (erro) {
    return (
      <p role="alert" className="rounded-lg bg-erro/10 px-3 py-2 text-sm text-erro">
        {erro}
      </p>
    );
  }

  if (!accessToken || !contratacao) {
    return <p className="text-texto-suave">Carregando…</p>;
  }

  return (
    <div className="mx-auto flex max-w-xl flex-col gap-6">
      <Card className="flex flex-col gap-4">
        <h1 className="font-heading text-2xl font-bold">Denunciar</h1>
        <FormDenuncia
          contratacaoId={contratacao.id}
          servicoId={contratacao.servicoId}
          profissionalId={contratacao.profissionalId}
          accessToken={accessToken}
        />
      </Card>
    </div>
  );
}
