'use client';

import { useRef, useState } from 'react';

import { enviarFotoCapa } from '@/api/portfolio';
import type { Portfolio } from '@/api/contratos/portfolio';
import { ErroApi } from '@/api/erros';
import { Botao } from '@/components/ui/Botao';
import { useSessao } from '@/hooks/useSessao';

export function UploadFotoCapa({ portfolio }: { portfolio: Portfolio }) {
  const { accessToken } = useSessao();
  const inputRef = useRef<HTMLInputElement>(null);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [fotoCapaUrl, setFotoCapaUrl] = useState(portfolio.fotoCapaUrl);

  async function aoSelecionarArquivo(evento: React.ChangeEvent<HTMLInputElement>) {
    const arquivo = evento.target.files?.[0];
    evento.target.value = '';
    if (!arquivo || !accessToken) return;

    setEnviando(true);
    setErro(null);

    try {
      const atualizado = await enviarFotoCapa(portfolio.id, arquivo, accessToken);
      setFotoCapaUrl(atualizado.fotoCapaUrl);
    } catch (causa) {
      setErro(
        causa instanceof ErroApi
          ? (causa.detail ?? 'Não foi possível enviar a foto de capa.')
          : 'Não foi possível enviar a foto de capa.',
      );
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="flex flex-col gap-3">
      <div className="aspect-video w-full overflow-hidden rounded-xl border border-borda bg-borda/30">
        {fotoCapaUrl && (
          // eslint-disable-next-line @next/next/no-img-element
          <img src={fotoCapaUrl} alt={portfolio.titulo} className="h-full w-full object-cover" />
        )}
      </div>

      <input
        ref={inputRef}
        type="file"
        accept="image/*"
        className="hidden"
        onChange={aoSelecionarArquivo}
      />

      <Botao
        variante="secundario"
        carregando={enviando}
        onClick={() => inputRef.current?.click()}
      >
        {fotoCapaUrl ? 'Trocar foto de capa' : 'Enviar foto de capa'}
      </Botao>

      {erro && (
        <p role="alert" className="rounded-lg bg-erro/10 px-3 py-2 text-sm text-erro">
          {erro}
        </p>
      )}
    </div>
  );
}
