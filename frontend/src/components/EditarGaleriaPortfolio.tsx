'use client';

import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { z } from 'zod';

import { ErroApi } from '@/api/erros';
import { enviarMidiaPortfolio, listarMidiasPortfolio, removerMidiaPortfolio } from '@/api/portfolio';
import { GaleriaPortfolio } from './GaleriaPortfolio';
import { Botao } from './ui/Botao';
import { Card } from './ui/Card';

export function EditarGaleriaPortfolio({ id, token }: { id: number; token: string }) {
  const cache = useQueryClient();
  const [ocupado, setOcupado] = useState(false);
  const [erro, setErro] = useState('');
  const queryKey = ['portfolio-midias', id];
  const galeria = useQuery({ queryKey, queryFn: () => listarMidiasPortfolio(id) });

  async function executar(operacao: () => Promise<unknown>) {
    setOcupado(true);
    setErro('');
    try {
      await operacao();
      await cache.invalidateQueries({ queryKey });
    } catch (causa) {
      setErro(causa instanceof z.ZodError
        ? (causa.issues[0]?.message ?? 'Arquivo inválido.')
        : causa instanceof ErroApi
          ? (causa.detail ?? 'Não foi possível salvar a galeria.')
          : causa instanceof Error ? causa.message : 'Não foi possível salvar a galeria.');
    } finally {
      setOcupado(false);
    }
  }

  return (
    <Card className="flex flex-col gap-4">
      <h2 className="text-xl font-semibold">Fotos e vídeos dos trabalhos</h2>
      <p className="text-sm text-texto-suave">Imagens JPEG, PNG ou WebP até 5 MB. Vídeos MP4 ou WebM até 25 MB.</p>
      <input
        type="file"
        aria-label="Adicionar foto ou vídeo à galeria"
        accept="image/jpeg,image/png,image/webp,video/mp4,video/webm"
        disabled={ocupado}
        onChange={(evento) => {
          const arquivo = evento.currentTarget.files?.[0];
          evento.currentTarget.value = '';
          if (arquivo) void executar(() => enviarMidiaPortfolio(id, arquivo, token));
        }}
      />
      {ocupado && <p role="status">Atualizando galeria…</p>}
      {erro && <p role="alert" className="text-erro">{erro}</p>}
      {galeria.isLoading && <p>Carregando galeria…</p>}
      {galeria.isError && (
        <div role="alert">
          Não foi possível carregar a galeria. <Botao onClick={() => galeria.refetch()}>Tentar novamente</Botao>
        </div>
      )}
      {galeria.data && (galeria.data.length > 0 ? (
        <GaleriaPortfolio
          midias={galeria.data}
          renderAcao={(midia) => (
            <Botao
              variante="secundario"
              disabled={ocupado}
              onClick={() => {
                if (window.confirm('Remover este arquivo da galeria?')) {
                  void executar(() => removerMidiaPortfolio(id, midia.id, token));
                }
              }}
            >
              Remover arquivo
            </Botao>
          )}
        />
      ) : <p>Você ainda não adicionou fotos ou vídeos.</p>)}
    </Card>
  );
}
