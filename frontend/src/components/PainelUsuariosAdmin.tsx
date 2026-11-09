'use client';

import { useQueryClient } from '@tanstack/react-query';
import { useEffect, useState } from 'react';
import { z } from 'zod';

import { alterarSituacaoConta, cadastrarAdministrador, useUsuariosAdmin } from '@/api/admin-usuarios';
import type { ContaAdmin } from '@/api/contratos/admin-usuarios';
import { ErroApi } from '@/api/erros';
import { useSessao } from '@/hooks/useSessao';

import { DataFormatada } from './DataFormatada';
import { Botao } from './ui/Botao';
import { Card } from './ui/Card';
import { Input } from './ui/Input';
import { Select } from './ui/Select';
import { Textarea } from './ui/Textarea';

const FILTROS_INICIAIS = { busca: '', ativo: '' };
const PAPEIS = { CLIENTE: 'Cliente', PROFISSIONAL: 'Profissional', ADMIN: 'Administrador' };

function mensagemDeErro(erro: unknown) {
  if (erro instanceof ErroApi) return erro.detail ?? 'Não foi possível salvar.';
  if (erro instanceof z.ZodError) return erro.issues.map((issue) => issue.message).join('. ');
  return 'Não foi possível salvar. Tente novamente.';
}

export function PainelUsuariosAdmin() {
  const { accessToken, perfil, status, usuario } = useSessao();
  const cache = useQueryClient();
  const [filtros, setFiltros] = useState(FILTROS_INICIAIS);
  const [pagina, setPagina] = useState(0);
  const [selecionado, setSelecionado] = useState<ContaAdmin | null>(null);
  const [ocupado, setOcupado] = useState(false);
  const [mensagem, setMensagem] = useState('');
  const [erro, setErro] = useState('');
  const autorizado = perfil?.tipo === 'ADMIN';
  const consulta = useUsuariosAdmin(accessToken, autorizado, pagina, filtros);
  const dados = consulta.data;

  useEffect(() => {
    if (dados && pagina > 0 && pagina >= dados.totalPaginas) {
      setPagina(Math.max(0, dados.totalPaginas - 1));
    }
  }, [dados, pagina]);

  async function salvar(operacao: () => Promise<unknown>, sucesso: string) {
    setOcupado(true);
    setErro('');
    setMensagem('');
    try {
      await operacao();
      setSelecionado(null);
      setMensagem(sucesso);
      await cache.invalidateQueries({ queryKey: ['usuarios-admin'] });
    } catch (causa) {
      setErro(mensagemDeErro(causa));
    } finally {
      setOcupado(false);
    }
  }

  if (status === 'carregando' || (status === 'autenticado' && !perfil)) {
    return <p>Carregando sessão…</p>;
  }
  if (!autorizado || !accessToken) {
    return <p role="alert">Acesso restrito ao administrador.</p>;
  }

  return (
    <div className="flex flex-col gap-6">
      <h1 className="font-heading text-3xl font-bold">Administração de usuários</h1>

      <Card>
        <details>
          <summary className="cursor-pointer font-semibold">Cadastrar administrador</summary>
          <form
            className="mt-4 grid gap-4 sm:grid-cols-2"
            onSubmit={(evento) => {
              evento.preventDefault();
              const formulario = evento.currentTarget;
              const campos = new FormData(formulario);
              void salvar(async () => {
                await cadastrarAdministrador({
                  nomeCompleto: String(campos.get('nomeCompleto')),
                  nomeUsuario: String(campos.get('nomeUsuario')),
                  nomeExibicao: String(campos.get('nomeExibicao')),
                  email: String(campos.get('email')),
                  cpf: String(campos.get('cpf')),
                  senha: String(campos.get('senha')),
                }, accessToken);
                formulario.reset();
              }, 'Administrador cadastrado.');
            }}
          >
            <label>Nome completo<Input name="nomeCompleto" required minLength={3} maxLength={150} /></label>
            <label>Nome de usuário<Input name="nomeUsuario" required minLength={3} maxLength={50} autoComplete="off" /></label>
            <label>Nome de exibição<Input name="nomeExibicao" required minLength={2} maxLength={100} /></label>
            <label>E-mail<Input name="email" type="email" required maxLength={255} autoComplete="off" /></label>
            <label>CPF (11 dígitos)<Input name="cpf" inputMode="numeric" pattern="[0-9]{11}" required maxLength={11} /></label>
            <label>Senha inicial<Input name="senha" type="password" autoComplete="new-password" required minLength={8} maxLength={50} /></label>
            <Botao type="submit" disabled={ocupado} className="justify-self-start">Criar conta administrativa</Botao>
          </form>
        </details>
      </Card>

      <form
        className="flex flex-wrap items-end gap-3"
        onSubmit={(evento) => {
          evento.preventDefault();
          const campos = new FormData(evento.currentTarget);
          setFiltros({ busca: String(campos.get('busca')).trim(), ativo: String(campos.get('ativo')) });
          setPagina(0);
          setSelecionado(null);
        }}
      >
        <label>Buscar nome ou e-mail<Input name="busca" /></label>
        <label>
          Situação
          <Select name="ativo">
            <option value="">Todas</option>
            <option value="true">Somente ativas</option>
            <option value="false">Somente suspensas</option>
          </Select>
        </label>
        <Botao type="submit" disabled={ocupado}>Aplicar filtros</Botao>
      </form>

      {mensagem && <p role="status">{mensagem}</p>}
      {erro && <p role="alert" className="text-erro">{erro}</p>}
      {consulta.isError && (
        <div role="alert">
          Não foi possível carregar as contas. <Botao onClick={() => consulta.refetch()}>Tentar novamente</Botao>
        </div>
      )}
      {consulta.isFetching && <p>Carregando contas…</p>}
      {dados && (
        <>
          <p>{dados.totalElementos} conta(s) encontrada(s)</p>
          {dados.conteudo.length === 0 ? <p>Nenhuma conta encontrada.</p> : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <caption className="sr-only">Contas cadastradas na plataforma</caption>
                <thead>
                  <tr>
                    {['Nome', 'E-mail', 'Papéis', 'Situação', 'Cadastro', 'Ação'].map((rotulo) => (
                      <th scope="col" className="p-3" key={rotulo}>{rotulo}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {dados.conteudo.map((conta) => (
                    <tr key={conta.id} className="border-borda border-t">
                      <td className="p-3">{conta.nome}</td>
                      <td className="p-3">{conta.email}</td>
                      <td className="p-3">{conta.papeis.map((papel) => PAPEIS[papel]).join(', ')}</td>
                      <td className="p-3">{conta.ativo ? 'Ativa' : 'Suspensa'}</td>
                      <td className="p-3">
                        {conta.criadoEm
                          ? <DataFormatada dataIso={conta.criadoEm} opcoes={{ dateStyle: 'short' }} />
                          : 'Data indisponível'}
                      </td>
                      <td className="p-3">
                        <Botao
                          variante="secundario"
                          disabled={ocupado || conta.id === usuario?.id}
                          onClick={() => {
                            setSelecionado(conta);
                            setErro('');
                            setMensagem('');
                          }}
                        >
                          {conta.ativo ? 'Suspender' : 'Reativar'}
                        </Botao>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          {dados.totalPaginas > 1 && (
            <nav aria-label="Paginação das contas" className="flex items-center gap-4">
              <Botao disabled={pagina === 0 || ocupado} onClick={() => { setPagina(pagina - 1); setSelecionado(null); }}>
                Anterior
              </Botao>
              <span>Página {pagina + 1} de {dados.totalPaginas}</span>
              <Botao disabled={pagina + 1 >= dados.totalPaginas || ocupado} onClick={() => { setPagina(pagina + 1); setSelecionado(null); }}>
                Próxima
              </Botao>
            </nav>
          )}
        </>
      )}

      {selecionado && (
        <Card>
          <h2 className="text-xl font-semibold">
            {selecionado.ativo ? 'Suspender' : 'Reativar'} a conta de {selecionado.nome}
          </h2>
          <form
            key={selecionado.id}
            className="mt-4 flex flex-col gap-4"
            onSubmit={(evento) => {
              evento.preventDefault();
              const motivo = String(new FormData(evento.currentTarget).get('motivo'));
              void salvar(
                () => alterarSituacaoConta(selecionado.id, !selecionado.ativo, motivo, accessToken),
                selecionado.ativo ? 'Conta suspensa.' : 'Conta reativada.',
              );
            }}
          >
            <label>Motivo (mínimo de 10 caracteres)<Textarea name="motivo" required minLength={10} maxLength={1000} /></label>
            <div className="flex gap-3">
              <Botao type="submit" carregando={ocupado}>Confirmar</Botao>
              <Botao variante="secundario" disabled={ocupado} onClick={() => setSelecionado(null)}>Cancelar</Botao>
            </div>
          </form>
        </Card>
      )}
    </div>
  );
}
