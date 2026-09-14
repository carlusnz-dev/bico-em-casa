'use client';

import { useParams, useRouter } from 'next/navigation';
import { useEffect } from 'react';

import { FormAvaliacao } from '@/components/forms/FormAvaliacao';
import { Card } from '@/components/ui/Card';
import { useSessao } from '@/hooks/useSessao';

export default function AvaliarContratacaoPage() {
  const { id } = useParams<{ id: string }>();
  const { accessToken, status } = useSessao();
  const router = useRouter();

  useEffect(() => {
    if (status === 'anonimo') {
      router.push('/login');
    }
  }, [status, router]);

  if (!accessToken) {
    return <p className="text-texto-suave">Carregando…</p>;
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
