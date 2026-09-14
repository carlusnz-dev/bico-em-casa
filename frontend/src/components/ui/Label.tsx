import clsx from 'clsx';

interface LabelProps extends React.ComponentProps<'label'> {
  obrigatorio?: boolean;
}

export function Label({ obrigatorio = false, className, children, ...resto }: LabelProps) {
  return (
    <label className={clsx('text-sm font-medium text-texto', className)} {...resto}>
      {children}
      {obrigatorio && (
        <span aria-hidden="true" className="ml-0.5 text-erro">
          *
        </span>
      )}
    </label>
  );
}
