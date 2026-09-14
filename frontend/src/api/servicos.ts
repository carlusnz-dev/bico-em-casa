import { request } from './cliente';
import { paginaSchema, type Pagina } from './contratos/pagina';
import { Servico, ServicoRequest, servicoSchema } from './contratos/servico';

export async function listarServicos(pagina = 0, tamanho = 20): Promise<Pagina<Servico>> {
  const resposta = await request(`/servico?pagina=${pagina}&tamanho=${tamanho}`);
  return paginaSchema(servicoSchema).parse(resposta);
}

export async function buscarServicoPorId(id: string): Promise<Servico> {
  const resposta = await request(`/servico/${id}`);
  return servicoSchema.parse(resposta);
}

export async function listarMeusServicos(
  accessToken: string,
  pagina = 0,
  tamanho = 20,
): Promise<Pagina<Servico>> {
  const resposta = await request(
    `/servico/meus?pagina=${pagina}&tamanho=${tamanho}`,
    {},
    accessToken,
  );
  return paginaSchema(servicoSchema).parse(resposta);
}

export async function criarServico(dados: ServicoRequest, accessToken: string): Promise<Servico> {
  const resposta = await request(
    '/servico',
    { method: 'POST', body: JSON.stringify(dados) },
    accessToken,
  );
  return servicoSchema.parse(resposta);
}

export async function editarServico(
  id: string,
  dados: ServicoRequest,
  accessToken: string,
): Promise<Servico> {
  const resposta = await request(
    `/servico/${id}`,
    { method: 'PUT', body: JSON.stringify(dados) },
    accessToken,
  );
  return servicoSchema.parse(resposta);
}

export async function ativarServico(id: string, accessToken: string): Promise<Servico> {
  const resposta = await request(`/servico/${id}/ativar`, { method: 'PATCH' }, accessToken);
  return servicoSchema.parse(resposta);
}

export async function desativarServico(id: string, accessToken: string): Promise<Servico> {
  const resposta = await request(`/servico/${id}/desativar`, { method: 'PATCH' }, accessToken);
  return servicoSchema.parse(resposta);
}
