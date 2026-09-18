'use client';

import { useParams } from 'next/navigation';
import { useEffect, useState } from 'react';

import { listarAvaliacoesRecebidas } from '@/api/avaliacoes';
import type { Avaliacao } from '@/api/contratos/avaliacao';
import { ErroApi } from '@/api/erros';
import { Card } from '@/components/ui/Card';

function mensagemDeErro(causa: unknown, padrao: string): string {
  if (causa instanceof ErroApi) {
    return causa.semConexao ? 'Não foi possível falar com o servidor. Tente de novo.' : (causa.detail ?? padrao);
  }
  return padrao;
}

function formatarData(data: string): string {
  return new Date(data).toLocaleDateString('pt-BR', { day: '2-digit', month: 'long', year: 'numeric' });
}

export default function AvaliacoesDoPerfilPage() {
  const { id } = useParams<{ id: string }>();
  const [avaliacoes, setAvaliacoes] = useState<Avaliacao[] | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    listarAvaliacoesRecebidas(id)
      .then(setAvaliacoes)
      .catch((causa) => setErro(mensagemDeErro(causa, 'Não foi possível carregar as avaliações.')));
  }, [id]);

  if (erro) {
    return (
      <p role="alert" className="rounded-lg bg-erro/10 px-3 py-2 text-sm text-erro">
        {erro}
      </p>
    );
  }

  if (!avaliacoes) {
    return <p className="text-texto-suave">Carregando…</p>;
  }

  return (
    <div className="mx-auto flex max-w-xl flex-col gap-4">
      <h1 className="font-heading text-2xl font-bold">Avaliações recebidas</h1>

      {avaliacoes.length === 0 && <p className="text-texto-suave">Ainda não há avaliações.</p>}

      {avaliacoes.map((avaliacao) => (
        <Card key={avaliacao.id} className="flex flex-col gap-2">
          <div className="flex items-center justify-between">
            <span className="font-medium text-primary">{avaliacao.nota} / 5</span>
            <span className="text-sm text-texto-suave">{formatarData(avaliacao.criadoEm)}</span>
          </div>
          {avaliacao.comentario && (
            <p className="whitespace-pre-line text-texto-suave">{avaliacao.comentario}</p>
          )}
        </Card>
      ))}
    </div>
  );
}
