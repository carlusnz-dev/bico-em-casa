import * as z from 'zod';

export const statusContratacaoSchema = z.enum(['ATIVA', 'ARQUIVADA']);

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
