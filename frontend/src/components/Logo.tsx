import Link from 'next/link';
import clsx from 'clsx';

/**
 * Símbolo do tucano: só formas geométricas (dois arcos + um círculo),
 * na paleta primary/secondary. Reaproveitado sozinho (ícone) ou com a
 * marca por extenso — a mesma peça que deve virar dois componentes no
 * Figma (tucano; tucano + nome).
 */
function SimboloTucano({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 40 40"
      role="img"
      aria-label="Bico em Casa"
      className={className}
    >
      <circle cx="18" cy="22" r="12" className="fill-primary" />
      <path
        d="M23 15c8-2 14 1 16 6-6 3-12 2-16-1z"
        className="fill-secondary"
      />
      <circle cx="14" cy="18" r="2" className="fill-white-warm" />
    </svg>
  );
}

export function Logo({ className }: { className?: string }) {
  return (
    <Link
      href="/"
      aria-label="Bico em Casa — ir para a página inicial"
      className={clsx(
        'flex shrink-0 items-center gap-2 font-heading font-bold text-texto',
        className,
      )}
    >
      <SimboloTucano className="h-8 w-8" />
      <span className="hidden text-lg sm:inline">
        Bico<span className="text-secondary">em casa</span>
      </span>
    </Link>
  );
}
