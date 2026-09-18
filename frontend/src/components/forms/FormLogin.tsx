'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { useForm } from 'react-hook-form';

import { LoginRequest, loginRequestSchema } from '@/api/contratos/autenticacao';
import { ErroApi } from '@/api/erros';
import { Botao } from '@/components/ui/Botao';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { useSessao } from '@/hooks/useSessao';

export default function FormLogin() {
  const { login } = useSessao();
  const router = useRouter();
  const [erroGeral, setErroGeral] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginRequest>({ resolver: zodResolver(loginRequestSchema) });

  async function aoEnviar(dados: LoginRequest) {
    setErroGeral(null);

    try {
      await login(dados);
      router.push('/');
    } catch (causa) {
      if (causa instanceof ErroApi) {
        setErroGeral(
          causa.semConexao
            ? 'Não foi possível falar com o servidor. Tente de novo.'
            : (causa.detail ?? 'Não foi possível entrar.'),
        );
        return;
      }
      setErroGeral('Não foi possível entrar.');
    }
  }

  return (
    <form onSubmit={handleSubmit(aoEnviar)} className="flex flex-col gap-4" noValidate>
      <div className="flex flex-col gap-1.5">
        <Label htmlFor="email" obrigatorio>
          E-mail
        </Label>
        <Input
          id="email"
          type="email"
          autoComplete="email"
          placeholder="voce@exemplo.com"
          erro={Boolean(errors.email)}
          {...register('email')}
        />
        {errors.email && <p className="text-sm text-erro">Informe um e-mail válido</p>}
      </div>

      <div className="flex flex-col gap-1.5">
        <Label htmlFor="senha" obrigatorio>
          Senha
        </Label>
        <Input
          id="senha"
          type="password"
          autoComplete="current-password"
          erro={Boolean(errors.senha)}
          {...register('senha')}
        />
        {errors.senha && <p className="text-sm text-erro">A senha tem de 8 a 50 caracteres</p>}
      </div>

      {erroGeral && (
        <p role="alert" className="rounded-lg bg-erro/10 px-3 py-2 text-sm text-erro">
          {erroGeral}
        </p>
      )}

      <Botao type="submit" tamanho="grande" carregando={isSubmitting}>
        Entrar
      </Botao>

      <p className="text-center text-sm text-texto-suave">
        Ainda não tem conta?{' '}
        <Link href="/cadastro" className="font-medium text-primary hover:underline">
          Criar conta
        </Link>
      </p>
    </form>
  );
}
