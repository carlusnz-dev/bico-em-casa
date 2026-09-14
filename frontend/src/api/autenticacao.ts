import { request } from './cliente';
import {
  CadastroRequest,
  CadastroResponse,
  cadastroResponseSchema,
  LoginRequest,
  LoginResponse,
  loginResponseSchema,
  LogoutResponse,
  logoutResponseSchema,
  RenovarResponse,
  renovarResponseSchema,
} from './contratos/autenticacao';

export async function cadastrar(cadastroRequest: CadastroRequest): Promise<CadastroResponse> {
  const resposta = await request(`/autenticacao/cadastrar`, {
    method: 'POST',
    body: JSON.stringify(cadastroRequest),
  });

  return cadastroResponseSchema.parse(resposta);
}

export async function entrar(loginRequest: LoginRequest): Promise<LoginResponse> {
  const resposta = await request(`/autenticacao/entrar`, {
    method: 'POST',
    body: JSON.stringify(loginRequest),
  });

  return loginResponseSchema.parse(resposta);
}

export async function renovar(): Promise<RenovarResponse> {
  const resposta = await request(`/autenticacao/renovar`, {
    method: 'POST',
  });

  return renovarResponseSchema.parse(resposta);
}

export async function sair(): Promise<LogoutResponse> {
  const resposta = await request(`/autenticacao/sair`, {
    method: 'POST',
  });

  return logoutResponseSchema.parse(resposta);
}
