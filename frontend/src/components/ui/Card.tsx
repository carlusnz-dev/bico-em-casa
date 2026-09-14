import clsx from 'clsx';

export function Card({ className, children, ...resto }: React.ComponentProps<'div'>) {
  return (
    <div
      className={clsx(
        'rounded-xl border border-borda bg-superficie p-6 shadow-sm',
        className,
      )}
      {...resto}
    >
      {children}
    </div>
  );
}
