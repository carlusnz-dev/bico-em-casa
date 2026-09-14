import Link from 'next/link';

import { Cabecalho } from '@/components/layout/Cabecalho';
import { Rodape } from '@/components/layout/Rodape';
import { classesDoBotao } from '@/components/ui/Botao';

export default function NaoEncontrado() {
  return (
    <div className="flex min-h-dvh flex-col">
      <Cabecalho />
      <main className="mx-auto flex w-full max-w-5xl flex-1 flex-col items-center justify-center gap-4 px-4 py-20 text-center">
        <p className="font-heading text-7xl font-bold text-primary">404</p>
        <h1 className="text-2xl font-bold">Esta página não existe</h1>
        <p className="max-w-md text-texto-suave">
          O endereço pode ter mudado de lugar, ou nunca ter existido.
        </p>
        <Link href="/" className={classesDoBotao('primario', 'grande')}>
          Voltar para a home
        </Link>
      </main>
      <Rodape />
    </div>
  );
}
