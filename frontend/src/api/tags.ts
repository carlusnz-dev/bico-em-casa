import * as z from 'zod';
import { useQuery } from '@tanstack/react-query';

import { request } from './cliente';
import { Tag, tagSchema } from './contratos/tag';

export async function listarTags(): Promise<Tag[]> {
  const resposta = await request('/tag');
  return z.array(tagSchema).parse(resposta);
}

export function useCategorias(autorizado: boolean) {
  return useQuery({ queryKey: ['tags'], queryFn: listarTags, enabled: autorizado });
}
