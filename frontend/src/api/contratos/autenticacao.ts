import * as z from 'zod';

export const loginRequestSchema = z.object({
  email: z.email(),
  senha: z.string().min(8).max(50),
});

export const loginResponseSchema = z.object({
  status: z.boolean(),
  accessToken: z.string(),
});

export const logoutResponseSchema = z.object({
  usuarioId: z.number(),
  dataSaida: z.iso.datetime({ offset: true }),
});

export type LoginRequest = z.infer<typeof loginRequestSchema>;
export type LoginResponse = z.infer<typeof loginResponseSchema>;
export type LogoutResponse = z.infer<typeof logoutResponseSchema>;
