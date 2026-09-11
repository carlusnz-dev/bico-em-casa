import { request } from './cliente';
import { Usuario, usuarioSchema } from './contratos/usuario';

export async function buscarUsuarioPorId(id: number, acessToken: string): Promise<Usuario> {
  const resposta = await request(`/usuario/${id}`, {}, acessToken);
  return usuarioSchema.parse(resposta);
}
