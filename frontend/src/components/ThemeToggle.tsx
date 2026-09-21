'use client';

import { Moon, Sun } from 'lucide-react';
import { AnimatePresence, motion } from 'motion/react';
import { useEffect, useState } from 'react';

const CHAVE_TEMA = 'bico-em-casa:tema';

function aplicarTema(escuro: boolean) {
  document.documentElement.classList.toggle('dark', escuro);
}

export function ThemeToggle({ className }: { className?: string }) {
  const [escuro, setEscuro] = useState(false);

  useEffect(() => {
    setEscuro(document.documentElement.classList.contains('dark'));
  }, []);

  function alternar() {
    const proximo = !escuro;
    setEscuro(proximo);
    aplicarTema(proximo);
    localStorage.setItem(CHAVE_TEMA, proximo ? 'escuro' : 'claro');
  }

  return (
    <button
      type="button"
      onClick={alternar}
      aria-label={escuro ? 'Ativar tema claro' : 'Ativar tema escuro'}
      aria-pressed={escuro}
      className={
        className ??
        'flex h-10 w-10 items-center justify-center rounded-lg text-texto-suave transition-colors hover:bg-borda/40 hover:text-texto'
      }
    >
      <AnimatePresence mode="wait" initial={false}>
        <motion.span
          key={escuro ? 'lua' : 'sol'}
          initial={{ opacity: 0, rotate: -90, scale: 0.6 }}
          animate={{ opacity: 1, rotate: 0, scale: 1 }}
          exit={{ opacity: 0, rotate: 90, scale: 0.6 }}
          transition={{ duration: 0.2 }}
          className="flex"
        >
          {escuro ? <Moon size={18} aria-hidden="true" /> : <Sun size={18} aria-hidden="true" />}
        </motion.span>
      </AnimatePresence>
    </button>
  );
}
