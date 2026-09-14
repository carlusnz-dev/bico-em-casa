import * as z from 'zod';

export const portfolioSchema = z.object({
  id: z.number(),
  perfilId: z.uuid(),
  titulo: z.string(),
  descricao: z.string().nullable(),
  slugUrl: z.string(),
  fotoCapaUrl: z.string().nullable(),
  criadoEm: z.string(),
});

export type Portfolio = z.infer<typeof portfolioSchema>;

export const portfolioRequestSchema = z.object({
  titulo: z.string().min(1, 'Informe um título').max(120, 'Máximo de 120 caracteres'),
  descricao: z.string().max(1000, 'Máximo de 1000 caracteres').optional(),
  slugUrl: z
    .string()
    .min(1, 'Informe um endereço')
    .max(120, 'Máximo de 120 caracteres')
    .regex(/^[a-z0-9-]+$/, 'Use apenas letras minúsculas, números e hífen'),
});

export type PortfolioRequest = z.infer<typeof portfolioRequestSchema>;

export const urlUploadSchema = z.object({
  url: z.string(),
  chave: z.string(),
});

export type UrlUpload = z.infer<typeof urlUploadSchema>;
