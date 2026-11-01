import { useQuery } from '@tanstack/react-query';
import { z } from 'zod';
import { request } from './cliente';
import { paginaSchema } from './contratos/pagina';
import { tagSchema } from './contratos/tag';
import {
  avisoSchema,
  desativacaoSchema,
  moderacaoSchema,
  servicoAdminSchema,
  type FiltrosAdmin,
  type Moderacao,
} from './contratos/admin-servicos';

export function useServicosAdmin(
  token: string | null,
  autorizado: boolean,
  pagina: number,
  filtros: FiltrosAdmin,
) {
  return useQuery({
    queryKey: ['servicos-admin', token, pagina, filtros],
    enabled: Boolean(token) && autorizado,
    queryFn: async () => {
      const params = new URLSearchParams({
        pagina: String(pagina),
        tamanho: '20',
        busca: filtros.busca,
      });
      if (filtros.ativo) params.set('ativo', filtros.ativo);
      if (filtros.categoria) params.set('categoria', filtros.categoria);
      return paginaSchema(servicoAdminSchema).parse(
        await request(`/admin/servicos?${params}`, {}, token ?? undefined),
      );
    },
    retry: false,
  });
}
export async function moderarServico(id: string, dados: Moderacao, token: string) {
  await request(
    `/admin/servicos/${id}/moderar`,
    { method: 'PUT', body: JSON.stringify(moderacaoSchema.parse(dados)) },
    token,
  );
}
export async function alterarSituacaoAdmin(
  id: string,
  ativo: boolean,
  motivo: string,
  token: string,
) {
  await request(
    `/admin/servicos/${id}/${ativo ? 'reativar' : 'desativar'}`,
    {
      method: 'PATCH',
      body: ativo ? undefined : JSON.stringify(desativacaoSchema.parse({ motivo })),
    },
    token,
  );
}
export async function renomearCategoria(id: string, nome: string, token: string) {
  return tagSchema.parse(
    await request(
      `/admin/categorias/${id}`,
      { method: 'PUT', body: JSON.stringify({ nome }) },
      token,
    ),
  );
}
export async function removerCategoria(id: string, token: string) {
  await request(`/admin/categorias/${id}`, { method: 'DELETE' }, token);
}
export function useAvisosModeracao(token: string | null) {
  return useQuery({
    queryKey: ['avisos-moderacao', token],
    enabled: Boolean(token),
    queryFn: async () =>
      z.array(avisoSchema).parse(await request('/perfil/me/avisos', {}, token ?? undefined)),
  });
}
