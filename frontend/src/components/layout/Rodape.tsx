import Link from 'next/link';

const LINKS_INSTITUCIONAIS = [
  { rotulo: 'Sobre', href: '/sobre' },
  { rotulo: 'Termos de uso', href: '/termos' },
  { rotulo: 'Privacidade', href: '/privacidade' },
];

export function Rodape() {
  return (
    <footer className="mt-auto border-t border-borda bg-superficie">
      <div className="mx-auto flex w-full max-w-5xl flex-col gap-2 px-4 py-6 text-sm text-texto-suave sm:flex-row sm:items-center sm:justify-between">
        <p>© {new Date().getFullYear()} Bico em Casa</p>

        <nav className="flex flex-wrap gap-4">
          {LINKS_INSTITUCIONAIS.map((link) => (
            <Link key={link.href} href={link.href} className="hover:text-texto">
              {link.rotulo}
            </Link>
          ))}
        </nav>
      </div>
    </footer>
  );
}
