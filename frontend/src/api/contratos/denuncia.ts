import * as z from 'zod';

export const tipoAlvoDenunciaSchema = z.enum(['SERVICO', 'PERFIL', 'AVALIACAO']);

export type TipoAlvoDenuncia = z.infer<typeof tipoAlvoDenunciaSchema>;

export const statusDenunciaSchema = z.enum(['PENDENTE', 'PROCEDENTE', 'IMPROCEDENTE']);

export type StatusDenuncia = z.infer<typeof statusDenunciaSchema>;

export const denunciaSchema = z.object({
  id: z.uuid(),
  autorPerfilId: z.uuid(),
  denunciadoPerfilId: z.uuid().nullable(),
  contratacaoId: z.uuid().nullable(),
  alvoTipo: tipoAlvoDenunciaSchema,
  alvoId: z.string(),
  motivo: z.string(),
  descricao: z.string().nullable(),
  status: statusDenunciaSchema,
  analisadoPorPerfilId: z.uuid().nullable(),
  analisadoEm: z.string().nullable(),
  parecer: z.string().nullable(),
  criadoEm: z.string(),
  atualizadoEm: z.string(),
});

export type Denuncia = z.infer<typeof denunciaSchema>;

export const denunciaRequestSchema = z.object({
  alvoTipo: tipoAlvoDenunciaSchema,
  alvoId: z.uuid(),
  motivo: z
    .string({ error: 'O motivo é obrigatório' })
    .min(1, 'O motivo é obrigatório')
    .max(80, 'Máximo de 80 caracteres'),
  descricao: z.string().max(1000, 'Máximo de 1000 caracteres').optional(),
  denunciadoPerfilId: z.uuid().optional(),
  contratacaoId: z.uuid().optional(),
});

export type DenunciaRequest = z.infer<typeof denunciaRequestSchema>;
