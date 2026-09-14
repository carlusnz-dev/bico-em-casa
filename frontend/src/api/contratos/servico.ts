import * as z from 'zod';

export const unidadePrecoSchema = z.enum(['SERVICO', 'HORA', 'METRO_QUADRADO']);

export type UnidadePreco = z.infer<typeof unidadePrecoSchema>;

export const servicoSchema = z.object({
  id: z.uuid(),
  perfilId: z.uuid(),
  titulo: z.string(),
  descricao: z.string(),
  precoPrevio: z.number(),
  unidadePreco: unidadePrecoSchema,
  ativo: z.boolean(),
  tagIds: z.array(z.uuid()),
  criadoEm: z.string(),
});

export type Servico = z.infer<typeof servicoSchema>;
