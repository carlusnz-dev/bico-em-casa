import { request } from './cliente';
import { paginaSchema, type Pagina } from './contratos/pagina';
import { Servico, ServicoRequest, servicoSchema, valoresMediosSchema } from './contratos/servico';

export interface FiltrosCatalogo {
  busca?: string;
  categoria?: string;
  precoMinimo?: string;
  precoMaximo?: string;
}

export async function listarServicos(
  pagina = 0,
  tamanho = 20,
  cidade?: string,
  filtros: FiltrosCatalogo = {},
): Promise<Pagina<Servico>> {
  const parametros = new URLSearchParams({
    pagina: String(pagina),
    tamanho: String(tamanho),
  });
  if (cidade?.trim()) {
    parametros.set('cidade', cidade.trim());
  }
  for (const [nome, valor] of Object.entries(filtros)) {
    if (valor) parametros.set(nome, valor);
  }

  const resposta = await request(`/servico?${parametros.toString()}`);
  return paginaSchema(servicoSchema).parse(resposta);
}

export async function buscarServicoPorId(id: string): Promise<Servico> {
  const resposta = await request(`/servico/${id}`);
  return servicoSchema.parse(resposta);
}

export async function buscarMeuServico(id: string, accessToken: string): Promise<Servico> {
  return servicoSchema.parse(await request(`/servico/meus/${id}`, {}, accessToken));
}

export async function listarPrecosMedios(categoria: string) {
  const parametros = new URLSearchParams({ categoria });
  return valoresMediosSchema.parse(await request(`/servico/precos-medios?${parametros}`));
}

export async function buscarMeuOrcamentoMedio(accessToken: string) {
  return valoresMediosSchema.parse(await request('/servico/meus/orcamento-medio', {}, accessToken));
}

export async function reenviarServico(id: string, dados: ServicoRequest, accessToken: string) {
  return servicoSchema.parse(await request(
    `/servico/${id}/reenviar`,
    { method: 'PUT', body: JSON.stringify(dados) },
    accessToken,
  ));
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

export async function deletarServico(id: string, accessToken: string): Promise<void> {
  await request(`/servico/${id}`, { method: 'DELETE' }, accessToken);
}
