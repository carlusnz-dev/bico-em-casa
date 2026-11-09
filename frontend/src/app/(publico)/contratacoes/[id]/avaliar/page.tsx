'use client';

import { useQuery } from '@tanstack/react-query';
import { useParams, useRouter } from 'next/navigation';
import { useEffect } from 'react';

import { buscarContratacaoPorId } from '@/api/contratacoes';
import { ErroApi } from '@/api/erros';
import { FormAvaliacao } from '@/components/forms/FormAvaliacao';
import { Card } from '@/components/ui/Card';
import { useSessao } from '@/hooks/useSessao';

export default function AvaliarContratacaoPage() {
  const { id } = useParams<{ id: string }>();
  const { accessToken, perfil, status } = useSessao();
  const router = useRouter();
  const consulta = useQuery({
    queryKey: ['contratacao-para-avaliar', accessToken, id],
    queryFn: () => buscarContratacaoPorId(id, accessToken!),
    enabled: Boolean(accessToken),
    retry: false,
  });

  useEffect(() => {
    if (status === 'anonimo') {
      router.push('/login');
    }
  }, [status, router]);

  if (!accessToken || !perfil || consulta.isLoading) {
    return <p className="text-texto-suave">Carregando…</p>;
  }
  if (consulta.isError) {
    const detalhe = consulta.error instanceof ErroApi ? consulta.error.detail : undefined;
    return <p role="alert">{detalhe ?? 'Não foi possível carregar a contratação.'}</p>;
  }
  if (consulta.data?.status !== 'CONCLUIDO' || consulta.data.clienteId !== perfil.id) {
    return <p role="alert">A avaliação fica disponível para o cliente após a conclusão da contratação.</p>;
  }

  return (
    <div className="mx-auto flex max-w-xl flex-col gap-6">
      <Card className="flex flex-col gap-4">
        <h1 className="font-heading text-2xl font-bold">Avaliar contratação</h1>
        <FormAvaliacao contratacaoId={id} accessToken={accessToken} />
      </Card>
    </div>
  );
}
