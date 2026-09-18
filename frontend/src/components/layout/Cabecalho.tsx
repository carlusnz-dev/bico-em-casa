'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';

import { Botao, classesDoBotao } from '@/components/ui/Botao';
import { AcaoDeNavegacao, useSessao } from '@/hooks/useSessao';

export function Cabecalho() {
  const { apresentacao, logout } = useSessao();
  const router = useRouter();

  async function aoSair() {
    await logout();
    router.push('/');
  }

  function renderizarAcao(acao: AcaoDeNavegacao) {
    const variante = acao.destaque ? 'primario' : 'fantasma';

    if (acao.acao === 'logout') {
      return (
        <Botao key={acao.rotulo} variante={variante} onClick={aoSair}>
          {acao.rotulo}
        </Botao>
      );
    }

    return (
      <Link key={acao.rotulo} href={acao.href ?? '/'} className={classesDoBotao(variante)}>
        {acao.rotulo}
      </Link>
    );
  }

  return (
    <header className="border-b border-borda bg-superficie">
      <div className="mx-auto flex h-16 w-full max-w-5xl items-center justify-between gap-4 px-4">
        <Link href="/" className="font-heading text-xl font-bold text-texto">
          Bico<span className="text-accent">em casa</span>
        </Link>

        <nav className="flex items-center gap-2">
          {apresentacao.mostrarEsqueleto ? (
            <span className="h-10 w-40 animate-pulse rounded-lg bg-borda/50" aria-hidden="true" />
          ) : (
            apresentacao.acoes.map(renderizarAcao)
          )}
        </nav>
      </div>
    </header>
  );
}
