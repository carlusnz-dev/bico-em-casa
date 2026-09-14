import * as z from 'zod';

import { request } from './cliente';
import { Tag, tagSchema } from './contratos/tag';

export async function listarTags(): Promise<Tag[]> {
  const resposta = await request('/tag');
  return z.array(tagSchema).parse(resposta);
}
