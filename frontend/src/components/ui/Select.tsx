import clsx from 'clsx';

interface SelectProps extends React.ComponentProps<'select'> {
  erro?: boolean;
}

export function Select({ erro = false, className, children, ...resto }: SelectProps) {
  return (
    <select
      aria-invalid={erro || undefined}
      className={clsx(
        'h-10 w-full rounded-lg border bg-superficie px-3 text-sm text-texto',
        'focus:outline-2 focus:outline-offset-1 focus:outline-primary',
        'disabled:cursor-not-allowed disabled:bg-borda/30',
        erro ? 'border-erro' : 'border-borda',
        className,
      )}
      {...resto}
    >
      {children}
    </select>
  );
}
