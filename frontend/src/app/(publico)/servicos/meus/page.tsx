'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';

import { ativarServico, desativarServico, listarMeusServicos } from '@/api/servicos';
import type { Servico } from '@/api/contratos/servico';
import { ErroApi } from '@/api/erros';
import { formatarPreco } from '@/components/CardServico';
import { Botao, classesDoBotao } from '@/components/ui/Botao';
import { Card } from '@/components/ui/Card';
import { useSessao } from '@/hooks/useSessao';

function mensagemDeErro(causa: unknown, padrao: string): string {
  if (causa instanceof ErroApi) {
    return causa.semConexao ? 'Não foi possível falar com o servidor. Tente de novo.' : (causa.detail ?? padrao);
  }
  return padrao;
}

export default function MeusServicosPage() {
  const { accessToken, status } = useSessao();
  const router = useRouter();
  const [servicos, setServicos] = useState<Servico[] | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [idEmAcao, setIdEmAcao] = useState<string | null>(null);

  useEffect(() => {
    if (status === 'anonimo') {
      router.push('/login');
    }
  }, [status, router]);

  useEffect(() => {
    if (!accessToken) return;

    listarMeusServicos(accessToken)
      .then((pagina) => setServicos(pagina.conteudo))
      .catch((causa) => setErro(mensagemDeErro(causa, 'Não foi possível carregar seus serviços.')));
  }, [accessToken]);

  async function alternarStatus(servico: Servico) {
    if (!accessToken) return;

    setIdEmAcao(servico.id);
    setErro(null);

    try {
      const atualizado = servico.ativo
        ? await desativarServico(servico.id, accessToken)
        : await ativarServico(servico.id, accessToken);

      setServicos((atual) =>
        atual ? atual.map((item) => (item.id === atualizado.id ? atualizado : item)) : atual,
      );
    } catch (causa) {
      setErro(mensagemDeErro(causa, 'Não foi possível atualizar o serviço.'));
    } finally {
      setIdEmAcao(null);
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between gap-4">
        <h1 className="font-heading text-3xl font-bold">Meus serviços</h1>
        <Link href="/servicos/novo" className={classesDoBotao('primario')}>
          Anunciar serviço
        </Link>
      </div>

      {erro && (
        <p role="alert" className="rounded-lg bg-erro/10 px-3 py-2 text-sm text-erro">
          {erro}
        </p>
      )}

      {servicos === null ? (
        <p className="text-texto-suave">Carregando…</p>
      ) : servicos.length === 0 ? (
        <p className="text-texto-suave">Você ainda não anunciou nenhum serviço.</p>
      ) : (
        <ul className="flex flex-col gap-3">
          {servicos.map((servico) => (
            <li key={servico.id}>
              <Card className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div className="flex flex-col gap-1">
                  <div className="flex items-center gap-2">
                    <h2 className="text-lg font-semibold">{servico.titulo}</h2>
                    <span
                      className={
                        servico.ativo
                          ? 'rounded-full bg-primary/10 px-2 py-0.5 text-xs font-medium text-primary'
                          : 'rounded-full bg-borda/40 px-2 py-0.5 text-xs font-medium text-texto-suave'
                      }
                    >
                      {servico.ativo ? 'Ativo' : 'Inativo'}
                    </span>
                  </div>
                  <p className="text-sm text-texto-suave">
                    {formatarPreco(servico.precoPrevio, servico.unidadePreco)}
                  </p>
                </div>

                <div className="flex gap-2">
                  <Link
                    href={`/servicos/${servico.id}/editar`}
                    className={classesDoBotao('secundario')}
                  >
                    Editar
                  </Link>
                  <Botao
                    variante={servico.ativo ? 'fantasma' : 'primario'}
                    carregando={idEmAcao === servico.id}
                    onClick={() => alternarStatus(servico)}
                  >
                    {servico.ativo ? 'Desativar' : 'Ativar'}
                  </Botao>
                </div>
              </Card>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
