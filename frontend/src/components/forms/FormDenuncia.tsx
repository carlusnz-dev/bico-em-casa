'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import * as z from 'zod';

import { criarDenuncia } from '@/api/denuncias';
import { ErroApi } from '@/api/erros';
import { Botao } from '@/components/ui/Botao';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Textarea } from '@/components/ui/Textarea';

const ALVOS = [
  { valor: 'PERFIL', rotulo: 'O profissional' },
  { valor: 'SERVICO', rotulo: 'O serviço anunciado' },
] as const;

const formDenunciaSchema = z.object({
  alvoEscolha: z.enum(['PERFIL', 'SERVICO']),
  motivo: z
    .string({ error: 'O motivo é obrigatório' })
    .min(1, 'O motivo é obrigatório')
    .max(80, 'Máximo de 80 caracteres'),
  descricao: z.string().max(1000, 'Máximo de 1000 caracteres').optional(),
});

type FormDenunciaValues = z.infer<typeof formDenunciaSchema>;

interface FormDenunciaProps {
  contratacaoId: string;
  servicoId: string;
  profissionalId: string;
  accessToken: string;
}

export function FormDenuncia({ contratacaoId, servicoId, profissionalId, accessToken }: FormDenunciaProps) {
  const router = useRouter();
  const [erroGeral, setErroGeral] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormDenunciaValues>({
    resolver: zodResolver(formDenunciaSchema),
    defaultValues: { alvoEscolha: 'PERFIL', descricao: '' },
  });

  async function aoEnviar(dados: FormDenunciaValues) {
    setErroGeral(null);

    try {
      await criarDenuncia(
        {
          alvoTipo: dados.alvoEscolha,
          alvoId: dados.alvoEscolha === 'PERFIL' ? profissionalId : servicoId,
          motivo: dados.motivo,
          descricao: dados.descricao || undefined,
          denunciadoPerfilId: profissionalId,
          contratacaoId,
        },
        accessToken,
      );
      router.push(`/contratacoes/${contratacaoId}`);
      router.refresh();
    } catch (causa) {
      if (causa instanceof ErroApi) {
        setErroGeral(
          causa.semConexao
            ? 'Não foi possível falar com o servidor. Tente de novo.'
            : (causa.detail ?? 'Não foi possível enviar a denúncia.'),
        );
        return;
      }
      setErroGeral('Não foi possível enviar a denúncia.');
    }
  }

  return (
    <form onSubmit={handleSubmit(aoEnviar)} className="flex flex-col gap-4" noValidate>
      <fieldset className="flex flex-col gap-1.5">
        <legend className="mb-1.5 text-sm font-medium text-texto">O que você quer denunciar?</legend>
        <div className="flex gap-2">
          {ALVOS.map((alvo) => (
            <label
              key={alvo.valor}
              className="flex flex-1 cursor-pointer items-center gap-2 rounded-lg border border-borda px-3 py-2 text-sm has-checked:border-primary has-checked:bg-primary/10"
            >
              <input type="radio" value={alvo.valor} {...register('alvoEscolha')} />
              {alvo.rotulo}
            </label>
          ))}
        </div>
      </fieldset>

      <div className="flex flex-col gap-1.5">
        <Label htmlFor="motivo" obrigatorio>
          Motivo
        </Label>
        <Input
          id="motivo"
          placeholder="Ex.: golpe, cobrança indevida, conteúdo ofensivo"
          erro={Boolean(errors.motivo)}
          {...register('motivo')}
        />
        {errors.motivo && <p className="text-sm text-erro">{errors.motivo.message}</p>}
      </div>

      <div className="flex flex-col gap-1.5">
        <Label htmlFor="descricao">Descrição (opcional)</Label>
        <Textarea id="descricao" rows={4} erro={Boolean(errors.descricao)} {...register('descricao')} />
        {errors.descricao && <p className="text-sm text-erro">{errors.descricao.message}</p>}
      </div>

      {erroGeral && (
        <p role="alert" className="rounded-lg bg-erro/10 px-3 py-2 text-sm text-erro">
          {erroGeral}
        </p>
      )}

      <Botao type="submit" tamanho="grande" carregando={isSubmitting}>
        Enviar denúncia
      </Botao>
    </form>
  );
}
