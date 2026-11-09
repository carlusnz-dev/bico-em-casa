import type { ValorMedio } from '@/api/contratos/servico';
import { formatarPreco } from './formatarPreco';

export function ListaValoresMedios({ valores }: { valores: ValorMedio[] }) {
  return (
    <ul className="flex flex-wrap gap-x-6 gap-y-2">
      {valores.map((valor) => (
        <li key={valor.unidadePreco}>
          <strong>{formatarPreco(valor.precoMedio, valor.unidadePreco)}</strong>
          <span className="ml-2 text-sm text-texto-suave">
            ({valor.quantidadeServicos} {valor.quantidadeServicos === 1 ? 'oferta' : 'ofertas'})
          </span>
        </li>
      ))}
    </ul>
  );
}
