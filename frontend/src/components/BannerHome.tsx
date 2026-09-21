import Link from 'next/link';

import { classesDoBotao } from '@/components/ui/Botao';

export function BannerHome() {
  return (
    <section className="relative overflow-hidden rounded-3xl border border-tertiary bg-superficie px-6 py-16 sm:px-12 sm:py-20">
      <div
        aria-hidden="true"
        className="pointer-events-none absolute inset-0 overflow-hidden"
      >
        <div className="absolute -top-16 -right-16 h-64 w-64 rounded-full bg-primary/10" />
        <div className="absolute top-24 right-10 h-24 w-24 rounded-full bg-secondary/15" />
        <div className="absolute -right-6 bottom-0 h-40 w-72 rounded-tl-[6rem] bg-mint/20" />
        <div className="absolute right-28 bottom-10 h-16 w-16 rounded-full border-8 border-primary/15" />
      </div>

      <div className="relative flex max-w-xl flex-col gap-5">
        <h1 className="font-heading text-4xl font-bold text-texto sm:text-5xl">Bico em Casa</h1>
        <p className="text-lg text-texto-suave">
          Encontre tudo em um só lugar: o profissional certo para o seu bico, sem burocracia.
        </p>
        <Link href="/servicos" className={classesDoBotao('primario', 'grande', 'w-fit')}>
          Explorar serviços
        </Link>
      </div>
    </section>
  );
}
