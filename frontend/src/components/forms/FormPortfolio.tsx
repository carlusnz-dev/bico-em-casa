'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';

import { criarPortfolio, editarPortfolio } from '@/api/portfolio';
import {
  Portfolio,
  PortfolioRequest,
  portfolioRequestSchema,
} from '@/api/contratos/portfolio';
import { ErroApi } from '@/api/erros';
import { Botao } from '@/components/ui/Botao';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Textarea } from '@/components/ui/Textarea';
import { useSessao } from '@/hooks/useSessao';

export function FormPortfolio({ portfolio }: { portfolio?: Portfolio }) {
  const { accessToken, status } = useSessao();
  const router = useRouter();
  const [erroGeral, setErroGeral] = useState<string | null>(null);

  useEffect(() => {
    if (status === 'anonimo') {
      router.push('/login');
    }
  }, [status, router]);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<PortfolioRequest>({
    resolver: zodResolver(portfolioRequestSchema),
    defaultValues: portfolio
      ? {
          titulo: portfolio.titulo,
          descricao: portfolio.descricao ?? undefined,
          slugUrl: portfolio.slugUrl,
        }
      : undefined,
  });

  async function aoEnviar(dados: PortfolioRequest) {
    setErroGeral(null);

    if (!accessToken) {
      setErroGeral('Sua sessão expirou. Entre novamente.');
      return;
    }

    try {
      if (portfolio) {
        await editarPortfolio(portfolio.id, dados, accessToken);
      } else {
        await criarPortfolio(dados, accessToken);
      }
      router.push('/profissionais/meu');
      router.refresh();
    } catch (causa) {
      if (causa instanceof ErroApi) {
        setErroGeral(
          causa.semConexao
            ? 'Não foi possível falar com o servidor. Tente de novo.'
            : (causa.detail ?? 'Não foi possível salvar o portfólio.'),
        );
        return;
      }
      setErroGeral('Não foi possível salvar o portfólio.');
    }
  }

  return (
    <form onSubmit={handleSubmit(aoEnviar)} className="flex flex-col gap-4" noValidate>
      <div className="flex flex-col gap-1.5">
        <Label htmlFor="titulo" obrigatorio>
          Título
        </Label>
        <Input id="titulo" erro={Boolean(errors.titulo)} {...register('titulo')} />
        {errors.titulo && <p className="text-sm text-erro">{errors.titulo.message}</p>}
      </div>

      <div className="flex flex-col gap-1.5">
        <Label htmlFor="descricao">Descrição</Label>
        <Textarea
          id="descricao"
          rows={4}
          erro={Boolean(errors.descricao)}
          {...register('descricao')}
        />
        {errors.descricao && <p className="text-sm text-erro">{errors.descricao.message}</p>}
      </div>

      <div className="flex flex-col gap-1.5">
        <Label htmlFor="slugUrl" obrigatorio>
          Endereço (ex.: joao-eletricista)
        </Label>
        <Input id="slugUrl" erro={Boolean(errors.slugUrl)} {...register('slugUrl')} />
        {errors.slugUrl && <p className="text-sm text-erro">{errors.slugUrl.message}</p>}
      </div>

      {erroGeral && (
        <p role="alert" className="rounded-lg bg-erro/10 px-3 py-2 text-sm text-erro">
          {erroGeral}
        </p>
      )}

      <Botao type="submit" tamanho="grande" carregando={isSubmitting}>
        {portfolio ? 'Salvar alterações' : 'Criar portfólio'}
      </Botao>
    </form>
  );
}
