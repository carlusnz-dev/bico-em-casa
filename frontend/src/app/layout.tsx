import type { Metadata } from 'next';
import { Albert_Sans, Josefin_Sans } from 'next/font/google';

import { Provedores } from './provedores';
import './globals.css';

const josefinSans = Josefin_Sans({
  subsets: ['latin'],
  variable: '--font-josefin-sans',
});

const albertSans = Albert_Sans({
  subsets: ['latin'],
  variable: '--font-albert-sans',
});

export const metadata: Metadata = {
  title: 'Bico em Casa',
  description:
    'Plataforma de contratação de profissionais autônomos para serviços rápidos.',
};

const SCRIPT_TEMA_INICIAL = `
  try {
    var tema = localStorage.getItem('bico-em-casa:tema');
    var escuro = tema ? tema === 'escuro' : window.matchMedia('(prefers-color-scheme: dark)').matches;
    document.documentElement.classList.toggle('dark', escuro);
  } catch (e) {}
`;

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="pt-BR" className={`${josefinSans.variable} ${albertSans.variable}`}>
      <head>
        <script dangerouslySetInnerHTML={{ __html: SCRIPT_TEMA_INICIAL }} />
      </head>
      <body>
        <Provedores>{children}</Provedores>
      </body>
    </html>
  );
}
