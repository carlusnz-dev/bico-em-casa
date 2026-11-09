import { z } from 'zod';
import { perfilTipoSchema } from './perfil';

export const contaAdminSchema = z.object({
  id: z.number(),
  nome: z.string(),
  email: z.email(),
  ativo: z.boolean(),
  papeis: z.array(perfilTipoSchema),
  criadoEm: z.string().nullable(),
});
export type ContaAdmin = z.infer<typeof contaAdminSchema>;

export const cadastroAdministradorSchema = z.object({
  nomeCompleto: z.string().trim().min(3, 'Informe o nome completo').max(150),
  nomeUsuario: z.string().trim().min(3).max(50)
    .regex(/^[a-z0-9._-]+$/, 'Use letras minúsculas, números, ponto, hífen ou sublinhado'),
  nomeExibicao: z.string().trim().min(2, 'Informe o nome de exibição').max(100),
  email: z.email('Informe um e-mail válido').max(255),
  cpf: z.string().trim().regex(/^\d{11}$/, 'Informe 11 dígitos de CPF'),
  senha: z.string().min(8, 'A senha precisa de pelo menos 8 caracteres').max(50),
});
export type CadastroAdministrador = z.infer<typeof cadastroAdministradorSchema>;

export const motivoContaSchema = z.object({
  motivo: z.string().trim().min(10, 'Informe um motivo com pelo menos 10 caracteres').max(1000),
});
