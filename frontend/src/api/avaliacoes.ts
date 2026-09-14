import * as z from 'zod';

import { request } from './cliente';
import { Avaliacao, AvaliacaoRequest, avaliacaoSchema } from './contratos/avaliacao';

export async function criarAvaliacao(
  contratacaoId: string,
  dados: AvaliacaoRequest,
  accessToken: string,
): Promise<Avaliacao> {
  const resposta = await request(
    `/avaliacao/${contratacaoId}`,
    { method: 'POST', body: JSON.stringify(dados) },
    accessToken,
  );
  return avaliacaoSchema.parse(resposta);
}

export async function buscarAvaliacaoPorId(id: string): Promise<Avaliacao> {
  const resposta = await request(`/avaliacao/${id}`);
  return avaliacaoSchema.parse(resposta);
}

export async function listarAvaliacoesRecebidas(avaliadoPerfilId: string): Promise<Avaliacao[]> {
  const resposta = await request(`/avaliacao/avaliado/${avaliadoPerfilId}`);
  return z.array(avaliacaoSchema).parse(resposta);
}

export async function listarAvaliacoesFeitas(autorPerfilId: string): Promise<Avaliacao[]> {
  const resposta = await request(`/avaliacao/autor/${autorPerfilId}`);
  return z.array(avaliacaoSchema).parse(resposta);
}

export async function alterarAvaliacao(
  id: string,
  dados: AvaliacaoRequest,
  accessToken: string,
): Promise<Avaliacao> {
  const resposta = await request(
    `/avaliacao/${id}`,
    { method: 'PUT', body: JSON.stringify(dados) },
    accessToken,
  );
  return avaliacaoSchema.parse(resposta);
}

export async function deletarAvaliacao(id: string, accessToken: string): Promise<void> {
  await request(`/avaliacao/${id}`, { method: 'DELETE' }, accessToken);
}
