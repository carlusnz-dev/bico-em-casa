'use client';

import { useQuery } from '@tanstack/react-query';
import { AnimatePresence, motion } from 'motion/react';
import Link from 'next/link';
import { Dialog } from 'radix-ui';

import { listarAvaliacoesRecebidas } from '@/api/avaliacoes';
import type { Perfil } from '@/api/contratos/perfil';
import { listarPortfolios } from '@/api/portfolio';
import { AvaliacaoMedia } from '@/components/AvaliacaoMedia';
import { AvatarIniciais } from '@/components/AvatarIniciais';
import { DataFormatada } from '@/components/DataFormatada';
import { classesDoBotao } from '@/components/ui/Botao';

export function ModalPerfilProfissional({
  perfil,
  aberto,
  aoFechar,
}: {
  perfil: Perfil;
  aberto: boolean;
  aoFechar: () => void;
}) {
  const { data: avaliacoes } = useQuery({
    queryKey: ['avaliacoes-recebidas', perfil.id],
    queryFn: () => listarAvaliacoesRecebidas(perfil.id),
    enabled: aberto,
  });

  // Não existe endpoint de "portfólio por perfil" — filtramos a listagem
  // pública, que já é dado real (não mock), só não indexado por perfilId.
  const { data: portfolios } = useQuery({
    queryKey: ['portfolios-para-modal'],
    queryFn: () => listarPortfolios(0, 100).then((pagina) => pagina.conteudo),
    enabled: aberto,
  });

  const portfolio = portfolios?.find((item) => item.perfilId === perfil.id);
  const notaMedia = avaliacoes?.length
    ? avaliacoes.reduce((soma, avaliacao) => soma + avaliacao.nota, 0) / avaliacoes.length
    : null;

  return (
    <Dialog.Root open={aberto} onOpenChange={(proximoAberto) => !proximoAberto && aoFechar()}>
      <AnimatePresence>
        {aberto && (
          <Dialog.Portal forceMount>
            <Dialog.Overlay asChild forceMount>
              <motion.div
                className="fixed inset-0 z-50 bg-texto/40"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                transition={{ duration: 0.2 }}
              />
            </Dialog.Overlay>

            <Dialog.Content asChild forceMount aria-describedby={undefined}>
              <motion.div
                initial={{ opacity: 0, y: -32 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -32 }}
                transition={{ duration: 0.25, ease: 'easeOut' }}
                className="fixed inset-x-4 top-16 z-50 mx-auto flex max-w-md flex-col gap-4 rounded-xl border border-borda bg-superficie p-6 shadow-lg outline-none"
              >
                <div className="flex items-start gap-4">
                  {perfil.fotoUrl ? (
                    // eslint-disable-next-line @next/next/no-img-element
                    <motion.img
                      layoutId={`avatar-perfil-${perfil.id}`}
                      src={perfil.fotoUrl}
                      alt=""
                      className="h-16 w-16 shrink-0 rounded-full object-cover"
                    />
                  ) : (
                    <motion.div layoutId={`avatar-perfil-${perfil.id}`}>
                      <AvatarIniciais nome={perfil.nomeExibicao} tamanho="grande" />
                    </motion.div>
                  )}

                  <div className="flex flex-col gap-1">
                    <Dialog.Title className="font-heading text-lg font-bold text-texto">
                      {perfil.nomeExibicao}
                    </Dialog.Title>
                    <p className="text-sm text-texto-suave">
                      No Bico em Casa desde <DataFormatada dataIso={perfil.criadoEm} />
                    </p>
                    {notaMedia !== null && (
                      <AvaliacaoMedia nota={notaMedia} quantidade={avaliacoes?.length} />
                    )}
                  </div>

                  <Dialog.Close
                    aria-label="Fechar"
                    className="ml-auto rounded-lg p-1 text-texto-suave hover:bg-borda/40 hover:text-texto"
                  >
                    ✕
                  </Dialog.Close>
                </div>

                {portfolio?.fotoCapaUrl && (
                  // eslint-disable-next-line @next/next/no-img-element
                  <img
                    src={portfolio.fotoCapaUrl}
                    alt={portfolio.titulo}
                    className="h-32 w-full rounded-lg object-cover"
                  />
                )}

                {perfil.bio && <p className="text-sm text-texto-suave">{perfil.bio}</p>}

                {portfolio && (
                  <Link
                    href={`/profissionais/${portfolio.id}`}
                    onClick={aoFechar}
                    className={classesDoBotao('primario', 'medio', 'self-start')}
                  >
                    Ver perfil completo
                  </Link>
                )}
              </motion.div>
            </Dialog.Content>
          </Dialog.Portal>
        )}
      </AnimatePresence>
    </Dialog.Root>
  );
}
