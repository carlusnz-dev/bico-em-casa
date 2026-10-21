import { classesDoBotao } from '@/components/ui/Botao';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';

interface FiltroCidadeProps {
  acao: string;
  cidade?: string;
  busca?: string;
}

export function FiltroCidade({ acao, cidade = '', busca }: FiltroCidadeProps) {
  return (
    <form action={acao} method="get" className="flex flex-col gap-2 sm:flex-row sm:items-end">
      <div className="flex flex-1 flex-col gap-1.5">
        <Label htmlFor="filtro-cidade">Filtrar por cidade</Label>
        <Input
          id="filtro-cidade"
          name="cidade"
          autoComplete="address-level2"
          placeholder="Ex.: Belo Horizonte"
          defaultValue={cidade}
        />
      </div>
      {busca && <input type="hidden" name="busca" value={busca} />}
      <button type="submit" className={classesDoBotao('primario')}>
        Buscar
      </button>
    </form>
  );
}
