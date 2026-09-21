import { request } from './cliente';
import {
  type Denuncia,
  type DenunciaContraMim,
  denunciaContraMimSchema,
  type DenunciaRequest,
  denunciaSchema,
} from './contratos/denuncia';
import { paginaSchema } from './contratos/pagina';
import type { Pagina } from './contratos/pagina';
export async function criarDenuncia(dados: DenunciaRequest, accessToken: string): Promise<Denuncia> {
  const resposta = await request(
    '/denuncia',
    { method: 'POST', body: JSON.stringify(dados) },
    accessToken,
  );
  return denunciaSchema.parse(resposta);
}

export async function listarDenunciasContraMim(accessToken:string , pagina = 0 , tamanho = 20 ): Promise<Pagina<DenunciaContraMim>>{
  const resposta = await request(`/denuncia/contra-mim?pagina=${pagina}&tamanho=${tamanho}`, {}, accessToken)
  return paginaSchema(denunciaContraMimSchema).parse(resposta)
}
