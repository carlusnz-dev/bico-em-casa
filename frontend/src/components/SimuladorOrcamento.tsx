'use client';

import { type FormEvent, useState } from 'react';

import type { ValorMedio } from '@/api/contratos/servico';
import { formatarPreco } from './formatarPreco';
import { Botao } from './ui/Botao';
import { Input } from './ui/Input';
import { Select } from './ui/Select';

export function SimuladorOrcamento({ valores }: { valores: ValorMedio[] }) {
  const [estimativa, setEstimativa] = useState<number | null>(null);

  function calcular(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    const campos = new FormData(evento.currentTarget);
    const valor = valores.find((item) => item.unidadePreco === campos.get('unidade'));
    const quantidade = Number(campos.get('quantidade'));

    if (valor && Number.isFinite(quantidade) && quantidade > 0) {
      setEstimativa(valor.precoMedio * quantidade);
    }
  }

  return (
    <form onSubmit={calcular} onChange={() => setEstimativa(null)} className="mt-3 flex flex-wrap items-end gap-3">
      <label className="flex flex-col gap-1">
        Unidade da estimativa
        <Select name="unidade">
          {valores.map((valor) => (
            <option key={valor.unidadePreco} value={valor.unidadePreco}>
              {formatarPreco(valor.precoMedio, valor.unidadePreco)}
            </option>
          ))}
        </Select>
      </label>
      <label className="flex flex-col gap-1">
        Quantidade
        <Input name="quantidade" type="number" defaultValue="1" min="0.01" step="0.01" required />
      </label>
      <Botao type="submit">Calcular orçamento</Botao>
      <output aria-live="polite" className="basis-full font-semibold">
        {estimativa !== null && `Estimativa total: ${estimativa.toLocaleString('pt-BR', {
          style: 'currency', currency: 'BRL',
        })}`}
      </output>
    </form>
  );
}
