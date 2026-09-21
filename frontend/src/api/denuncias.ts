import { request } from './cliente';
import { Denuncia, DenunciaRequest, denunciaSchema } from './contratos/denuncia';

export async function criarDenuncia(dados: DenunciaRequest, accessToken: string): Promise<Denuncia> {
  const resposta = await request(
    '/denuncia',
    { method: 'POST', body: JSON.stringify(dados) },
    accessToken,
  );
  return denunciaSchema.parse(resposta);
}
