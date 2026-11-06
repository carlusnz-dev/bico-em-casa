import type { Metadata } from 'next';
import Link from 'next/link';

import { MarteloNoPrego } from '@/components/MarteloNoPrego';
import { Card } from '@/components/ui/Card';
import { classesDoBotao } from '@/components/ui/Botao';

export const metadata: Metadata = {
  title: 'Sobre | Bico em Casa',
  description:
    'Conheça o Bico em Casa: a plataforma que conecta clientes a profissionais autônomos para serviços rápidos.',
};

const PILARES = [
  {
    titulo: 'Propósito',
    texto:
      'Aproximar quem precisa de um serviço de quem sabe fazer, sem intermediários e sem burocracia.',
  },
  {
    titulo: 'Visão',
    texto:
      'Ser a primeira escolha de quem busca um profissional autônomo confiável perto de casa.',
  },
  {
    titulo: 'Valores',
    texto:
      'Transparência, respeito ao trabalho de cada profissional e confiança construída por avaliações reais.',
  },
];

const PASSOS_CLIENTE = [
  'Busque o serviço e filtre pela sua cidade.',
  'Veja o perfil e o portfólio do profissional.',
  'Contrate direto, sem cadastro complicado.',
  'Avalie o atendimento e ajude a comunidade.',
];

const PASSOS_PROFISSIONAL = [
  'Crie seu perfil e cadastre os serviços que oferece.',
  'Mostre seu trabalho com um portfólio (opcional).',
  'Receba contratações e acompanhe cada uma.',
  'Construa reputação com avaliações dos clientes.',
];

const DESTAQUES = [
  { titulo: 'Sem burocracia', texto: 'Da busca à contratação em poucos passos.' },
  { titulo: 'Portfólio opcional', texto: 'Quem tem o que mostrar mostra; quem não tem também começa.' },
  { titulo: 'Reputação real', texto: 'Avaliações vindas de contratações concluídas.' },
  { titulo: 'Segurança', texto: 'Denúncias e moderação para manter a plataforma saudável.' },
];

function ListaDePassos({ passos }: { passos: string[] }) {
  return (
    <ol className="flex flex-col gap-3">
      {passos.map((passo, indice) => (
        <li key={passo} className="flex items-start gap-3">
          <span className="flex size-7 shrink-0 items-center justify-center rounded-full bg-primary text-sm font-bold text-white">
            {indice + 1}
          </span>
          <span className="text-texto-suave">{passo}</span>
        </li>
      ))}
    </ol>
  );
}

export default function SobrePage() {
  return (
    <div className="flex flex-col gap-16">
      <section className="rounded-2xl bg-navy px-6 py-16 text-white sm:px-12 sm:py-20">
        <p className="mb-3 text-sm font-medium uppercase tracking-widest text-mint">
          Sobre o Bico em Casa
        </p>
        <h1 className="max-w-3xl font-heading text-4xl font-bold leading-tight sm:text-5xl">
          Bico bom é bico resolvido, perto de você.
        </h1>
        <p className="mt-5 max-w-2xl text-lg text-white/80">
          Conectamos clientes a profissionais autônomos para serviços rápidos do dia a dia, com
          portfólio, avaliações e contratação direta.
        </p>
        <div className="mt-8 flex flex-wrap gap-3">
          <Link href="/servicos" className={classesDoBotao('acento', 'grande')}>
            Explorar serviços
          </Link>
          <Link
            href="/cadastro"
            className={classesDoBotao('secundario', 'grande', 'border-white text-white hover:bg-white/10')}
          >
            Criar conta
          </Link>
        </div>
      </section>

      <MarteloNoPrego />

      <section className="flex flex-col gap-6">
        <h2 className="font-heading text-3xl font-bold">O que nos move</h2>
        <ul className="grid grid-cols-1 gap-4 md:grid-cols-3">
          {PILARES.map((pilar) => (
            <li key={pilar.titulo}>
              <Card className="h-full border-t-4 border-t-primary">
                <h3 className="mb-2 text-xl font-bold">{pilar.titulo}</h3>
                <p className="text-texto-suave">{pilar.texto}</p>
              </Card>
            </li>
          ))}
        </ul>
      </section>

      <section className="flex flex-col gap-6">
        <h2 className="font-heading text-3xl font-bold">Como funciona</h2>
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
          <Card>
            <h3 className="mb-4 text-xl font-bold text-primary">Para quem contrata</h3>
            <ListaDePassos passos={PASSOS_CLIENTE} />
          </Card>
          <Card>
            <h3 className="mb-4 text-xl font-bold text-accent">Para quem oferece</h3>
            <ListaDePassos passos={PASSOS_PROFISSIONAL} />
          </Card>
        </div>
      </section>

      <section className="flex flex-col gap-6">
        <h2 className="font-heading text-3xl font-bold">Por que o Bico em Casa</h2>
        <ul className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {DESTAQUES.map((destaque) => (
            <li key={destaque.titulo} className="rounded-xl bg-cream p-5">
              <h3 className="mb-1 text-lg font-bold">{destaque.titulo}</h3>
              <p className="text-sm text-texto-suave">{destaque.texto}</p>
            </li>
          ))}
        </ul>
      </section>

      <section className="flex flex-col items-center gap-4 rounded-2xl bg-primary px-6 py-12 text-center text-white">
        <h2 className="font-heading text-3xl font-bold">Pronto para resolver o seu bico?</h2>
        <p className="max-w-xl text-white/90">
          Encontre um profissional agora ou ofereça o seu trabalho para quem está procurando.
        </p>
        <Link href="/cadastro" className={classesDoBotao('acento', 'grande')}>
          Começar agora
        </Link>
      </section>
    </div>
  );
}
