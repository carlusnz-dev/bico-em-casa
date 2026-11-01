import { z } from 'zod';
import { servicoSchema } from './servico';

export const servicoAdminSchema = z.object({ servico: servicoSchema, profissional: z.string() });
export type ServicoAdmin = z.infer<typeof servicoAdminSchema>;
export const filtrosAdminSchema = z.object({
  busca: z.string(),
  ativo: z.string(),
  categoria: z.string(),
});
export type FiltrosAdmin = z.infer<typeof filtrosAdminSchema>;
export const moderacaoSchema = z.object({
  titulo: z.string().trim().min(1).max(120),
  descricao: z.string().trim().min(1).max(1000),
  tagIds: z.array(z.uuid()).min(1, 'Selecione ao menos uma categoria'),
  motivo: z.string().trim().min(1, 'Informe o motivo').max(1000),
});
export type Moderacao = z.infer<typeof moderacaoSchema>;
export const desativacaoSchema = z.object({
  motivo: z.string().trim().min(10, 'Informe um motivo com pelo menos 10 caracteres').max(1000),
});
export const avisoSchema = z.object({ id: z.uuid(), mensagem: z.string(), alvoId: z.string() });
