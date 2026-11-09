'use client';

import { useQuery } from '@tanstack/react-query';
import Link from 'next/link';

import type { Tag } from '@/api/contratos/tag';
import { ErroApi } from '@/api/erros';
import { buscarMeuServico } from '@/api/servicos';
import { useSessao } from '@/hooks/useSessao';
import { Botao } from '../ui/Botao';
import { Card } from '../ui/Card';
import { FormServico } from './FormServico';

export function EditarServico({ id, tags }: { id: string; tags: Tag[] }) {
  const { accessToken, status } = useSessao();
  const consulta = useQuery({
    queryKey: ['meu-servico', accessToken, id],
    queryFn: () => buscarMeuServico(id, accessToken!),
    enabled: Boolean(accessToken),
    retry: false,
  });

  if (status === 'anonimo') return <Link href="/login">Entre para editar seu serviço</Link>;
  if (consulta.isError) {
    const causa = consulta.error;
    const detalhe = causa instanceof ErroApi ? causa.detail : undefined;
    return (
      <div role="alert">
        {detalhe ?? 'Não foi possível carregar seu serviço.'}{' '}
        <Botao onClick={() => consulta.refetch()}>Tentar novamente</Botao>
      </div>
    );
  }
  const servico = consulta.data;
  if (!servico) return <p>Carregando serviço…</p>;

  const reprovado = servico.statusRevisao === 'REPROVADO';
  return (
    <div className="mx-auto flex max-w-xl flex-col gap-6">
      <header className="flex flex-col gap-2">
        <h1 className="font-heading text-3xl font-bold">
          {reprovado ? 'Corrigir e reenviar serviço' : 'Editar serviço'}
        </h1>
        <p className="text-texto-suave">Atualize os dados de &quot;{servico.titulo}&quot;.</p>
        {reprovado && <p role="status">Motivo da reprovação: {servico.motivoReprovacao}</p>}
      </header>
      <Card>
        <FormServico key={servico.id} tags={tags} servico={servico} />
      </Card>
    </div>
  );
}
