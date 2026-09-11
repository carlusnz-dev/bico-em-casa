const BASE_URL = process.env.NEXT_PUBLIC_API_URL;

interface ProblemDetail {
  title: string;
  status: number;
  detail?: string;
}

export class ErroApi extends Error {
  constructor(
    public status: number,
    public detail?: string,
  ) {
    super(detail ?? 'Erro na API');
  }
}

export async function request(
  path: string,
  opcoes: RequestInit = {},
  acessToken?: string,
): Promise<unknown> {
  const headers = new Headers(opcoes.headers);
  headers.set('Content-Type', 'application/json');
  if (acessToken) {
    headers.set('Authorization', `Bearer ${acessToken}`);
  }

  const resposta = await fetch(`${BASE_URL}${path}`, {
    ...opcoes,
    credentials: 'include',
    headers,
  });

  if (!resposta.ok) {
    const problema: ProblemDetail = await resposta.json();
    throw new ErroApi(problema.status, problema.detail);
  }

  if (resposta.status === 204) {
    return undefined;
  }

  return resposta.json();
}
