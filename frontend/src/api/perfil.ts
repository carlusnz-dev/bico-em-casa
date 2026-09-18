import { request } from './cliente';
import { Perfil, perfilSchema } from './contratos/perfil';

export async function buscarMeuPerfil(accessToken: string): Promise<Perfil> {
  const resposta = await request('/perfil/me', {}, accessToken);
  return perfilSchema.parse(resposta);
}

export async function buscarPerfilPorId(id: string): Promise<Perfil> {
  const resposta = await request(`/perfil/${id}`);
  return perfilSchema.parse(resposta);
}
