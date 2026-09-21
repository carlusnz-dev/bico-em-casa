import type { UnidadePreco } from '@/api/contratos/servico';

const LABEL_UNIDADE_PRECO: Record<UnidadePreco, string> = {
  SERVICO: 'por serviço',
  HORA: 'por hora',
  METRO_QUADRADO: 'por m²',
};

export function formatarPreco(precoPrevio: number, unidadePreco: UnidadePreco): string {
  const preco = precoPrevio.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  return `${preco} ${LABEL_UNIDADE_PRECO[unidadePreco]}`;
}
