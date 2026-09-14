import clsx from 'clsx';

export type VarianteBotao = 'primario' | 'acento' | 'secundario' | 'fantasma';
export type TamanhoBotao = 'medio' | 'grande';

const POR_VARIANTE: Record<VarianteBotao, string> = {
  primario: 'bg-primary text-white hover:bg-primary/90 focus-visible:outline-primary',
  acento: 'bg-accent text-white hover:bg-accent/90 focus-visible:outline-accent',
  secundario:
    'border border-primary bg-transparent text-primary hover:bg-primary/10 focus-visible:outline-primary',
  fantasma: 'bg-transparent text-texto hover:bg-borda/40 focus-visible:outline-primary',
};

const POR_TAMANHO: Record<TamanhoBotao, string> = {
  medio: 'h-10 px-4 text-sm',
  grande: 'h-12 px-6 text-base',
};

export function classesDoBotao(
  variante: VarianteBotao = 'primario',
  tamanho: TamanhoBotao = 'medio',
  extra?: string,
): string {
  return clsx(
    'inline-flex items-center justify-center gap-2 rounded-lg font-medium transition-colors',
    'focus-visible:outline-2 focus-visible:outline-offset-2',
    'disabled:cursor-not-allowed disabled:opacity-50',
    POR_VARIANTE[variante],
    POR_TAMANHO[tamanho],
    extra,
  );
}

interface BotaoProps extends React.ComponentProps<'button'> {
  variante?: VarianteBotao;
  tamanho?: TamanhoBotao;
  carregando?: boolean;
}

export function Botao({
  variante = 'primario',
  tamanho = 'medio',
  carregando = false,
  className,
  disabled,
  children,
  type = 'button',
  ...resto
}: BotaoProps) {
  return (
    <button
      type={type}
      className={classesDoBotao(variante, tamanho, className)}
      disabled={disabled || carregando}
      aria-busy={carregando}
      {...resto}
    >
      {carregando ? 'Aguarde…' : children}
    </button>
  );
}
