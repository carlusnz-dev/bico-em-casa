'use client';

import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';

import { alterarAvaliacao, deletarAvaliacao, listarAvaliacoesFeitas } from '@/api/avaliacoes';
import type { Avaliacao, AvaliacaoRequest } from '@/api/contratos/avaliacao';
import { avaliacaoRequestSchema } from '@/api/contratos/avaliacao';
import { ErroApi } from '@/api/erros';
import { buscarMeuPerfil } from '@/api/perfil';
import { Botao } from '@/components/ui/Botao';
import { Card } from '@/components/ui/Card';
import { Label } from '@/components/ui/Label';
import { Textarea } from '@/components/ui/Textarea';
import { useSessao } from '@/hooks/useSessao';

const NOTAS = [1, 2, 3, 4, 5] as const;

function mensagemDeErro(causa: unknown, padrao: string): string {
  if (causa instanceof ErroApi) {
    return causa.semConexao ? 'Não foi possível falar com o servidor. Tente de novo.' : (causa.detail ?? padrao);
  }
  return padrao;
}

function formatarData(data: string): string {
  return new Date(data).toLocaleDateString('pt-BR', { day: '2-digit', month: 'long', year: 'numeric' });
}

export default function MinhasAvaliacoesPage() {
  const { accessToken, status } = useSessao();
  const router = useRouter();

  const [avaliacoes, setAvaliacoes] = useState<Avaliacao[] | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  const [editandoId, setEditandoId] = useState<string | null>(null);
  const [nota, setNota] = useState(0);
  const [comentario, setComentario] = useState('');
  const [erroEdicao, setErroEdicao] = useState<string | null>(null);
  const [salvando, setSalvando] = useState(false);

  useEffect(() => {
    if (status === 'anonimo') {
      router.push('/login');
    }
  }, [status, router]);

  useEffect(() => {
    if (!accessToken) return;

    buscarMeuPerfil(accessToken)
      .then((perfil) => listarAvaliacoesFeitas(perfil.id))
      .then(setAvaliacoes)
      .catch((causa) => setErro(mensagemDeErro(causa, 'Não foi possível carregar suas avaliações.')));
  }, [accessToken]);

  function handleEditar(avaliacao: Avaliacao) {
    setEditandoId(avaliacao.id);
    setNota(avaliacao.nota);
    setComentario(avaliacao.comentario ?? '');
    setErroEdicao(null);
  }

  function handleCancelar() {
    setEditandoId(null);
    setErroEdicao(null);
  }

  async function handleSalvar(id: string) {
    if (!accessToken) return;

    const dados: AvaliacaoRequest = { nota, comentario: comentario.trim() === '' ? undefined : comentario };
    const validacao = avaliacaoRequestSchema.safeParse(dados);
    if (!validacao.success) {
      setErroEdicao(validacao.error.issues[0]?.message ?? 'Dados inválidos.');
      return;
    }

    setSalvando(true);
    setErroEdicao(null);

    try {
      const atualizada = await alterarAvaliacao(id, validacao.data, accessToken);
      setAvaliacoes((atual) => atual?.map((av) => (av.id === id ? atualizada : av)) ?? atual);
      setEditandoId(null);
    } catch (causa) {
      setErroEdicao(mensagemDeErro(causa, 'Não foi possível salvar a alteração.'));
    } finally {
      setSalvando(false);
    }
  }

  async function handleDeletar(id: string) {
    if (!accessToken) return;
    if (!window.confirm('Tem certeza que deseja excluir esta avaliação?')) return;

    try {
      await deletarAvaliacao(id, accessToken);
      setAvaliacoes((atual) => atual?.filter((av) => av.id !== id) ?? atual);
    } catch (causa) {
      setErro(mensagemDeErro(causa, 'Não foi possível excluir a avaliação.'));
    }
  }

  if (!accessToken) {
    return <p className="text-texto-suave">Carregando…</p>;
  }

  return (
    <div className="mx-auto flex max-w-xl flex-col gap-4">
      <h1 className="font-heading text-2xl font-bold">Minhas avaliações</h1>

      {erro && (
        <p role="alert" className="rounded-lg bg-erro/10 px-3 py-2 text-sm text-erro">
          {erro}
        </p>
      )}

      {!avaliacoes ? (
        !erro && <p className="text-texto-suave">Carregando…</p>
      ) : avaliacoes.length === 0 ? (
        <Card className="text-center text-texto-suave">Você ainda não avaliou nenhuma contratação.</Card>
      ) : (
        avaliacoes.map((avaliacao) => (
          <Card key={avaliacao.id} className="flex flex-col gap-3">
            {editandoId === avaliacao.id ? (
              <>
                <div className="flex flex-col gap-1.5">
                  <Label htmlFor={`nota-${avaliacao.id}`}>Nota</Label>
                  <div className="flex gap-2" role="radiogroup" aria-label="Nota">
                    {NOTAS.map((valor) => (
                      <button
                        key={valor}
                        type="button"
                        aria-pressed={nota === valor}
                        onClick={() => setNota(valor)}
                        className={
                          nota === valor
                            ? 'flex h-10 w-10 items-center justify-center rounded-lg bg-primary font-medium text-white'
                            : 'flex h-10 w-10 items-center justify-center rounded-lg border border-borda text-texto-suave hover:bg-borda/40'
                        }
                      >
                        {valor}
                      </button>
                    ))}
                  </div>
                </div>

                <div className="flex flex-col gap-1.5">
                  <Label htmlFor={`comentario-${avaliacao.id}`}>Comentário (opcional)</Label>
                  <Textarea
                    id={`comentario-${avaliacao.id}`}
                    rows={4}
                    value={comentario}
                    onChange={(e) => setComentario(e.target.value)}
                    placeholder="Deixe sua opinião"
                  />
                </div>

                {erroEdicao && (
                  <p role="alert" className="rounded-lg bg-erro/10 px-3 py-2 text-sm text-erro">
                    {erroEdicao}
                  </p>
                )}

                <div className="flex gap-2">
                  <Botao tamanho="medio" carregando={salvando} onClick={() => handleSalvar(avaliacao.id)}>
                    Salvar alteração
                  </Botao>
                  <Botao variante="secundario" tamanho="medio" onClick={handleCancelar} disabled={salvando}>
                    Cancelar
                  </Botao>
                </div>
              </>
            ) : (
              <>
                <div className="flex items-center justify-between">
                  <span className="font-medium text-primary">{avaliacao.nota} / 5</span>
                  <span className="text-sm text-texto-suave">{formatarData(avaliacao.criadoEm)}</span>
                </div>

                {avaliacao.comentario && (
                  <p className="whitespace-pre-line text-texto-suave">{avaliacao.comentario}</p>
                )}

                <div className="flex gap-2">
                  <Botao variante="secundario" tamanho="medio" onClick={() => handleEditar(avaliacao)}>
                    Alterar avaliação
                  </Botao>
                  <Botao
                    variante="fantasma"
                    tamanho="medio"
                    className="text-erro hover:bg-erro/10"
                    onClick={() => handleDeletar(avaliacao.id)}
                  >
                    Excluir
                  </Botao>
                </div>
              </>
            )}
          </Card>
        ))
      )}
    </div>
  );
}
