import * as z from 'zod';

export const statusContratacaoSchema = z.enum(['EM_ANDAMENTO', 'CONCLUIDO', 'CANCELADO']);

export type StatusContratacao = z.infer<typeof statusContratacaoSchema>;

export const contratacaoSchema = z.object({
  id: z.uuid(),
  servicoId: z.uuid(),
  clienteId: z.uuid(),
  profissionalId: z.uuid(),
  telefoneProfissional: z.string().nullable().optional(),
  tituloServico: z.string(),
  precoServico: z.number(),
  observacao: z.string().nullable(),
  status: statusContratacaoSchema,
  arquivada: z.boolean(),
  criadoEm: z.string(),
});

export type Contratacao = z.infer<typeof contratacaoSchema>;

export const contratarServicoRequestSchema = z.object({
  servicoId: z.uuid(),
  observacao: z.string().max(1000, 'Máximo de 1000 caracteres').optional(),
});

export type ContratarServicoRequest = z.infer<typeof contratarServicoRequestSchema>;

export const editarContratacaoRequestSchema = z.object({
  observacao: z.string().max(1000, 'Máximo de 1000 caracteres').optional(),
});

export type EditarContratacaoRequest = z.infer<typeof editarContratacaoRequestSchema>;

export const alterarStatusContratacaoRequestSchema = z.object({
  status: statusContratacaoSchema,
});

export type AlterarStatusContratacaoRequest = z.infer<typeof alterarStatusContratacaoRequestSchema>;

export function gerarUrlWhatsappPrestador(
    telefone: string | null | undefined,
    tituloServico: string,
): string | null {
  if (!telefone) return null;

  const digitos = telefone.replace(/\D/g,``);
  if (!digitos) return null;

  const numeroComDdi = digitos.startsWith(`55`) ? digitos : `55${digitos}`;
  const mensagem = `Olá! Acabei de contratar o seu serviço "${tituloServico}" pelo Bico em Casa.
  Por favor, acesse suas contratações para ver os detalhes do serviço na plataforma para realizar o serviço ou cancelar a contratação. `;

  return `https://wa.me/${numeroComDdi}?text=${encodeURIComponent(mensagem)}`;
}