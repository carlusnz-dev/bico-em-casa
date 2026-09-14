'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';

import { buscarMeuPortfolio } from '@/api/portfolio';
import type { Portfolio } from '@/api/contratos/portfolio';
import { ErroApi } from '@/api/erros';
import { FormPortfolio } from '@/components/forms/FormPortfolio';
import { UploadFotoCapa } from '@/components/UploadFotoCapa';
import { classesDoBotao } from '@/components/ui/Botao';
import { Card } from '@/components/ui/Card';
import { useSessao } from '@/hooks/useSessao';

export default function MeuPortfolioPage() {
  const { accessToken, status } = useSessao();
  const router = useRouter();
  const [portfolio, setPortfolio] = useState<Portfolio | null | undefined>(undefined);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    if (status === 'anonimo') {
      router.push('/login');
    }
  }, [status, router]);

  useEffect(() => {
    if (!accessToken) return;

    buscarMeuPortfolio(accessToken)
      .then(setPortfolio)
      .catch((causa) => {
        if (causa instanceof ErroApi && (causa.status === 404 || causa.status === 400)) {
          setPortfolio(null);
          return;
        }
        setErro(
          causa instanceof ErroApi
            ? (causa.detail ?? 'Não foi possível carregar seu portfólio.')
            : 'Não foi possível carregar seu portfólio.',
        );
      });
  }, [accessToken]);

  if (erro) {
    return <p className="rounded-lg bg-erro/10 px-3 py-2 text-sm text-erro">{erro}</p>;
  }

  if (portfolio === undefined) {
    return <p className="text-texto-suave">Carregando…</p>;
  }

  if (portfolio === null) {
    return (
      <div className="mx-auto flex max-w-xl flex-col items-start gap-4">
        <h1 className="font-heading text-3xl font-bold">Meu portfólio</h1>
        <p className="text-texto-suave">Você ainda não criou um portfólio.</p>
        <Link href="/profissionais/novo" className={classesDoBotao('primario')}>
          Criar portfólio
        </Link>
      </div>
    );
  }

  return (
    <div className="mx-auto flex max-w-xl flex-col gap-6">
      <h1 className="font-heading text-3xl font-bold">Meu portfólio</h1>

      <UploadFotoCapa portfolio={portfolio} />

      <Card>
        <FormPortfolio portfolio={portfolio} />
      </Card>
    </div>
  );
}
