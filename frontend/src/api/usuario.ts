import { request } from './cliente';
import { Usuario, usuarioSchema } from './contratos/usuario';

export async function buscarUsuarioPorId(id: number, accessToken: string): Promise<Usuario> {
  const resposta = await request(`/usuario/${id}`, {}, accessToken);
  return usuarioSchema.parse(resposta);
}
