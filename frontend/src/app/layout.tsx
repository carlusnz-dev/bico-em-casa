import type { Metadata } from 'next';

import { Provedores } from './provedores';
import './globals.css';

export const metadata: Metadata = {
  title: 'Bico em Casa',
  description:
    'Plataforma de contratação de profissionais autônomos para serviços rápidos.',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="pt-BR">
      <body>
        <Provedores>{children}</Provedores>
      </body>
    </html>
  );
}
