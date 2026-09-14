import * as z from 'zod';

export const perfilTipoSchema = z.enum(['CLIENTE', 'PROFISSIONAL', 'ADMIN']);

export type PerfilTipo = z.infer<typeof perfilTipoSchema>;

export const perfilSchema = z.object({
  id: z.uuid(),
  usuarioId: z.number(),
  tipo: perfilTipoSchema,
  nomeUsuario: z.string(),
  nomeExibicao: z.string(),
  fotoUrl: z.string().nullable(),
  bio: z.string().nullable(),
  criadoEm: z.string(),
});

export type Perfil = z.infer<typeof perfilSchema>;
