import Link from 'next/link';

import type { Tag } from '@/api/contratos/tag';
import { CategoriaIcone } from '@/components/CategoriaIcone';

export function MiniMenuCategorias({ tags }: { tags: Tag[] }) {
  if (tags.length === 0) return null;

  return (
    <nav aria-label="Categorias de serviço" className="flex flex-wrap gap-4">
      {tags.map((tag) => (
        <Link
          key={tag.id}
          href={`/servicos?categoria=${tag.slug}`}
          className="flex w-20 flex-col items-center gap-2 text-center text-xs text-texto-suave hover:text-texto"
        >
          <span className="flex h-14 w-14 items-center justify-center rounded-full bg-primary/10">
            <CategoriaIcone slug={tag.slug} className="h-6 w-6 text-primary" />
          </span>
          {tag.nome}
        </Link>
      ))}
    </nav>
  );
}
