import * as z from 'zod';

export const usuarioSchema = z.object({
  id: z.number(),
  nome: z.string(),
  email: z.email(),
});

export type Usuario = z.infer<typeof usuarioSchema>;
