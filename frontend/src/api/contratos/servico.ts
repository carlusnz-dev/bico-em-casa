import * as z from 'zod';

export const unidadePrecoSchema = z.enum(['SERVICO', 'HORA', 'METRO_QUADRADO']);

export type UnidadePreco = z.infer<typeof unidadePrecoSchema>;

export const statusRevisaoSchema = z.enum(['PENDENTE', 'APROVADO', 'REPROVADO']);

export const valoresMediosSchema = z.array(z.object({
  unidadePreco: unidadePrecoSchema,
  precoMedio: z.number(),
  quantidadeServicos: z.number().int().positive(),
}));
export type ValorMedio = z.infer<typeof valoresMediosSchema>[number];

export const servicoSchema = z.object({
  id: z.uuid(),
  perfilId: z.uuid(),
  titulo: z.string(),
  descricao: z.string(),
  precoPrevio: z.number(),
  unidadePreco: unidadePrecoSchema,
  ativo: z.boolean(),
  motivoModeracao: z.string().nullable().optional(),
  statusRevisao: statusRevisaoSchema.nullish(),
  motivoReprovacao: z.string().nullish(),
  tagIds: z.array(z.uuid()),
  criadoEm: z.string(),
});

export type Servico = z.infer<typeof servicoSchema>;

export const servicoRequestSchema = z.object({
  titulo: z.string().min(1, 'Informe um título').max(120, 'Máximo de 120 caracteres'),
  descricao: z.string().min(1, 'Informe uma descrição').max(1000, 'Máximo de 1000 caracteres'),
  precoPrevio: z.number().min(0, 'Informe um preço válido'),
  unidadePreco: unidadePrecoSchema,
  tagIds: z.array(z.uuid()).min(1, 'Selecione ao menos uma categoria'),
});

export type ServicoRequest = z.infer<typeof servicoRequestSchema>;
