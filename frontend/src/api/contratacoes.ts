import { request } from './cliente';
import {
  Contratacao,
  ContratarServicoRequest,
  contratacaoSchema,
  EditarContratacaoRequest,
} from './contratos/contratacao';
import { paginaSchema, type Pagina } from './contratos/pagina';

export async function contratarServico(
  dados: ContratarServicoRequest,
  accessToken: string,
): Promise<Contratacao> {
  const resposta = await request(
    '/contratacao',
    { method: 'POST', body: JSON.stringify(dados) },
    accessToken,
  );
  return contratacaoSchema.parse(resposta);
}

export async function listarMinhasContratacoes(
  accessToken: string,
  pagina = 0,
  tamanho = 20,
): Promise<Pagina<Contratacao>> {
  const resposta = await request(
    `/contratacao/minhas?pagina=${pagina}&tamanho=${tamanho}`,
    {},
    accessToken,
  );
  return paginaSchema(contratacaoSchema).parse(resposta);
}

export async function buscarContratacaoPorId(
  id: string,
  accessToken: string,
): Promise<Contratacao> {
  const resposta = await request(`/contratacao/${id}`, {}, accessToken);
  return contratacaoSchema.parse(resposta);
}

export async function editarContratacao(
  id: string,
  dados: EditarContratacaoRequest,
  accessToken: string,
): Promise<Contratacao> {
  const resposta = await request(
    `/contratacao/${id}`,
    { method: 'PUT', body: JSON.stringify(dados) },
    accessToken,
  );
  return contratacaoSchema.parse(resposta);
}

export async function arquivarContratacao(id: string, accessToken: string): Promise<Contratacao> {
  const resposta = await request(`/contratacao/${id}/arquivar`, { method: 'PATCH' }, accessToken);
  return contratacaoSchema.parse(resposta);
}

export async function desarquivarContratacao(id: string, accessToken: string): Promise<Contratacao> {
  const resposta = await request(
    `/contratacao/${id}/desarquivar`,
    { method: 'PATCH' },
    accessToken,
  );
  return contratacaoSchema.parse(resposta);
}
