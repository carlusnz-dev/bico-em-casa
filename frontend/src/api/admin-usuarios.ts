import { useQuery } from '@tanstack/react-query';

import { request } from './cliente';
import {
  contaAdminSchema,
  cadastroAdministradorSchema,
  motivoContaSchema,
  type CadastroAdministrador,
} from './contratos/admin-usuarios';
import { paginaSchema } from './contratos/pagina';

export function useUsuariosAdmin(
  token: string | null,
  autorizado: boolean,
  pagina: number,
  filtros: { busca: string; ativo: string },
) {
  return useQuery({
    queryKey: ['usuarios-admin', token, pagina, filtros],
    enabled: Boolean(token) && autorizado,
    queryFn: async () => {
      const params = new URLSearchParams({ pagina: String(pagina), tamanho: '20' });
      if (filtros.busca) params.set('busca', filtros.busca);
      if (filtros.ativo) params.set('ativo', filtros.ativo);
      return paginaSchema(contaAdminSchema).parse(
        await request(`/admin/usuarios?${params}`, {}, token ?? undefined),
      );
    },
    retry: false,
  });
}

export async function cadastrarAdministrador(dados: CadastroAdministrador, token: string) {
  return contaAdminSchema.parse(await request(
    '/admin/usuarios',
    { method: 'POST', body: JSON.stringify(cadastroAdministradorSchema.parse(dados)) },
    token,
  ));
}

export async function alterarSituacaoConta(
  id: number,
  ativo: boolean,
  motivo: string,
  token: string,
) {
  await request(
    `/admin/usuarios/${id}/${ativo ? 'reativar' : 'suspender'}`,
    { method: 'PATCH', body: JSON.stringify(motivoContaSchema.parse({ motivo })) },
    token,
  );
}
