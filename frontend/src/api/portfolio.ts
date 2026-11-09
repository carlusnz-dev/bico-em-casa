import { request } from './cliente';
import { paginaSchema, type Pagina } from './contratos/pagina';
import {
  midiaPortfolioSchema,
  midiasPortfolioSchema,
  tipoMidiaPortfolioSchema,
  type MidiaPortfolio,
  Portfolio,
  PortfolioRequest,
  portfolioSchema,
  UrlUpload,
  urlUploadSchema,
} from './contratos/portfolio';

export async function listarPortfolios(pagina = 0, tamanho = 20, cidade = ''): Promise<Pagina<Portfolio>> {
  const parametros = new URLSearchParams({ pagina: String(pagina), tamanho: String(tamanho) });
  if (cidade.trim()) parametros.set('cidade', cidade.trim());
  const resposta = await request(`/portfolio?${parametros}`);
  return paginaSchema(portfolioSchema).parse(resposta);
}

export async function buscarPortfolioPorId(id: number): Promise<Portfolio> {
  const resposta = await request(`/portfolio/${id}`);
  return portfolioSchema.parse(resposta);
}

export async function buscarPortfolioPorPerfil(perfilId: string): Promise<Portfolio> {
  const resposta = await request(`/portfolio/perfil/${perfilId}`);
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

export async function listarMidiasPortfolio(id: number): Promise<MidiaPortfolio[]> {
  return midiasPortfolioSchema.parse(await request(`/portfolio/${id}/midias`));
}

export async function enviarMidiaPortfolio(
  id: number,
  arquivo: File,
  accessToken: string,
): Promise<MidiaPortfolio> {
  const contentType = tipoMidiaPortfolioSchema.parse(arquivo.type);
  const limite = contentType.startsWith('video/') ? 25 * 1024 * 1024 : 5 * 1024 * 1024;
  if (arquivo.size === 0 || arquivo.size > limite) {
    throw new Error('Selecione uma imagem de até 5 MB ou um vídeo de até 25 MB.');
  }

  const base = `/portfolio/${id}/midias`;
  const { url, chave } = urlUploadSchema.parse(await request(
    `${base}/upload`,
    { method: 'POST', body: JSON.stringify({ contentType }) },
    accessToken,
  ));

  const upload = await fetch(url, {
    method: 'PUT',
    headers: { 'Content-Type': contentType },
    body: arquivo,
  });
  if (!upload.ok) throw new Error('Não foi possível enviar o arquivo para o armazenamento.');

  return midiaPortfolioSchema.parse(await request(
    `${base}/confirmar`,
    { method: 'POST', body: JSON.stringify({ chave, contentType }) },
    accessToken,
  ));
}

export async function removerMidiaPortfolio(
  id: number,
  midiaId: string,
  accessToken: string,
): Promise<void> {
  await request(`/portfolio/${id}/midias/${midiaId}`, { method: 'DELETE' }, accessToken);
}
