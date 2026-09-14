'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';

import { editarContratacao } from '@/api/contratacoes';
import {
  Contratacao,
  EditarContratacaoRequest,
  editarContratacaoRequestSchema,
} from '@/api/contratos/contratacao';
import { ErroApi } from '@/api/erros';
import { Botao } from '@/components/ui/Botao';
import { Label } from '@/components/ui/Label';
import { Textarea } from '@/components/ui/Textarea';
import { useSessao } from '@/hooks/useSessao';

export function FormContratacao({ contratacao }: { contratacao: Contratacao }) {
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
    formState: { isSubmitting },
  } = useForm<EditarContratacaoRequest>({
    resolver: zodResolver(editarContratacaoRequestSchema),
    defaultValues: { observacao: contratacao.observacao ?? '' },
  });

  async function aoEnviar(dados: EditarContratacaoRequest) {
    setErroGeral(null);

    if (!accessToken) {
      setErroGeral('Sua sessão expirou. Entre novamente.');
      return;
    }

    try {
      await editarContratacao(contratacao.id, dados, accessToken);
      router.push('/contratacoes');
      router.refresh();
    } catch (causa) {
      if (causa instanceof ErroApi) {
        setErroGeral(
          causa.semConexao
            ? 'Não foi possível falar com o servidor. Tente de novo.'
            : (causa.detail ?? 'Não foi possível salvar a contratação.'),
        );
        return;
      }
      setErroGeral('Não foi possível salvar a contratação.');
    }
  }

  return (
    <form onSubmit={handleSubmit(aoEnviar)} className="flex flex-col gap-4" noValidate>
      <div className="flex flex-col gap-1.5">
        <Label htmlFor="observacao">Observação</Label>
        <Textarea id="observacao" rows={4} {...register('observacao')} />
      </div>

      {erroGeral && (
        <p role="alert" className="rounded-lg bg-erro/10 px-3 py-2 text-sm text-erro">
          {erroGeral}
        </p>
      )}

      <Botao type="submit" tamanho="grande" carregando={isSubmitting}>
        Salvar alterações
      </Botao>
    </form>
  );
}
