'use client';

import { useQuery } from '@tanstack/react-query';
import { motion } from 'motion/react';
import Link from 'next/link';
import { useState } from 'react';

import { buscarMediaPorServico } from '@/api/avaliacoes';
import type { Perfil } from '@/api/contratos/perfil';
import type { Servico } from '@/api/contratos/servico';
import type { Tag } from '@/api/contratos/tag';
import { AvaliacaoMedia } from '@/components/AvaliacaoMedia';
import { AvatarIniciais } from '@/components/AvatarIniciais';
import { formatarPreco } from '@/components/formatarPreco';
import { FotoPlaceholder } from '@/components/FotoPlaceholder';
import { ModalPerfilProfissional } from '@/components/ModalPerfilProfissional';
import { Card } from '@/components/ui/Card';

export function CardServico({
  servico,
  tags,
  perfil,
  notaMedia: notaMediaRecebida,
}: {
  servico: Servico;
  tags: Tag[];
  perfil: Perfil | null;
  notaMedia?: number;
}) {
  const [modalAberto, setModalAberto] = useState(false);
  const categoriaPrincipal = tags[0];
  const {
    data: notaMediaConsultada,
    isError,
    isPending,
  } = useQuery({
    queryKey: ['avaliacao-media-servico', servico.id],
    queryFn: () => buscarMediaPorServico(servico.id),
    enabled: notaMediaRecebida === undefined,
  });
  const notaMediaServico = notaMediaRecebida ?? notaMediaConsultada;

  return (
    <>
      <motion.div
        initial={{ opacity: 0, y: 24 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, margin: '-80px' }}
        transition={{ duration: 0.4, ease: 'easeOut' }}
        className="h-full"
      >
        <Card className="flex h-full flex-col gap-3 overflow-hidden border-tertiary p-0 shadow-none">
          <Link href={`/servicos/${servico.id}`}>
            <FotoPlaceholder
              seed={servico.id}
              categoriaSlug={categoriaPrincipal?.slug}
              className="aspect-video w-full"
            />
          </Link>

          <div className="flex flex-1 flex-col gap-2 p-4 pt-0">
            <div className="flex items-center justify-between gap-2">
              {categoriaPrincipal && (
                <span className="rounded-full bg-primary/10 px-2.5 py-0.5 text-xs font-medium text-primary">
                  {categoriaPrincipal.nome}
                </span>
              )}
              {notaMediaServico !== undefined && notaMediaServico > 0 ? (
                <AvaliacaoMedia nota={notaMediaServico} />
              ) : notaMediaServico === 0 ? (
                <span className="text-xs text-texto-suave">Sem avaliações</span>
              ) : (
                <span className="text-xs text-texto-suave" role={isError ? 'alert' : 'status'}>
                  {isError ? 'Nota indisponível' : isPending ? 'Carregando nota' : 'Sem avaliações'}
                </span>
              )}
            </div>

            <Link href={`/servicos/${servico.id}`} className="hover:text-primary">
              <h2 className="text-lg font-semibold">{servico.titulo}</h2>
            </Link>

            <p className="line-clamp-2 flex-1 text-sm text-texto-suave">{servico.descricao}</p>

            {perfil && (
              <button
                type="button"
                onClick={() => setModalAberto(true)}
                className="flex items-center gap-2 self-start text-sm text-texto-suave hover:text-texto"
              >
                <motion.span layoutId={`avatar-perfil-${perfil.id}`} className="flex">
                  {perfil.fotoUrl ? (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img src={perfil.fotoUrl} alt="" className="h-6 w-6 rounded-full object-cover" />
                  ) : (
                    <AvatarIniciais nome={perfil.nomeExibicao} className="h-6 w-6 text-xs" />
                  )}
                </motion.span>
                {perfil.nomeExibicao}
              </button>
            )}

            <div className="mt-1 flex items-center justify-between gap-2">
              <span className="font-medium">{formatarPreco(servico.precoPrevio, servico.unidadePreco)}</span>
              <Link
                href={`/servicos/${servico.id}`}
                className="inline-flex h-9 items-center justify-center rounded-lg bg-secondary px-4 text-sm font-medium text-white transition-all duration-150 hover:scale-95 hover:bg-secondary-dark hover:ring-2 hover:ring-secondary hover:ring-offset-2 hover:ring-offset-superficie"
              >
                Ver serviço
              </Link>
            </div>
          </div>
        </Card>
      </motion.div>

      {perfil && (
        <ModalPerfilProfissional
          perfil={perfil}
          aberto={modalAberto}
          aoFechar={() => setModalAberto(false)}
        />
      )}
    </>
  );
}
