'use client';

export function DataFormatada({
  dataIso,
  opcoes = { month: 'long', year: 'numeric' },
}: {
  dataIso: string;
  opcoes?: Intl.DateTimeFormatOptions;
}) {
  return new Date(dataIso).toLocaleDateString('pt-BR', opcoes);
}
