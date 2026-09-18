import * as z from 'zod';

export function paginaSchema<T extends z.ZodType>(itemSchema: T) {
  return z.object({
    conteudo: z.array(itemSchema),
    pagina: z.number(),
    tamanho: z.number(),
    totalElementos: z.number(),
    totalPaginas: z.number(),
  });
}

export type Pagina<T> = {
  conteudo: T[];
  pagina: number;
  tamanho: number;
  totalElementos: number;
  totalPaginas: number;
};
