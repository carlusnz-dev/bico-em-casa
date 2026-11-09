import { beforeEach, describe, expect, it, vi } from 'vitest';
import { alterarSituacaoConta, cadastrarAdministrador, useUsuariosAdmin } from './admin-usuarios';
import { request } from './cliente';

vi.mock('./cliente', () => ({ request: vi.fn() }));
vi.mock('@tanstack/react-query', () => ({ useQuery: vi.fn((opcoes: unknown) => opcoes) }));

const token = 'token-de-teste';
const cadastro = {
  nomeCompleto: 'Ana Costa',
  nomeUsuario: 'ana.admin',
  nomeExibicao: 'Ana',
  email: 'ana@example.com',
  cpf: '12345678909',
  senha: 'senha-inicial',
};
const conta = {
  id: 42, nome: cadastro.nomeCompleto, email: cadastro.email,
  ativo: true, papeis: ['ADMIN'], criadoEm: null,
};

beforeEach(() => vi.resetAllMocks());

describe('contas administrativas', () => {
  it('só habilita a consulta com autorização e preserva filtros de contas suspensas', async () => {
    const filtros = { busca: 'João & Filhos', ativo: 'false' };
    const consulta = (autorizado: boolean) => useUsuariosAdmin(token, autorizado, 1, filtros) as unknown as {
      enabled: boolean;
      queryFn: () => Promise<unknown>;
    };
    expect(consulta(false).enabled).toBe(false);
    expect(consulta(true).enabled).toBe(true);
    vi.mocked(request).mockResolvedValue({
      conteudo: [conta], pagina: 1, tamanho: 20, totalElementos: 21, totalPaginas: 2,
    });
    await expect(consulta(true).queryFn()).resolves.toMatchObject({ conteudo: [conta] });
    const path = vi.mocked(request).mock.calls[0]![0];
    const url = new URL(path, 'http://teste.local');
    expect(url.pathname).toBe('/admin/usuarios');
    expect(url.searchParams.get('busca')).toBe(filtros.busca);
    expect(url.searchParams.get('ativo')).toBe('false');
    expect(url.searchParams.get('pagina')).toBe('1');
  });

  it('cria um administrador no endpoint autenticado sem enviar um papel controlado pelo formulário', async () => {
    vi.mocked(request).mockResolvedValue(conta);
    await expect(cadastrarAdministrador(cadastro, token)).resolves.toEqual(conta);
    expect(request).toHaveBeenCalledWith(
      '/admin/usuarios',
      { method: 'POST', body: JSON.stringify(cadastro) },
      token,
    );
  });

  it('exige motivo válido tanto para suspender quanto para reativar', async () => {
    await expect(alterarSituacaoConta(42, false, 'curto', token)).rejects.toThrow();
    expect(request).not.toHaveBeenCalled();
    const corpo = { method: 'PATCH', body: JSON.stringify({ motivo: 'Solicitação confirmada' }) };
    await alterarSituacaoConta(42, false, '  Solicitação confirmada  ', token);
    await alterarSituacaoConta(42, true, 'Solicitação confirmada', token);
    expect(request).toHaveBeenNthCalledWith(1, '/admin/usuarios/42/suspender', corpo, token);
    expect(request).toHaveBeenNthCalledWith(2, '/admin/usuarios/42/reativar', corpo, token);
  });
});
