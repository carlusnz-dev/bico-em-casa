import * as z from 'zod';

export const PERFIS_DE_CADASTRO = ['CLIENTE', 'PROFISSIONAL'] as const;

export const loginRequestSchema = z.object({
  email: z.email(),
  senha: z.string().min(8).max(50),
});

export const loginResponseSchema = z.object({
  status: z.boolean(),
  accessToken: z.string(),
});

export const logoutResponseSchema = z.object({
  usuarioId: z.number(),
  dataSaida: z.iso.datetime({ offset: true }),
});

export const renovarResponseSchema = z.object({
  accessToken: z.string(),
});

export const cadastroRequestSchema = z.object({
  nomeCompleto: z.string().min(3).max(150),
  nomeUsuario: z
    .string()
    .min(3)
    .max(50)
    .regex(/^[a-z0-9._-]+$/, 'Use apenas letras minúsculas, números, ponto, hífen ou sublinhado'),
  email: z.email(),
  telefone: z.string().min(10).max(20),
  nomeExibicao: z.string().min(2).max(100),
  cadastroTipo: z.enum(PERFIS_DE_CADASTRO),
  cpf: z.string().regex(/^\d{11}$/, 'O CPF precisa ter 11 dígitos, sem pontuação'),
  senha: z.string().min(8).max(50),
  bio: z.string().max(500).optional(),
});

export const cadastroResponseSchema = z.object({
  perfilId: z.uuid(),
  nomeExibicao: z.string(),
  perfilTipo: z.string(),
  criadoEm: z.iso.datetime({ offset: true }),
});

export type LoginRequest = z.infer<typeof loginRequestSchema>;
export type LoginResponse = z.infer<typeof loginResponseSchema>;
export type LogoutResponse = z.infer<typeof logoutResponseSchema>;
export type RenovarResponse = z.infer<typeof renovarResponseSchema>;
export type CadastroRequest = z.infer<typeof cadastroRequestSchema>;
export type CadastroResponse = z.infer<typeof cadastroResponseSchema>;
export type PerfilDeCadastro = (typeof PERFIS_DE_CADASTRO)[number];
