'use client';

import { useQuery } from '@tanstack/react-query';
import clsx from 'clsx';
import Link from 'next/link';
import { useAvisosModeracao } from '@/api/admin-servicos';
import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';

import { ativarServico, buscarMeuOrcamentoMedio, desativarServico, listarMeusServicos, deletarServico } from '@/api/servicos';
import type { Servico } from '@/api/contratos/servico';
import { ErroApi } from '@/api/erros';
import { formatarPreco } from '@/components/formatarPreco';
import { ListaValoresMedios } from '@/components/ListaValoresMedios';
import { SimuladorOrcamento } from '@/components/SimuladorOrcamento';
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
  const { accessToken, perfil, status } = useSessao();
  const router = useRouter();
  const avisos = useAvisosModeracao(accessToken);
  const [servicos, setServicos] = useState<Servico[] | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [idEmAcao, setIdEmAcao] = useState<string | null>(null);
  const orcamento = useQuery({
    queryKey: ['meu-orcamento-medio', accessToken],
    queryFn: () => buscarMeuOrcamentoMedio(accessToken!),
    enabled: Boolean(accessToken) && perfil?.tipo === 'PROFISSIONAL',
  });

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
      void orcamento.refetch();
    } catch (causa) {
      setErro(mensagemDeErro(causa, 'Não foi possível atualizar o serviço.'));
    } finally {
      setIdEmAcao(null);
    }
  }
  async function handleDeletar(id: string) {
    if (!accessToken) return;
    if (!window.confirm("Tem certeza que deseja excluir este serviço?")) return;

    try {
      await deletarServico(id, accessToken);
      setServicos((atual) => atual?.filter((sv) => sv.id !== id) ?? atual);
      void orcamento.refetch();
    } catch (causa) {
      setErro(mensagemDeErro(causa, 'Não foi possível excluir o serviço.'));
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

      {perfil?.tipo === 'PROFISSIONAL' && (
        <Card className="flex flex-col gap-2">
          <h2 className="text-lg font-semibold">Meu orçamento médio</h2>
          <p className="text-sm text-texto-suave">
            Estimativa com base no preço de referência dos seus serviços ativos, por unidade.
          </p>
          {orcamento.isLoading && <p>Calculando orçamento médio…</p>}
          {orcamento.isError && <p role="alert">Não foi possível calcular seu orçamento médio.</p>}
          {orcamento.data && (orcamento.data.length > 0
            ? <>
                <ListaValoresMedios valores={orcamento.data} />
                <SimuladorOrcamento valores={orcamento.data} />
              </>
            : <p>Cadastre ou ative um serviço para ver sua estimativa.</p>)}
        </Card>
      )}

      {avisos.data && avisos.data.length > 0 && <section aria-label="Avisos da moderação">
        <h2 className="text-lg font-semibold">Avisos da moderação</h2>
        <ul>{avisos.data.map((aviso) => (
          <li key={aviso.id}>
            <Link href={`/servicos/${aviso.alvoId}/editar`} className="text-primary hover:underline">
              {aviso.titulo ?? 'Aviso da moderação'}
            </Link> — {aviso.mensagem}
          </li>
        ))}</ul>
      </section>}
      {erro && (
        <p role="alert" className="bg-erro/10 text-erro rounded-lg px-3 py-2 text-sm">
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
              <Card className={clsx(
                'flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between transition-colors',
                servico.ativo ? 'bg-green-300! border-green-300!' : 'bg-white! border-borda!',
              )}>
                <div className="flex flex-col gap-1">
                  <div className="flex items-center gap-2">
                    <h2 className="text-lg font-semibold">{servico.titulo}</h2>
                    <span
                      className={
                        servico.ativo
                          ? 'bg-primary/10 text-primary rounded-full px-2 py-0.5 text-xs font-medium'
                          : 'bg-borda/40 text-texto-suave rounded-full px-2 py-0.5 text-xs font-medium'
                      }
                    >
                      {servico.motivoModeracao ? 'Desativado pela moderação' : servico.ativo ? 'Ativo' : 'Inativo'}
                    </span>
                  </div>
                  <p className="text-texto-suave text-sm">
                    {formatarPreco(servico.precoPrevio, servico.unidadePreco)}
                    {servico.motivoModeracao && <span className="block">Motivo: {servico.motivoModeracao}</span>}
                    {servico.statusRevisao === 'PENDENTE' && <span className="block">Aguardando revisão</span>}
                    {servico.statusRevisao === 'REPROVADO' && (
                      <span className="block">Reprovado: {servico.motivoReprovacao}</span>
                    )}
                  </p>
                </div>

                <div className="flex gap-2">
                  <Link
                    href={`/servicos/${servico.id}/editar`}
                    className={classesDoBotao('secundario')}
                  >
                    {servico.statusRevisao === 'REPROVADO' ? 'Corrigir e reenviar' : 'Editar'}
                  </Link>
                  <Botao
                    variante={servico.ativo ? 'fantasma' : 'primario'}
                    disabled={Boolean(servico.motivoModeracao)
                      || servico.statusRevisao === 'REPROVADO'
                      || (!servico.ativo && servico.statusRevisao === 'PENDENTE')}
                    carregando={idEmAcao === servico.id}
                    onClick={() => alternarStatus(servico)}
                  >
                    {servico.ativo ? 'Desativar' : 'Ativar'}
                  </Botao>
                  <Botao
                    variante="fantasma"
                    tamanho="medio"
                    className="text-erro hover:bg-erro/10"
                    onClick={() => handleDeletar(servico.id)}
                  >
                    Excluir
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
