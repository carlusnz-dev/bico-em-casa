import { classesDoBotao } from '@/components/ui/Botao';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Select } from '@/components/ui/Select';
import type { Tag } from '@/api/contratos/tag';

interface FiltroCidadeProps {
  acao: string;
  cidade?: string;
  busca?: string;
  ordenar?: string;
  categoria?: string;
  categorias?: Tag[];
  precoMinimo?: string;
  precoMaximo?: string;
}

export function FiltroCidade({
  acao, cidade = '', busca, ordenar = '', categoria, categorias, precoMinimo, precoMaximo,
}: FiltroCidadeProps) {
  return (
    <form action={acao} method="get" className="flex flex-col gap-3 sm:flex-row sm:flex-wrap sm:items-end">
      <div className="flex flex-1 flex-col gap-1.5">
        <Label htmlFor="filtro-cidade">Filtrar por cidade</Label>
        <Input
          id="filtro-cidade"
          name="cidade"
          maxLength={100}
          autoComplete="address-level2"
          placeholder="Ex.: Belo Horizonte"
          defaultValue={cidade}
        />
      </div>
      {categorias && (
        <>
          <div className="flex flex-1 flex-col gap-1.5">
            <Label htmlFor="categoria">Categoria</Label>
            <Select id="categoria" name="categoria" defaultValue={categoria}>
              <option value="">Todas as categorias</option>
              {categorias.map((tag) => <option key={tag.id} value={tag.slug}>{tag.nome}</option>)}
            </Select>
          </div>
          <div className="flex flex-1 flex-col gap-1.5">
            <Label htmlFor="preco-minimo">Preço mínimo (R$)</Label>
            <Input id="preco-minimo" name="precoMinimo" type="number" inputMode="decimal"
              min="0" max="99999999.99" step="0.01" defaultValue={precoMinimo} />
          </div>
          <div className="flex flex-1 flex-col gap-1.5">
            <Label htmlFor="preco-maximo">Preço máximo (R$)</Label>
            <Input id="preco-maximo" name="precoMaximo" type="number" inputMode="decimal"
              min="0" max="99999999.99" step="0.01" defaultValue={precoMaximo} />
          </div>
        </>
      )}
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
