'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { useForm } from 'react-hook-form';

import { criarAvaliacao } from '@/api/avaliacoes';
import { AvaliacaoRequest, avaliacaoRequestSchema } from '@/api/contratos/avaliacao';
import { ErroApi } from '@/api/erros';
import { Botao } from '@/components/ui/Botao';
import { Label } from '@/components/ui/Label';
import { Textarea } from '@/components/ui/Textarea';

const NOTAS = [1, 2, 3, 4, 5] as const;

export function FormAvaliacao({ contratacaoId, accessToken }: { contratacaoId: string; accessToken: string }) {
  const router = useRouter();
  const [erroGeral, setErroGeral] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { isSubmitting },
  } = useForm<AvaliacaoRequest>({
    resolver: zodResolver(avaliacaoRequestSchema),
    defaultValues: { nota: 5, comentario: '' },
  });

  const notaEscolhida = watch('nota');

  async function aoEnviar(dados: AvaliacaoRequest) {
    setErroGeral(null);

    try {
      await criarAvaliacao(contratacaoId, dados, accessToken);
      router.push(`/contratacoes/${contratacaoId}`);
      router.refresh();
    } catch (causa) {
      if (causa instanceof ErroApi) {
        setErroGeral(
          causa.semConexao
            ? 'Não foi possível falar com o servidor. Tente de novo.'
            : (causa.detail ?? 'Não foi possível enviar a avaliação.'),
        );
        return;
      }
      setErroGeral('Não foi possível enviar a avaliação.');
    }
  }

  return (
    <form onSubmit={handleSubmit(aoEnviar)} className="flex flex-col gap-4" noValidate>
      <div className="flex flex-col gap-1.5">
        <Label htmlFor="nota">Nota</Label>
        <div className="flex gap-2" role="radiogroup" aria-label="Nota">
          {NOTAS.map((nota) => (
            <button
              key={nota}
              type="button"
              aria-pressed={notaEscolhida === nota}
              onClick={() => setValue('nota', nota)}
              className={
                notaEscolhida === nota
                  ? 'flex h-10 w-10 items-center justify-center rounded-lg bg-primary font-medium text-white'
                  : 'flex h-10 w-10 items-center justify-center rounded-lg border border-borda text-texto-suave hover:bg-borda/40'
              }
            >
              {nota}
            </button>
          ))}
        </div>
        <input type="hidden" {...register('nota', { valueAsNumber: true })} />
      </div>

      <div className="flex flex-col gap-1.5">
        <Label htmlFor="comentario">Comentário (opcional)</Label>
        <Textarea id="comentario" rows={4} {...register('comentario')} />
      </div>

      {erroGeral && (
        <p role="alert" className="rounded-lg bg-erro/10 px-3 py-2 text-sm text-erro">
          {erroGeral}
        </p>
      )}

      <Botao type="submit" tamanho="grande" carregando={isSubmitting}>
        Enviar avaliação
      </Botao>
    </form>
  );
}
