export interface ProblemDetail {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
  erros?: Record<string, string>;
}

export const STATUS_SEM_CONEXAO = 0;

export class ErroApi extends Error {
  readonly status: number;
  readonly detail?: string;
  readonly errosDeCampo?: Record<string, string>;

  constructor(status: number, detail?: string, errosDeCampo?: Record<string, string>) {
    super(detail ?? 'Erro na API');
    this.name = 'ErroApi';
    this.status = status;
    this.detail = detail;
    this.errosDeCampo = errosDeCampo;
  }

  get semConexao(): boolean {
    return this.status === STATUS_SEM_CONEXAO;
  }

  get naoAutenticado(): boolean {
    return this.status === 401;
  }
}

function ehProblemDetail(corpo: unknown): corpo is ProblemDetail {
  return typeof corpo === 'object' && corpo !== null;
}

export async function erroDaResposta(resposta: Response): Promise<ErroApi> {
  let corpo: unknown;

  try {
    corpo = await resposta.json();
  } catch {
    return new ErroApi(resposta.status, resposta.statusText || 'Erro na API');
  }

  if (!ehProblemDetail(corpo)) {
    return new ErroApi(resposta.status, resposta.statusText || 'Erro na API');
  }

  const problema = corpo as ProblemDetail;

  return new ErroApi(
    problema.status ?? resposta.status,
    problema.detail ?? problema.title ?? resposta.statusText,
    problema.erros,
  );
}

export function erroDeRede(causa: unknown): ErroApi {
  const detalhe =
    causa instanceof Error ? causa.message : 'Não foi possível conectar ao servidor';

  return new ErroApi(STATUS_SEM_CONEXAO, detalhe);
}
