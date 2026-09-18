import clsx from 'clsx';

interface TextareaProps extends React.ComponentProps<'textarea'> {
  erro?: boolean;
}

export function Textarea({ erro = false, className, ...resto }: TextareaProps) {
  return (
    <textarea
      aria-invalid={erro || undefined}
      className={clsx(
        'w-full rounded-lg border bg-superficie px-3 py-2 text-sm text-texto',
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
