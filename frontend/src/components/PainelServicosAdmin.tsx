'use client';

import { useEffect, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { z } from 'zod';
import {
  alterarSituacaoAdmin,
  moderarServico,
  renomearCategoria,
  removerCategoria,
  useServicosAdmin,
} from '@/api/admin-servicos';
import { type FiltrosAdmin, type ServicoAdmin } from '@/api/contratos/admin-servicos';
import { useCategorias } from '@/api/tags';
import { ErroApi } from '@/api/erros';
import { useSessao } from '@/hooks/useSessao';
import { Botao } from './ui/Botao';
import { Input } from './ui/Input';
import { Textarea } from './ui/Textarea';
import { formatarPreco } from './formatarPreco';

const iniciais: FiltrosAdmin = { busca: '', ativo: '', categoria: '' };
export function PainelServicosAdmin() {
  const { accessToken, perfil, status } = useSessao();
  const cache = useQueryClient();
  const [filtros, setFiltros] = useState(iniciais);
  const [pagina, setPagina] = useState(0);
  const [selecionado, setSelecionado] = useState<ServicoAdmin | null>(null);
  const [acao, setAcao] = useState<'editar' | 'desativar'>('editar');
  const [ocupado, setOcupado] = useState(false);
  const [mensagem, setMensagem] = useState('');
  const autorizado = perfil?.tipo === 'ADMIN';
  const listagem = useServicosAdmin(accessToken, autorizado, pagina, filtros);
  const categorias = useCategorias(autorizado);
  const dados = listagem.data;
  useEffect(() => {
    if (dados && pagina > 0 && pagina >= dados.totalPaginas)
      setPagina(Math.max(0, dados.totalPaginas - 1));
  }, [dados, pagina]);

  async function executar(operacao: () => Promise<unknown>) {
    setOcupado(true);
    setMensagem('');
    try {
      await operacao();
      setSelecionado(null);
      await Promise.all([
        cache.invalidateQueries({ queryKey: ['servicos-admin'] }),
        cache.invalidateQueries({ queryKey: ['tags'] }),
      ]);
      setMensagem('Alteração salva.');
    } catch (erro) {
      setMensagem(
        erro instanceof ErroApi
          ? (erro.detail ?? 'Não foi possível salvar.')
          : erro instanceof z.ZodError
            ? erro.issues.map((i) => i.message).join('; ')
            : 'Não foi possível salvar. Tente novamente.',
      );
    } finally {
      setOcupado(false);
    }
  }
  if (status === 'carregando' || (status === 'autenticado' && !perfil))
    return <p>Carregando sessão…</p>;
  if (!autorizado || !accessToken) return <p role="alert">Acesso restrito ao administrador.</p>;

  return (
    <div className="flex flex-col gap-6">
      <h1 className="font-heading text-3xl font-bold">Administração de serviços</h1>
      <p className="text-texto-suave">
        Revise o catálogo, corrija informações e modere ofertas inadequadas.
      </p>
      <form
        className="flex flex-wrap items-end gap-3"
        onSubmit={(e) => {
          e.preventDefault();
          const form = new FormData(e.currentTarget);
          setFiltros({
            busca: String(form.get('busca')),
            ativo: String(form.get('ativo')),
            categoria: String(form.get('categoria')),
          });
          setPagina(0);
          setSelecionado(null);
        }}
      >
        <label>
          Buscar título ou profissional
          <Input name="busca" placeholder="Nome, e-mail ou título" />
        </label>
        <label>
          Situação
          <select name="ativo" className="block rounded border p-2">
            <option value="">Todos</option>
            <option value="true">Somente ativos</option>
            <option value="false">Somente inativos</option>
          </select>
        </label>
        <label>
          Categoria
          <select name="categoria" className="block rounded border p-2">
            <option value="">Todas</option>
            {categorias.data?.map((t) => (
              <option key={t.id} value={t.id}>
                {t.nome}
              </option>
            ))}
          </select>
        </label>
        <Botao type="submit" disabled={ocupado}>
          Buscar / Aplicar filtro
        </Botao>
      </form>
      {mensagem && <p role="status">{mensagem}</p>}
      {listagem.isError && (
        <p role="alert">
          Não foi possível carregar os serviços.{' '}
          <Botao onClick={() => listagem.refetch()}>Tentar novamente</Botao>
        </p>
      )}
      {categorias.isError && (
        <p role="alert">
          Não foi possível carregar as categorias.{' '}
          <Botao onClick={() => categorias.refetch()}>Tentar novamente</Botao>
        </p>
      )}
      {listagem.isFetching ? (
        <p>Carregando serviços…</p>
      ) : (
        dados && (
          <>
            <p>{dados.totalElementos} serviço(s) encontrado(s)</p>
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <caption className="sr-only">Serviços cadastrados na plataforma</caption>
                <thead>
                  <tr>
                    {[
                      'Título',
                      'Profissional',
                      'Categorias',
                      'Preço prévio',
                      'Situação',
                      'Cadastro',
                      'Ações',
                    ].map((t) => (
                      <th className="p-3" scope="col" key={t}>
                        {t}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {dados.conteudo.map((item) => {
                    const s = item.servico;
                    return (
                      <tr key={s.id} className="border-borda border-t">
                        <td className="p-3">{s.titulo}</td>
                        <td className="p-3">{item.profissional}</td>
                        <td className="p-3">
                          {s.tagIds
                            .map((id) => categorias.data?.find((t) => t.id === id)?.nome ?? id)
                            .join(', ')}
                        </td>
                        <td className="p-3">{formatarPreco(s.precoPrevio, s.unidadePreco)}</td>
                        <td className="p-3">{s.ativo ? 'Ativo' : 'Inativo'}</td>
                        <td className="p-3">{new Date(s.criadoEm).toLocaleDateString('pt-BR')}</td>
                        <td className="flex gap-2 p-3">
                          <Botao
                            variante="secundario"
                            disabled={ocupado}
                            onClick={() => {
                              setSelecionado(item);
                              setAcao('editar');
                              setMensagem('');
                            }}
                          >
                            Editar
                          </Botao>
                          <Botao
                            disabled={ocupado}
                            onClick={() => {
                              if (s.ativo) {
                                setSelecionado(item);
                                setAcao('desativar');
                                setMensagem('');
                              } else if (window.confirm('Reativar este serviço?'))
                                void executar(() =>
                                  alterarSituacaoAdmin(s.id, true, '', accessToken),
                                );
                            }}
                          >
                            {s.ativo ? 'Desativar' : 'Reativar'}
                          </Botao>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
            {dados.conteudo.length === 0 && <p>Nenhum serviço encontrado para estes filtros.</p>}
            <nav aria-label="Paginação dos serviços" className="flex items-center gap-4">
              <Botao
                disabled={pagina === 0 || ocupado}
                onClick={() => {
                  setPagina((p) => p - 1);
                  setSelecionado(null);
                }}
              >
                Anterior
              </Botao>
              <span>
                Página {dados.totalPaginas === 0 ? 0 : pagina + 1} de {dados.totalPaginas}
              </span>
              <Botao
                disabled={pagina + 1 >= dados.totalPaginas || ocupado}
                onClick={() => {
                  setPagina((p) => p + 1);
                  setSelecionado(null);
                }}
              >
                Próxima
              </Botao>
            </nav>
          </>
        )
      )}
      {selecionado && (
        <section aria-label="Moderação do serviço" className="border-borda rounded-xl border p-5">
          <h2 className="text-xl font-semibold">
            {acao === 'editar' ? 'Editar' : 'Desativar'}: {selecionado.servico.titulo}
          </h2>
          <form
            key={`${selecionado.servico.id}-${acao}`}
            className="mt-4 flex flex-col gap-4"
            onSubmit={(e) => {
              e.preventDefault();
              const form = new FormData(e.currentTarget);
              const id = selecionado.servico.id;
              const motivo = String(form.get('motivo'));
              void executar(() =>
                acao === 'desativar'
                  ? alterarSituacaoAdmin(id, false, motivo, accessToken)
                  : moderarServico(
                      id,
                      {
                        titulo: String(form.get('titulo')),
                        descricao: String(form.get('descricao')),
                        tagIds: form.getAll('tagIds').map(String),
                        motivo,
                      },
                      accessToken,
                    ),
              );
            }}
          >
            {acao === 'editar' && (
              <>
                <label>
                  Título
                  <Input
                    name="titulo"
                    defaultValue={selecionado.servico.titulo}
                    required
                    maxLength={120}
                  />
                </label>
                <label>
                  Descrição
                  <Textarea
                    name="descricao"
                    defaultValue={selecionado.servico.descricao}
                    required
                    maxLength={1000}
                  />
                </label>
                <fieldset>
                  <legend>Categorias</legend>
                  {categorias.data?.map((t) => (
                    <label key={t.id} className="mr-4 inline-flex gap-2">
                      <input
                        type="checkbox"
                        name="tagIds"
                        value={t.id}
                        defaultChecked={selecionado.servico.tagIds.includes(t.id)}
                      />
                      {t.nome}
                    </label>
                  ))}
                </fieldset>
              </>
            )}
            <label>
              Motivo {acao === 'desativar' && '(mínimo de 10 caracteres)'}
              <Textarea
                name="motivo"
                required
                minLength={acao === 'desativar' ? 10 : 1}
                maxLength={1000}
              />
            </label>
            <p>Esta ação será registrada na auditoria.</p>
            <div className="flex gap-3">
              <Botao type="submit" carregando={ocupado}>
                Confirmar {acao === 'editar' ? 'correção' : 'desativação'}
              </Botao>
              <Botao disabled={ocupado} variante="secundario" onClick={() => setSelecionado(null)}>
                Cancelar
              </Botao>
            </div>
          </form>
        </section>
      )}
      <section aria-labelledby="categorias-titulo" className="flex flex-col gap-3">
        <h2 id="categorias-titulo" className="text-xl font-semibold">
          Categorias predefinidas
        </h2>
        <p>Renomear preserva os vínculos. Apenas categorias sem serviços podem ser removidas.</p>
        {categorias.data?.map((t) => (
          <form
            key={`${t.id}-${t.nome}`}
            className="flex flex-wrap items-end gap-3"
            onSubmit={(e) => {
              e.preventDefault();
              const nome = String(new FormData(e.currentTarget).get('nome'));
              void executar(() => renomearCategoria(t.id, nome, accessToken));
            }}
          >
            <label>
              Nome da categoria
              <Input name="nome" defaultValue={t.nome} required maxLength={50} />
            </label>
            <Botao type="submit" disabled={ocupado}>
              Renomear
            </Botao>
            <Botao
              variante="secundario"
              disabled={ocupado}
              onClick={() => {
                if (window.confirm(`Remover a categoria ${t.nome}?`))
                  void executar(() => removerCategoria(t.id, accessToken));
              }}
            >
              Remover {t.nome}
            </Botao>
          </form>
        ))}
      </section>
    </div>
  );
}
