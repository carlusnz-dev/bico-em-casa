import * as z from 'zod';

export const tagSchema = z.object({
  id: z.uuid(),
  nome: z.string(),
  slug: z.string(),
});

export type Tag = z.infer<typeof tagSchema>;
