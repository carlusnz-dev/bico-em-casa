import { describe, it, expect, vi, beforeEach } from 'vitest';
import { request } from './cliente';
import { alterarSituacaoAdmin, moderarServico, useServicosAdmin } from './admin-servicos';
vi.mock('./cliente', () => ({ request: vi.fn() }));
vi.mock('@tanstack/react-query', () => ({ useQuery: vi.fn((opcoes: unknown) => opcoes) }));
const token = 'token-de-teste';
const id = 'c4b1cfef-a803-44ba-b385-5f5b52d34298';
const filtros = { busca: 'João & elétrica', ativo: 'false', categoria: id };
beforeEach(() => {
  vi.clearAllMocks();
});
function consulta(autorizado: boolean) {
  return useServicosAdmin(token, autorizado, 2, filtros) as unknown as {
    enabled: boolean;
    queryFn: () => Promise<unknown>;
  };
}
describe('contratos da administração', () => {
  it('não consulta o painel sem autorização', () => {
    expect(consulta(false).enabled).toBe(false);
    expect(request).not.toHaveBeenCalled();
  });
  it('envia a página e codifica busca e filtros sem perder caracteres', async () => {
    vi.mocked(request).mockResolvedValue({
      conteudo: [],
      pagina: 2,
      tamanho: 20,
      totalElementos: 0,
      totalPaginas: 0,
    });
    await consulta(true).queryFn();
    const params = new URL(vi.mocked(request).mock.calls[0]![0], 'http://teste.local').searchParams;
    expect(params.get('pagina')).toBe('2');
    expect(params.get('busca')).toBe(filtros.busca);
    expect(params.get('ativo')).toBe('false');
    expect(params.get('categoria')).toBe(id);
  });
  it('recusa desativação com motivo curto antes de enviar', async () => {
    await expect(alterarSituacaoAdmin(id, false, '  curto  ', token)).rejects.toThrow();
    expect(request).not.toHaveBeenCalled();
  });
  it('recusa correção sem categoria antes de enviar', async () => {
    await expect(
      moderarServico(
        id,
        { titulo: 'Título', descricao: 'Descrição', tagIds: [], motivo: 'Correção' },
        token,
      ),
    ).rejects.toThrow();
    expect(request).not.toHaveBeenCalled();
  });
});
