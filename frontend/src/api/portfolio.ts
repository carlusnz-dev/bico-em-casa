import { request } from './cliente';
import { paginaSchema, type Pagina } from './contratos/pagina';
import {
  Portfolio,
  PortfolioRequest,
  portfolioSchema,
  UrlUpload,
  urlUploadSchema,
} from './contratos/portfolio';

export async function listarPortfolios(pagina = 0, tamanho = 20): Promise<Pagina<Portfolio>> {
  const resposta = await request(`/portfolio?pagina=${pagina}&tamanho=${tamanho}`);
  return paginaSchema(portfolioSchema).parse(resposta);
}

export async function buscarPortfolioPorId(id: number): Promise<Portfolio> {
  const resposta = await request(`/portfolio/${id}`);
  return portfolioSchema.parse(resposta);
}

export async function buscarMeuPortfolio(accessToken: string): Promise<Portfolio> {
  const resposta = await request('/portfolio/meu', {}, accessToken);
  return portfolioSchema.parse(resposta);
}

export async function criarPortfolio(
  dados: PortfolioRequest,
  accessToken: string,
): Promise<Portfolio> {
  const resposta = await request(
    '/portfolio',
    { method: 'POST', body: JSON.stringify(dados) },
    accessToken,
  );
  return portfolioSchema.parse(resposta);
}

export async function editarPortfolio(
  id: number,
  dados: PortfolioRequest,
  accessToken: string,
): Promise<Portfolio> {
  const resposta = await request(
    `/portfolio/${id}`,
    { method: 'PUT', body: JSON.stringify(dados) },
    accessToken,
  );
  return portfolioSchema.parse(resposta);
}

async function solicitarUploadFotoCapa(
  id: number,
  contentType: string,
  accessToken: string,
): Promise<UrlUpload> {
  const resposta = await request(
    `/portfolio/${id}/foto-capa/upload`,
    { method: 'POST', body: JSON.stringify({ contentType }) },
    accessToken,
  );
  return urlUploadSchema.parse(resposta);
}

async function confirmarFotoCapa(
  id: number,
  chave: string,
  accessToken: string,
): Promise<Portfolio> {
  const resposta = await request(
    `/portfolio/${id}/foto-capa/confirmar`,
    { method: 'POST', body: JSON.stringify({ chave }) },
    accessToken,
  );
  return portfolioSchema.parse(resposta);
}

export async function enviarFotoCapa(
  id: number,
  arquivo: File,
  accessToken: string,
): Promise<Portfolio> {
  const { url, chave } = await solicitarUploadFotoCapa(id, arquivo.type, accessToken);

  const upload = await fetch(url, {
    method: 'PUT',
    headers: { 'Content-Type': arquivo.type },
    body: arquivo,
  });

  if (!upload.ok) {
    throw new Error('Não foi possível enviar a imagem para o armazenamento');
  }

  return confirmarFotoCapa(id, chave, accessToken);
}
