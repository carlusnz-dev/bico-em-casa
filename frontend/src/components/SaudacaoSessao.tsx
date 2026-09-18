'use client';

import Link from 'next/link';

import { classesDoBotao } from '@/components/ui/Botao';
import { Card } from '@/components/ui/Card';
import { useSessao } from '@/hooks/useSessao';

export function SaudacaoSessao() {
  const { apresentacao, status, usuario } = useSessao();

  return (
    <section className="flex flex-col gap-4">
      <h1 className="font-heading text-4xl font-bold">{apresentacao.saudacao(usuario)}</h1>

      {apresentacao.mostrarEsqueleto ? (
        <span className="h-5 w-72 animate-pulse rounded bg-borda/50" aria-hidden="true" />
      ) : (
        <p className="max-w-xl text-texto-suave">
          {status === 'autenticado'
            ? 'Sua conta está ativa. Em breve você poderá buscar profissionais e acompanhar suas contratações por aqui.'
            : 'Encontre o profissional, veja o portfólio e contrate sem burocracia.'}
        </p>
      )}

      {status === 'anonimo' && (
        <div className="flex flex-wrap gap-2">
          <Link href="/cadastro" className={classesDoBotao('primario', 'grande')}>
            Criar conta
          </Link>
          <Link href="/login" className={classesDoBotao('secundario', 'grande')}>
            Já tenho conta
          </Link>
        </div>
      )}

      {status === 'autenticado' && usuario && (
        <Card className="max-w-md">
          <h2 className="mb-3 text-lg font-semibold">Sua conta</h2>
          <dl className="flex flex-col gap-2 text-sm">
            <div className="flex justify-between gap-4">
              <dt className="text-texto-suave">Nome</dt>
              <dd>{usuario.nome}</dd>
            </div>
            <div className="flex justify-between gap-4">
              <dt className="text-texto-suave">E-mail</dt>
              <dd>{usuario.email}</dd>
            </div>
          </dl>
        </Card>
      )}
    </section>
  );
}
