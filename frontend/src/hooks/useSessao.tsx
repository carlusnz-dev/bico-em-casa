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

type StatusSessao = 'carregando' | 'autenticado' | 'anonimo';

interface SessaoContexto {
  accessToken: string | null;
  status: StatusSessao;
  login: (credenciais: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
}

const SessaoContext = createContext<SessaoContexto | null>(null);

/** Renova o access token esta margem antes do exp real, absorvendo latência de rede. */
const MARGEM_RENOVACAO_MS = 30_000;

function expiracaoDoToken(token: string): number | null {
  const payloadCodificado = token.split('.')[1];
  if (!payloadCodificado) return null;

  try {
    const base64 = payloadCodificado.replace(/-/g, '+').replace(/_/g, '/');
    const payload: unknown = JSON.parse(atob(base64));

    if (
      typeof payload === 'object' &&
      payload !== null &&
      'exp' in payload &&
      typeof (payload as { exp: unknown }).exp === 'number'
    ) {
      return (payload as { exp: number }).exp * 1000;
    }
  } catch {
    return null;
  }

  return null;
}

export function SessaoProvider({ children }: { children: React.ReactNode }) {
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [status, setStatus] = useState<StatusSessao>('carregando');
  const timerRenovacao = useRef<ReturnType<typeof setTimeout> | null>(null);

  const limparRenovacao = useCallback(() => {
    if (timerRenovacao.current !== null) {
      clearTimeout(timerRenovacao.current);
      timerRenovacao.current = null;
    }
  }, []);

  const agendarRenovacao = useCallback(
    (token: string) => {
      limparRenovacao();

      const expiraEm = expiracaoDoToken(token);
      if (expiraEm === null) return;

      const atraso = Math.max(expiraEm - Date.now() - MARGEM_RENOVACAO_MS, 0);

      timerRenovacao.current = setTimeout(async () => {
        try {
          const resposta = await renovar();
          setAccessToken(resposta.accessToken);
          setStatus('autenticado');
          agendarRenovacao(resposta.accessToken);
        } catch {
          setAccessToken(null);
          setStatus('anonimo');
        }
      }, atraso);
    },
    [limparRenovacao],
  );

  useEffect(() => {
    renovar()
      .then((resposta) => {
        setAccessToken(resposta.accessToken);
        setStatus('autenticado');
        agendarRenovacao(resposta.accessToken);
      })
      .catch(() => {
        setStatus('anonimo');
      });

    return limparRenovacao;
  }, [agendarRenovacao, limparRenovacao]);

  const login = useCallback(
    async (credenciais: LoginRequest) => {
      const resposta = await entrar(credenciais);
      setAccessToken(resposta.accessToken);
      setStatus('autenticado');
      agendarRenovacao(resposta.accessToken);
    },
    [agendarRenovacao],
  );

  const logout = useCallback(async () => {
    limparRenovacao();
    await sair();
    setAccessToken(null);
    setStatus('anonimo');
  }, [limparRenovacao]);

  return (
    <SessaoContext.Provider value={{ accessToken, status, login, logout }}>
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
