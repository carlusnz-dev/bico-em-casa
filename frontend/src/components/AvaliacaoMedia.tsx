import { Star } from 'lucide-react';
import clsx from 'clsx';

export function AvaliacaoMedia({
  nota,
  quantidade,
  className,
}: {
  nota: number;
  quantidade?: number;
  className?: string;
}) {
  return (
    <div className={clsx('flex items-center gap-1 text-sm', className)}>
      <Star size={14} className="fill-secondary text-secondary" aria-hidden="true" />
      <span className="font-medium text-texto">{nota.toFixed(1)}</span>
      {typeof quantidade === 'number' && (
        <span className="text-texto-suave">
          ({quantidade} {quantidade === 1 ? 'avaliação' : 'avaliações'})
        </span>
      )}
    </div>
  );
}
