import * as z from 'zod';

export const avaliacaoSchema = z.object({
  id: z.uuid(),
  contratacaoId: z.uuid(),
  autorPerfilId: z.uuid(),
  avaliadoPerfilId: z.uuid(),
  nota: z.number().int().min(1).max(5),
  comentario: z.string().nullable(),
  criadoEm: z.string(),
  atualizadoEm: z.string(),
});

export type Avaliacao = z.infer<typeof avaliacaoSchema>;

export const avaliacaoRequestSchema = z.object({
  nota: z
    .number({ error: 'É necessário informar uma nota' })
    .int()
    .min(1, 'A nota deve ser no mínimo 1')
    .max(5, 'A nota deve ser no máximo 5'),
  comentario: z.string().max(1000, 'Máximo de 1000 caracteres').optional(),
});

export type AvaliacaoRequest = z.infer<typeof avaliacaoRequestSchema>;
