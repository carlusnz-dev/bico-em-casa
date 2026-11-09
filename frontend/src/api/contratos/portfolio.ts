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

export const tipoMidiaPortfolioSchema = z.enum([
  'image/jpeg', 'image/png', 'image/webp', 'video/mp4', 'video/webm',
], { error: 'Selecione uma imagem JPEG, PNG ou WebP, ou um vídeo MP4 ou WebM.' });

export const midiaPortfolioSchema = z.object({
  id: z.uuid(),
  url: z.string(),
  contentType: tipoMidiaPortfolioSchema,
});

export const midiasPortfolioSchema = z.array(midiaPortfolioSchema);
export type MidiaPortfolio = z.infer<typeof midiaPortfolioSchema>;
