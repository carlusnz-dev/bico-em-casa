import { Cabecalho } from '@/components/layout/Cabecalho';
import { Rodape } from '@/components/layout/Rodape';

export default function LayoutAutenticacao({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex min-h-dvh flex-col">
      <Cabecalho />
      <main className="mx-auto flex w-full max-w-md flex-1 items-center px-4 py-10">
        <div className="w-full">{children}</div>
      </main>
      <Rodape />
    </div>
  );
}
