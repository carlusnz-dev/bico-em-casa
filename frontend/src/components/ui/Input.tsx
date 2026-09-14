import clsx from 'clsx';

interface InputProps extends React.ComponentProps<'input'> {
  erro?: boolean;
}

export function Input({ erro = false, className, ...resto }: InputProps) {
  return (
    <input
      aria-invalid={erro || undefined}
      className={clsx(
        'h-10 w-full rounded-lg border bg-superficie px-3 text-sm text-texto',
        'placeholder:text-texto-suave',
        'focus:outline-2 focus:outline-offset-1 focus:outline-primary',
        'disabled:cursor-not-allowed disabled:bg-borda/30',
        erro ? 'border-erro' : 'border-borda',
        className,
      )}
      {...resto}
    />
  );
}
