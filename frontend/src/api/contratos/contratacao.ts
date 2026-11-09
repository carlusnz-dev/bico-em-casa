import * as z from 'zod';

export const statusContratacaoSchema = z.enum(['EM_ANDAMENTO', 'CONCLUIDO', 'CANCELADO']);

export type StatusContratacao = z.infer<typeof statusContratacaoSchema>;

export const contratacaoSchema = z.object({
  id: z.uuid(),
  servicoId: z.uuid(),
  clienteId: z.uuid(),
  profissionalId: z.uuid(),
  tituloServico: z.string(),
  precoServico: z.number(),
  observacao: z.string().nullable(),
  status: statusContratacaoSchema,
  arquivada: z.boolean(),
  criadoEm: z.string(),
});

export type Contratacao = z.infer<typeof contratacaoSchema>;

export const contratarServicoRequestSchema = z.object({
  servicoId: z.uuid(),
  observacao: z.string().max(1000, 'Máximo de 1000 caracteres').optional(),
});

export type ContratarServicoRequest = z.infer<typeof contratarServicoRequestSchema>;

export const editarContratacaoRequestSchema = z.object({
  observacao: z.string().max(1000, 'Máximo de 1000 caracteres').optional(),
});

export type EditarContratacaoRequest = z.infer<typeof editarContratacaoRequestSchema>;

export const alterarStatusContratacaoRequestSchema = z.object({
  status: statusContratacaoSchema,
});

export type AlterarStatusContratacaoRequest = z.infer<typeof alterarStatusContratacaoRequestSchema>;

export const mensagemContratacaoSchema = z.object({
  id: z.uuid(),
  contratacaoId: z.uuid(),
  autorPerfilId: z.uuid(),
  autorNome: z.string(),
  conteudo: z.string(),
  criadoEm: z.string(),
});

export type MensagemContratacao = z.infer<typeof mensagemContratacaoSchema>;

export const enviarMensagemRequestSchema = z.object({
  conteudo: z.string().trim().min(1, 'Digite uma mensagem').max(1000, 'Máximo de 1000 caracteres'),
  autorPerfilId: z.uuid().optional(),
});

export type EnviarMensagemRequest = z.infer<typeof enviarMensagemRequestSchema>;
