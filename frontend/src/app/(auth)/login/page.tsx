import type { Metadata } from 'next';

import FormLogin from '@/components/forms/FormLogin';
import { Card } from '@/components/ui/Card';

export const metadata: Metadata = {
  title: 'Entrar · Bico em Casa',
};

export default function LoginPage() {
  return (
    <Card>
      <h1 className="mb-1 text-2xl font-bold">Entrar</h1>
      <p className="mb-6 text-sm text-texto-suave">Bem-vindo de volta.</p>
      <FormLogin />
    </Card>
  );
}
