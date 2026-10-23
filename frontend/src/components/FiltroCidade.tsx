import { classesDoBotao } from '@/components/ui/Botao';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Select } from '@/components/ui/Select';

interface FiltroCidadeProps {
  acao: string;
  cidade?: string;
  busca?: string;
  ordenar?: string;
}

export function FiltroCidade({ acao, cidade = '', busca, ordenar = '' }: FiltroCidadeProps) {
  return (
    <form action={acao} method="get" className="flex flex-col gap-3 sm:flex-row sm:items-end">
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
      <div className="flex flex-1 flex-col gap-1.5">
        <Label htmlFor="ordenar-servicos">Ordenar serviços</Label>
        <Select id="ordenar-servicos" name="ordenar" defaultValue={ordenar}>
          <option value="">Mais recentes</option>
          <option value="nota">Maior nota média</option>
        </Select>
      </div>
      {busca && <input type="hidden" name="busca" value={busca} />}
      <button type="submit" className={classesDoBotao('primario')}>
        Aplicar filtros
      </button>
    </form>
  );
}
