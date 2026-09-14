import clsx from 'clsx';

export function iniciaisDoNome(nome: string): string {
  const nomes = nome.trim().split(/\s+/).slice(0, 2);
  return nomes.map((parte) => parte[0]?.toUpperCase() ?? '').join('');
}

export function AvatarIniciais({
  nome,
  tamanho = 'medio',
  className,
}: {
  nome: string;
  tamanho?: 'medio' | 'grande';
  className?: string;
}) {
  return (
    <div
      aria-hidden="true"
      className={clsx(
        'flex shrink-0 items-center justify-center rounded-full bg-primary/10 font-semibold text-primary',
        tamanho === 'grande' ? 'h-16 w-16 text-xl' : 'h-10 w-10 text-sm',
        className,
      )}
    >
      {iniciaisDoNome(nome)}
    </div>
  );
}
