import { Cabecalho } from '@/components/layout/Cabecalho';
import { Rodape } from '@/components/layout/Rodape';

export default function LayoutPublico({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex min-h-dvh flex-col">
      <Cabecalho />
      <main className="mx-auto w-full max-w-5xl flex-1 px-4 py-10">{children}</main>
      <Rodape />
    </div>
  );
}
