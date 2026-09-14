'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useState } from 'react';

import { contratarServico } from '@/api/contratacoes';
import { ErroApi } from '@/api/erros';
import { Botao, classesDoBotao } from '@/components/ui/Botao';
import { useSessao } from '@/hooks/useSessao';

function mensagemDeErro(causa: unknown, padrao: string): string {
  if (causa instanceof ErroApi) {
    return causa.semConexao ? 'Não foi possível falar com o servidor. Tente de novo.' : (causa.detail ?? padrao);
  }
  return padrao;
}

export function BotaoContratar({ servicoId }: { servicoId: string }) {
  const { accessToken, status } = useSessao();
  const router = useRouter();
  const [carregando, setCarregando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  if (status === 'anonimo') {
    return (
      <Link href="/login" className={classesDoBotao('primario', 'grande')}>
        Entre para contratar
      </Link>
    );
  }

  async function contratar() {
    if (!accessToken) return;

    setCarregando(true);
    setErro(null);

    try {
      await contratarServico({ servicoId }, accessToken);
      router.push('/contratacoes');
    } catch (causa) {
      setErro(mensagemDeErro(causa, 'Não foi possível contratar este serviço.'));
      setCarregando(false);
    }
  }

  return (
    <div className="flex flex-col gap-2">
      <Botao tamanho="grande" carregando={carregando} onClick={contratar}>
        Contratar serviço
      </Botao>
      {erro && (
        <p role="alert" className="text-sm text-erro">
          {erro}
        </p>
      )}
    </div>
  );
}
