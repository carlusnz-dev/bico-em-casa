import {
  Baby,
  Brush,
  Car,
  Hammer,
  Leaf,
  type LucideIcon,
  PaintRoller,
  Plug,
  Scissors,
  Snowflake,
  Sparkles,
  Truck,
  Wrench,
} from 'lucide-react';

/**
 * As tags vêm da API sem ícone — mapeamos pelo slug para o mini-menu de
 * categorias da home. Categoria sem correspondência cai no ícone padrão.
 */
const ICONE_POR_PALAVRA: [string, LucideIcon][] = [
  ['limp', Sparkles],
  ['eletric', Plug],
  ['encana', Wrench],
  ['hidraulic', Wrench],
  ['jardin', Leaf],
  ['pint', PaintRoller],
  ['montage', Hammer],
  ['marcenaria', Hammer],
  ['beleza', Scissors],
  ['cabelo', Scissors],
  ['ar-condicionado', Snowflake],
  ['climatiz', Snowflake],
  ['mudanc', Truck],
  ['transport', Car],
  ['bab', Baby],
  ['reform', Brush],
];

const ICONE_PADRAO: LucideIcon = Sparkles;

export function iconePorCategoria(slug: string): LucideIcon {
  const encontrado = ICONE_POR_PALAVRA.find(([palavra]) => slug.includes(palavra));
  return encontrado ? encontrado[1] : ICONE_PADRAO;
}

export function CategoriaIcone({
  slug,
  className,
}: {
  slug: string;
  className?: string;
}) {
  const Icone = iconePorCategoria(slug);
  return <Icone aria-hidden="true" className={className} />;
}
