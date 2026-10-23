'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { useForm } from 'react-hook-form';

import { cadastrar } from '@/api/autenticacao';
import { CadastroRequest, cadastroRequestSchema } from '@/api/contratos/autenticacao';
import { ErroApi } from '@/api/erros';
import { Botao } from '@/components/ui/Botao';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';

const TIPOS = [
  { valor: 'CLIENTE', rotulo: 'Quero contratar' },
  { valor: 'PROFISSIONAL', rotulo: 'Quero trabalhar' },
] as const;

export default function FormCadastro() {
  const router = useRouter();
  const [erroGeral, setErroGeral] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<CadastroRequest>({
    resolver: zodResolver(cadastroRequestSchema),
    defaultValues: { cadastroTipo: 'CLIENTE' },
    shouldUnregister: true,
  });
  const cadastroTipo = watch('cadastroTipo');

  async function aoEnviar(dados: CadastroRequest) {
    setErroGeral(null);

    try {
      await cadastrar(dados);
      router.push('/login?cadastro=ok');
    } catch (causa) {
      if (causa instanceof ErroApi) {
        setErroGeral(
          causa.semConexao
            ? 'Não foi possível falar com o servidor. Tente de novo.'
            : (causa.detail ?? 'Não foi possível criar a conta.'),
        );
        return;
      }
      setErroGeral('Não foi possível criar a conta.');
    }
  }

  return (
    <form onSubmit={handleSubmit(aoEnviar)} className="flex flex-col gap-4" noValidate>
      <fieldset className="flex flex-col gap-1.5">
        <legend className="mb-1.5 text-sm font-medium text-texto">O que você procura?</legend>
        <div className="flex gap-2">
          {TIPOS.map((tipo) => (
            <label
              key={tipo.valor}
              className="flex flex-1 cursor-pointer items-center gap-2 rounded-lg border border-borda px-3 py-2 text-sm has-checked:border-primary has-checked:bg-primary/10"
            >
              <input type="radio" value={tipo.valor} {...register('cadastroTipo')} />
              {tipo.rotulo}
            </label>
          ))}
        </div>
      </fieldset>

      <div className="flex flex-col gap-1.5">
        <Label htmlFor="nomeCompleto" obrigatorio>
          Nome completo
        </Label>
        <Input
          id="nomeCompleto"
          autoComplete="name"
          erro={Boolean(errors.nomeCompleto)}
          {...register('nomeCompleto')}
        />
        {errors.nomeCompleto && (
          <p className="text-sm text-erro">{errors.nomeCompleto.message}</p>
        )}
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        <div className="flex flex-col gap-1.5">
          <Label htmlFor="nomeUsuario" obrigatorio>
            Nome de usuário
          </Label>
          <Input
            id="nomeUsuario"
            autoComplete="username"
            placeholder="joao.silva"
            erro={Boolean(errors.nomeUsuario)}
            {...register('nomeUsuario')}
          />
          {errors.nomeUsuario && (
            <p className="text-sm text-erro">{errors.nomeUsuario.message}</p>
          )}
        </div>

        <div className="flex flex-col gap-1.5">
          <Label htmlFor="nomeExibicao" obrigatorio>
            Como quer ser chamado
          </Label>
          <Input
            id="nomeExibicao"
            erro={Boolean(errors.nomeExibicao)}
            {...register('nomeExibicao')}
          />
          {errors.nomeExibicao && (
            <p className="text-sm text-erro">{errors.nomeExibicao.message}</p>
          )}
        </div>
      </div>

      <div className="flex flex-col gap-1.5">
        <Label htmlFor="email" obrigatorio>
          E-mail
        </Label>
        <Input
          id="email"
          type="email"
          autoComplete="email"
          erro={Boolean(errors.email)}
          {...register('email')}
        />
        {errors.email && <p className="text-sm text-erro">Informe um e-mail válido</p>}
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        <div className="flex flex-col gap-1.5">
          <Label htmlFor="cpf" obrigatorio>
            CPF
          </Label>
          <Input
            id="cpf"
            inputMode="numeric"
            placeholder="00000000000"
            erro={Boolean(errors.cpf)}
            {...register('cpf')}
          />
          {errors.cpf && <p className="text-sm text-erro">{errors.cpf.message}</p>}
        </div>

        <div className="flex flex-col gap-1.5">
          <Label htmlFor="telefone" obrigatorio>
            Telefone
          </Label>
          <Input
            id="telefone"
            inputMode="tel"
            autoComplete="tel"
            placeholder="31999998888"
            erro={Boolean(errors.telefone)}
            {...register('telefone')}
          />
          {errors.telefone && <p className="text-sm text-erro">{errors.telefone.message}</p>}
        </div>
      </div>

      {cadastroTipo === 'PROFISSIONAL' && (
        <fieldset className="flex flex-col gap-4 rounded-xl border border-borda p-4">
          <legend className="px-1 text-sm font-semibold text-texto">
            Endereço de atendimento
          </legend>

          {errors.endereco?.message && (
            <p role="alert" className="text-sm text-erro">
              {errors.endereco.message}
            </p>
          )}

          <div className="flex flex-col gap-1.5">
            <Label htmlFor="endereco.logradouro" obrigatorio>
              Endereço
            </Label>
            <Input
              id="endereco.logradouro"
              autoComplete="address-line1"
              erro={Boolean(errors.endereco?.logradouro)}
              {...register('endereco.logradouro')}
            />
            {errors.endereco?.logradouro && (
              <p className="text-sm text-erro">{errors.endereco.logradouro.message}</p>
            )}
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="endereco.numero">Número</Label>
              <Input
                id="endereco.numero"
                autoComplete="address-line2"
                erro={Boolean(errors.endereco?.numero)}
                {...register('endereco.numero')}
              />
              {errors.endereco?.numero && (
                <p className="text-sm text-erro">{errors.endereco.numero.message}</p>
              )}
            </div>

            <div className="flex flex-col gap-1.5">
              <Label htmlFor="endereco.complemento">Complemento</Label>
              <Input
                id="endereco.complemento"
                erro={Boolean(errors.endereco?.complemento)}
                {...register('endereco.complemento')}
              />
              {errors.endereco?.complemento && (
                <p className="text-sm text-erro">{errors.endereco.complemento.message}</p>
              )}
            </div>
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="endereco.bairro" obrigatorio>
                Bairro
              </Label>
              <Input
                id="endereco.bairro"
                autoComplete="address-level3"
                erro={Boolean(errors.endereco?.bairro)}
                {...register('endereco.bairro')}
              />
              {errors.endereco?.bairro && (
                <p className="text-sm text-erro">{errors.endereco.bairro.message}</p>
              )}
            </div>

            <div className="flex flex-col gap-1.5">
              <Label htmlFor="endereco.cidade" obrigatorio>
                Cidade
              </Label>
              <Input
                id="endereco.cidade"
                autoComplete="address-level2"
                erro={Boolean(errors.endereco?.cidade)}
                {...register('endereco.cidade')}
              />
              {errors.endereco?.cidade && (
                <p className="text-sm text-erro">{errors.endereco.cidade.message}</p>
              )}
            </div>
          </div>

          <div className="flex flex-col gap-1.5 sm:max-w-36">
            <Label htmlFor="endereco.uf" obrigatorio>
              UF
            </Label>
            <Input
              id="endereco.uf"
              autoComplete="address-level1"
              maxLength={2}
              erro={Boolean(errors.endereco?.uf)}
              {...register('endereco.uf')}
            />
            {errors.endereco?.uf && (
              <p className="text-sm text-erro">{errors.endereco.uf.message}</p>
            )}
          </div>
        </fieldset>
      )}

      <div className="flex flex-col gap-1.5">
        <Label htmlFor="senha" obrigatorio>
          Senha
        </Label>
        <Input
          id="senha"
          type="password"
          autoComplete="new-password"
          erro={Boolean(errors.senha)}
          {...register('senha')}
        />
        {errors.senha && <p className="text-sm text-erro">A senha tem de 8 a 50 caracteres</p>}
      </div>

      <div className="flex flex-col gap-1.5">
        <Label htmlFor="bio">Sobre você</Label>
        <Input id="bio" placeholder="Opcional" erro={Boolean(errors.bio)} {...register('bio')} />
        {errors.bio && <p className="text-sm text-erro">{errors.bio.message}</p>}
      </div>

      {erroGeral && (
        <p role="alert" className="rounded-lg bg-erro/10 px-3 py-2 text-sm text-erro">
          {erroGeral}
        </p>
      )}

      <Botao type="submit" tamanho="grande" carregando={isSubmitting}>
        Criar conta
      </Botao>

      <p className="text-center text-sm text-texto-suave">
        Já tem conta?{' '}
        <Link href="/login" className="font-medium text-primary hover:underline">
          Entrar
        </Link>
      </p>
    </form>
  );
}
