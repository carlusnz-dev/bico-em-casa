'use client';

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
} from 'react';

import { entrar, renovar, sair } from '@/api/autenticacao';
import { LoginRequest } from '@/api/contratos/autenticacao';
import { Usuario } from '@/api/contratos/usuario';
import { buscarUsuarioPorId } from '@/api/usuario';

export type StatusSessao = 'carregando' | 'autenticado' | 'anonimo';

export interface AcaoDeNavegacao {
  rotulo: string;
  href?: string;
  acao?: 'logout';
  destaque?: boolean;
}

export interface ApresentacaoDaSessao {
  acoes: readonly AcaoDeNavegacao[];
  mostrarEsqueleto: boolean;
  saudacao: (usuario: Usuario | null) => string;
}

export const NAVEGACAO_POR_STATUS: Record<StatusSessao, ApresentacaoDaSessao> = {
  carregando: {
    acoes: [],
    mostrarEsqueleto: true,
    saudacao: () => 'Carregando sua sessão…',
  },
  anonimo: {
    acoes: [
      { rotulo: 'Entrar', href: '/login' },
      { rotulo: 'Criar conta', href: '/cadastro', destaque: true },
    ],
    mostrarEsqueleto: false,
    saudacao: () => 'Encontre um profissional para o seu bico',
  },
  autenticado: {
    acoes: [
      { rotulo: 'Minhas contratações', href: '/contratacoes' },
      { rotulo: 'Meus serviços', href: '/servicos/meus' },
      { rotulo: 'Meu portfólio', href: '/profissionais/meu' },
      { rotulo: 'Meu perfil', href: '/perfil' },
      { rotulo: 'Sair', acao: 'logout' },
    ],
    mostrarEsqueleto: false,
    saudacao: (usuario) =>
      usuario ? `Olá, ${usuario.nome.split(' ')[0]}` : 'Olá',
  },
};

interface SessaoContexto {
  accessToken: string | null;
  usuario: Usuario | null;
  status: StatusSessao;
  apresentacao: ApresentacaoDaSessao;
  login: (credenciais: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
}

const SessaoContext = createContext<SessaoContexto | null>(null);

const MARGEM_RENOVACAO_MS = 30_000;

function payloadDoToken(token: string): Record<string, unknown> | null {
  const payloadCodificado = token.split('.')[1];
  if (!payloadCodificado) return null;

  try {
    const base64 = payloadCodificado.replace(/-/g, '+').replace(/_/g, '/');
    const payload: unknown = JSON.parse(atob(base64));

    if (typeof payload === 'object' && payload !== null) {
      return payload as Record<string, unknown>;
    }
  } catch {
    return null;
  }

  return null;
}

function expiracaoDoToken(token: string): number | null {
  const exp = payloadDoToken(token)?.exp;
  return typeof exp === 'number' ? exp * 1000 : null;
}

function usuarioIdDoToken(token: string): number | null {
  const sub = payloadDoToken(token)?.sub;
  if (typeof sub !== 'string') return null;

  const id = Number(sub);
  return Number.isInteger(id) ? id : null;
}

export function SessaoProvider({ children }: { children: React.ReactNode }) {
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [usuario, setUsuario] = useState<Usuario | null>(null);
  const [status, setStatus] = useState<StatusSessao>('carregando');

  const timerRenovacao = useRef<ReturnType<typeof setTimeout> | null>(null);
  const aplicarSessaoRef = useRef<((token: string) => Promise<void>) | null>(null);

  const limparRenovacao = useCallback(() => {
    if (timerRenovacao.current !== null) {
      clearTimeout(timerRenovacao.current);
      timerRenovacao.current = null;
    }
  }, []);

  const encerrarSessao = useCallback(() => {
    limparRenovacao();
    setAccessToken(null);
    setUsuario(null);
    setStatus('anonimo');
  }, [limparRenovacao]);

  const agendarRenovacao = useCallback(
    (token: string) => {
      limparRenovacao();

      const expiraEm = expiracaoDoToken(token);
      if (expiraEm === null) return;

      const atraso = Math.max(expiraEm - Date.now() - MARGEM_RENOVACAO_MS, 0);

      timerRenovacao.current = setTimeout(async () => {
        try {
          const resposta = await renovar();
          await aplicarSessaoRef.current?.(resposta.accessToken);
        } catch {
          encerrarSessao();
        }
      }, atraso);
    },
    [encerrarSessao, limparRenovacao],
  );

  const aplicarSessao = useCallback(
    async (token: string) => {
      setAccessToken(token);
      setStatus('autenticado');
      agendarRenovacao(token);

      const id = usuarioIdDoToken(token);
      if (id === null) {
        setUsuario(null);
        return;
      }

      try {
        setUsuario(await buscarUsuarioPorId(id, token));
      } catch {
        setUsuario(null);
      }
    },
    [agendarRenovacao],
  );

  useEffect(() => {
    aplicarSessaoRef.current = aplicarSessao;
  }, [aplicarSessao]);

  useEffect(() => {
    renovar()
      .then((resposta) => aplicarSessao(resposta.accessToken))
      .catch(() => setStatus('anonimo'));

    return limparRenovacao;
  }, [aplicarSessao, limparRenovacao]);

  const login = useCallback(
    async (credenciais: LoginRequest) => {
      const resposta = await entrar(credenciais);
      await aplicarSessao(resposta.accessToken);
    },
    [aplicarSessao],
  );

  const logout = useCallback(async () => {
    limparRenovacao();

    try {
      await sair();
    } finally {
      encerrarSessao();
    }
  }, [encerrarSessao, limparRenovacao]);

  return (
    <SessaoContext.Provider
      value={{
        accessToken,
        usuario,
        status,
        apresentacao: NAVEGACAO_POR_STATUS[status],
        login,
        logout,
      }}
    >
      {children}
    </SessaoContext.Provider>
  );
}

export function useSessao(): SessaoContexto {
  const contexto = useContext(SessaoContext);
  if (contexto === null) {
    throw new Error('useSessao precisa ser usado dentro de um SessaoProvider');
  }
  return contexto;
}
