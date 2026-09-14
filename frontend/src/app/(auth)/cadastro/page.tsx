import type { Metadata } from 'next';

import FormCadastro from '@/components/forms/FormCadastro';
import { Card } from '@/components/ui/Card';

export const metadata: Metadata = {
  title: 'Criar conta · Bico em Casa',
};

export default function CadastroPage() {
  return (
    <Card>
      <h1 className="mb-1 text-2xl font-bold">Criar conta</h1>
      <p className="mb-6 text-sm text-texto-suave">Leva menos de um minuto.</p>
      <FormCadastro />
    </Card>
  );
}
