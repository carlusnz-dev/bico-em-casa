import clsx from 'clsx';

import { CategoriaIcone } from '@/components/CategoriaIcone';

const DUPLAS_DE_COR = [
  'from-primary/25 to-mint/25',
  'from-secondary/25 to-mint-pale/40',
  'from-blue/20 to-primary/20',
  'from-mint/25 to-blue/20',
];

function hashDeTexto(texto: string): number {
  let hash = 0;
  for (let indice = 0; indice < texto.length; indice += 1) {
    hash = (hash * 31 + texto.charCodeAt(indice)) >>> 0;
  }
  return hash;
}

/**
 * Nem `Servico` nem `Portfolio` têm campo de foto principal ainda — placeholder
 * geométrico determinístico (mesmo id, mesmo visual) até a equipe implementar
 * upload de imagem do serviço.
 */
export function FotoPlaceholder({
  seed,
  categoriaSlug,
  className,
}: {
  seed: string;
  categoriaSlug?: string;
  className?: string;
}) {
  const hash = hashDeTexto(seed);
  const dupla = DUPLAS_DE_COR[hash % DUPLAS_DE_COR.length];

  return (
    <div
      className={clsx(
        'flex items-center justify-center bg-gradient-to-br',
        dupla,
        className,
      )}
    >
      <CategoriaIcone slug={categoriaSlug ?? seed} className="h-8 w-8 text-primary/70" />
    </div>
  );
}
