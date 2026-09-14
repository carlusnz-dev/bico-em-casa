import { ErroApi, erroDaResposta, erroDeRede } from './erros';

const BASE_URL = process.env.NEXT_PUBLIC_API_URL;

export async function request(
  path: string,
  opcoes: RequestInit = {},
  accessToken?: string,
): Promise<unknown> {
  const headers = new Headers(opcoes.headers);
  headers.set('Content-Type', 'application/json');
  if (accessToken) {
    headers.set('Authorization', `Bearer ${accessToken}`);
  }

  let resposta: Response;

  try {
    resposta = await fetch(`${BASE_URL}${path}`, {
      ...opcoes,
      credentials: 'include',
      headers,
    });
  } catch (causa) {
    throw erroDeRede(causa);
  }

  if (!resposta.ok) {
    throw await erroDaResposta(resposta);
  }

  if (resposta.status === 204) {
    return undefined;
  }

  return resposta.json();
}

export { ErroApi };
