import { expect, it, vi } from 'vitest';
import { request } from './cliente';
import { buscarMeuOrcamentoMedio, listarPrecosMedios, listarServicos } from './servicos';

vi.mock('./cliente', () => ({ request: vi.fn() }));

it('envia pesquisa, categoria, faixa e localização juntas sem perder um preço mínimo zero', async () => {
  vi.mocked(request).mockResolvedValueOnce({
    conteudo: [], pagina: 2, tamanho: 20, totalElementos: 0, totalPaginas: 0,
  });
  await listarServicos(2, 20, '  São Paulo ', {
    busca: 'elétrica & reformas', categoria: 'eletrica', precoMinimo: '0', precoMaximo: '200.50',
  });
  const [path] = vi.mocked(request).mock.lastCall!;
  const url = new URL(path, 'http://teste.local');
  expect(url.pathname).toBe('/servico');
  expect(Object.fromEntries(url.searchParams)).toEqual({
    pagina: '2', tamanho: '20', cidade: 'São Paulo', busca: 'elétrica & reformas',
    categoria: 'eletrica', precoMinimo: '0', precoMaximo: '200.50',
  });
});

it('mantém as médias do catálogo e do profissional separadas por unidade', async () => {
  const valores = [
    { unidadePreco: 'SERVICO', precoMedio: 200, quantidadeServicos: 2 },
    { unidadePreco: 'HORA', precoMedio: 100, quantidadeServicos: 1 },
  ];
  vi.mocked(request).mockResolvedValueOnce(valores).mockResolvedValueOnce(valores);
  await expect(listarPrecosMedios('eletrica')).resolves.toEqual(valores);
  expect(request).toHaveBeenLastCalledWith('/servico/precos-medios?categoria=eletrica');
  await expect(buscarMeuOrcamentoMedio('token')).resolves.toEqual(valores);
  expect(request).toHaveBeenLastCalledWith('/servico/meus/orcamento-medio', {}, 'token');
});
