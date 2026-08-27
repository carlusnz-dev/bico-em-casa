'use client';

import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useState } from 'react';

/**
 * Reúne os provedores de contexto client-side da aplicação. Hoje só o
 * TanStack Query; qualquer outro provider global (tema, store do Zustand
 * com contexto, etc.) entra aqui — é o único arquivo com `'use client'` do
 * root layout.
 *
 * O `QueryClient` nasce dentro de `useState` para garantir uma instância por
 * sessão de navegador, evitando compartilhar cache entre requisições
 * diferentes no lado do servidor.
 */
export function Provedores({ children }: { children: React.ReactNode }) {
  const [clienteDeConsulta] = useState(() => new QueryClient());

  return <QueryClientProvider client={clienteDeConsulta}>{children}</QueryClientProvider>;
}
