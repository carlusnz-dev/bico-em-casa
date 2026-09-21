'use client';

import clsx from 'clsx';
import { LogOut, Menu, Search, User, X } from 'lucide-react';
import { AnimatePresence, motion } from 'motion/react';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useState } from 'react';

import { Logo } from '@/components/Logo';
import { ThemeToggle } from '@/components/ThemeToggle';
import { AcaoDeNavegacao, useSessao } from '@/hooks/useSessao';

function ItemMenu({ item, aoNavegar }: { item: AcaoDeNavegacao; aoNavegar?: () => void }) {
  const pathname = usePathname();

  if (item.desabilitado || !item.href) {
    return (
      <span
        title={item.motivoDesabilitado}
        aria-disabled="true"
        className="cursor-not-allowed rounded-lg px-3 py-2 text-sm text-texto-suave/50"
      >
        {item.rotulo}
      </span>
    );
  }

  const ativo = pathname === item.href || pathname.startsWith(`${item.href}/`);

  return (
    <Link
      href={item.href}
      onClick={aoNavegar}
      aria-current={ativo ? 'page' : undefined}
      className={clsx(
        'rounded-lg px-3 py-2 text-sm font-medium transition-colors',
        ativo ? 'bg-primary/10 text-primary' : 'text-texto-suave hover:bg-borda/40 hover:text-texto',
      )}
    >
      {item.rotulo}
    </Link>
  );
}

export function Cabecalho() {
  const { apresentacao, status, perfil, logout } = useSessao();
  const router = useRouter();
  const [menuAberto, setMenuAberto] = useState(false);
  const [busca, setBusca] = useState('');

  async function aoSair() {
    await logout();
    setMenuAberto(false);
    router.push('/');
  }

  function aoBuscar(evento: React.FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    const termo = busca.trim();
    router.push(termo ? `/servicos?busca=${encodeURIComponent(termo)}` : '/servicos');
  }

  return (
    <header className="sticky top-0 z-40 border-b border-borda bg-superficie">
      {/* Zona 1 — marca, busca e conta */}
      <div className="mx-auto flex h-16 w-full max-w-6xl items-center gap-3 px-4">
        <Logo />

        <form onSubmit={aoBuscar} role="search" className="min-w-0 flex-1">
          <label htmlFor="busca-servicos" className="sr-only">
            Pesquisar serviços
          </label>
          <div className="relative">
            <Search
              size={16}
              aria-hidden="true"
              className="pointer-events-none absolute top-1/2 left-3 -translate-y-1/2 text-texto-suave"
            />
            <input
              id="busca-servicos"
              type="search"
              value={busca}
              onChange={(evento) => setBusca(evento.target.value)}
              placeholder="Buscar serviços…"
              className="h-10 w-full rounded-full border border-borda bg-fundo pl-9 pr-3 text-sm text-texto placeholder:text-texto-suave focus:border-primary focus:outline-2 focus:outline-offset-1 focus:outline-primary"
            />
          </div>
        </form>

        <div className="flex shrink-0 items-center gap-1">
          {apresentacao.mostrarEsqueleto ? (
            <span className="h-10 w-32 animate-pulse rounded-lg bg-borda/50" aria-hidden="true" />
          ) : status === 'autenticado' ? (
            <>
              <Link
                href="/perfil"
                aria-label={perfil ? `Perfil de ${perfil.nomeExibicao}` : 'Meu perfil'}
                className="flex h-10 w-10 items-center justify-center rounded-lg text-texto-suave transition-colors hover:bg-borda/40 hover:text-texto"
              >
                <User size={18} aria-hidden="true" />
              </Link>
              <ThemeToggle />
              <button
                type="button"
                onClick={aoSair}
                aria-label="Sair da conta"
                className="flex h-10 w-10 items-center justify-center rounded-lg text-texto-suave transition-colors hover:bg-erro/10 hover:text-erro"
              >
                <LogOut size={18} aria-hidden="true" />
              </button>
            </>
          ) : (
            <>
              <ThemeToggle />
              {apresentacao.contaAnonima.map((acao) => (
                <Link
                  key={acao.rotulo}
                  href={acao.href ?? '/'}
                  className="inline-flex h-10 items-center justify-center rounded-lg bg-primary px-4 text-sm font-medium text-white transition-colors hover:bg-primary-dark"
                >
                  {acao.rotulo}
                </Link>
              ))}
            </>
          )}

          {apresentacao.menuPrincipal.length > 0 && (
            <button
              type="button"
              onClick={() => setMenuAberto((aberto) => !aberto)}
              aria-expanded={menuAberto}
              aria-controls="menu-principal"
              aria-label="Abrir menu"
              className="ml-1 flex h-10 w-10 items-center justify-center rounded-lg text-texto-suave hover:bg-borda/40 hover:text-texto md:hidden"
            >
              {menuAberto ? <X size={18} aria-hidden="true" /> : <Menu size={18} aria-hidden="true" />}
            </button>
          )}
        </div>
      </div>

      {/* Zona 2 — navegação das áreas do usuário */}
      {apresentacao.menuPrincipal.length > 0 && (
        <>
          <nav
            aria-label="Áreas da conta"
            className="mx-auto hidden w-full max-w-6xl items-center gap-1 px-4 pb-2 md:flex"
          >
            {apresentacao.menuPrincipal.map((item) => (
              <ItemMenu key={item.rotulo} item={item} />
            ))}
          </nav>

          <AnimatePresence initial={false}>
            {menuAberto && (
              <motion.nav
                id="menu-principal"
                aria-label="Áreas da conta"
                initial={{ height: 0, opacity: 0 }}
                animate={{ height: 'auto', opacity: 1 }}
                exit={{ height: 0, opacity: 0 }}
                transition={{ duration: 0.2, ease: 'easeInOut' }}
                className="overflow-hidden border-t border-borda md:hidden"
              >
                <div className="flex flex-col gap-1 px-4 py-2">
                  {apresentacao.menuPrincipal.map((item) => (
                    <ItemMenu key={item.rotulo} item={item} aoNavegar={() => setMenuAberto(false)} />
                  ))}
                </div>
              </motion.nav>
            )}
          </AnimatePresence>
        </>
      )}
    </header>
  );
}
