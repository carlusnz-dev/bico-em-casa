import { request } from './cliente';
import { paginaSchema, type Pagina } from './contratos/pagina';
import { Servico, servicoSchema } from './contratos/servico';

export async function listarServicos(pagina = 0, tamanho = 20): Promise<Pagina<Servico>> {
  const resposta = await request(`/servico?pagina=${pagina}&tamanho=${tamanho}`);
  return paginaSchema(servicoSchema).parse(resposta);
}

export async function buscarServicoPorId(id: string): Promise<Servico> {
  const resposta = await request(`/servico/${id}`);
  return servicoSchema.parse(resposta);
}
