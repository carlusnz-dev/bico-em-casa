'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';

import { criarServico, editarServico } from '@/api/servicos';
import {
  Servico,
  ServicoRequest,
  servicoRequestSchema,
  UnidadePreco,
} from '@/api/contratos/servico';
import type { Tag } from '@/api/contratos/tag';
import { ErroApi } from '@/api/erros';
import { Botao } from '@/components/ui/Botao';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Select } from '@/components/ui/Select';
import { Textarea } from '@/components/ui/Textarea';
import { useSessao } from '@/hooks/useSessao';

const UNIDADES: Array<{ valor: UnidadePreco; rotulo: string }> = [
  { valor: 'SERVICO', rotulo: 'Por serviço' },
  { valor: 'HORA', rotulo: 'Por hora' },
  { valor: 'METRO_QUADRADO', rotulo: 'Por metro quadrado' },
];

export function FormServico({ tags, servico }: { tags: Tag[]; servico?: Servico }) {
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
  } = useForm<ServicoRequest>({
    resolver: zodResolver(servicoRequestSchema),
    defaultValues: servico
      ? {
          titulo: servico.titulo,
          descricao: servico.descricao,
          precoPrevio: servico.precoPrevio,
          unidadePreco: servico.unidadePreco,
          tagIds: servico.tagIds,
        }
      : { unidadePreco: 'SERVICO', tagIds: [] },
  });

  async function aoEnviar(dados: ServicoRequest) {
    setErroGeral(null);

    if (!accessToken) {
      setErroGeral('Sua sessão expirou. Entre novamente.');
      return;
    }

    try {
      if (servico) {
        await editarServico(servico.id, dados, accessToken);
      } else {
        await criarServico(dados, accessToken);
      }
      router.push('/servicos/meus');
      router.refresh();
    } catch (causa) {
      if (causa instanceof ErroApi) {
        setErroGeral(
          causa.semConexao
            ? 'Não foi possível falar com o servidor. Tente de novo.'
            : (causa.detail ?? 'Não foi possível salvar o serviço.'),
        );
        return;
      }
      setErroGeral('Não foi possível salvar o serviço.');
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
        <Label htmlFor="descricao" obrigatorio>
          Descrição
        </Label>
        <Textarea
          id="descricao"
          rows={4}
          erro={Boolean(errors.descricao)}
          {...register('descricao')}
        />
        {errors.descricao && <p className="text-sm text-erro">{errors.descricao.message}</p>}
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        <div className="flex flex-col gap-1.5">
          <Label htmlFor="precoPrevio" obrigatorio>
            Preço de referência
          </Label>
          <Input
            id="precoPrevio"
            type="number"
            step="0.01"
            min="0"
            inputMode="decimal"
            erro={Boolean(errors.precoPrevio)}
            {...register('precoPrevio', { valueAsNumber: true })}
          />
          {errors.precoPrevio && (
            <p className="text-sm text-erro">{errors.precoPrevio.message}</p>
          )}
        </div>

        <div className="flex flex-col gap-1.5">
          <Label htmlFor="unidadePreco" obrigatorio>
            Unidade de preço
          </Label>
          <Select
            id="unidadePreco"
            erro={Boolean(errors.unidadePreco)}
            {...register('unidadePreco')}
          >
            {UNIDADES.map((unidade) => (
              <option key={unidade.valor} value={unidade.valor}>
                {unidade.rotulo}
              </option>
            ))}
          </Select>
        </div>
      </div>

      <fieldset className="flex flex-col gap-1.5">
        <legend className="text-sm font-medium text-texto">
          Categorias<span className="ml-0.5 text-erro">*</span>
        </legend>
        <div className="flex flex-wrap gap-2">
          {tags.map((tag) => (
            <label
              key={tag.id}
              className="flex cursor-pointer items-center gap-2 rounded-lg border border-borda px-3 py-2 text-sm has-checked:border-primary has-checked:bg-primary/10"
            >
              <input type="checkbox" value={tag.id} {...register('tagIds')} />
              {tag.nome}
            </label>
          ))}
        </div>
        {errors.tagIds && <p className="text-sm text-erro">{errors.tagIds.message}</p>}
      </fieldset>

      {erroGeral && (
        <p role="alert" className="rounded-lg bg-erro/10 px-3 py-2 text-sm text-erro">
          {erroGeral}
        </p>
      )}

      <Botao type="submit" tamanho="grande" carregando={isSubmitting}>
        {servico ? 'Salvar alterações' : 'Publicar serviço'}
      </Botao>
    </form>
  );
}
