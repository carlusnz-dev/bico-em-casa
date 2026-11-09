import type { MidiaPortfolio } from '@/api/contratos/portfolio';

export function GaleriaPortfolio({
  midias,
  renderAcao,
}: {
  midias: MidiaPortfolio[];
  renderAcao?: (midia: MidiaPortfolio) => React.ReactNode;
}) {
  return (
    <ul className="grid gap-4 sm:grid-cols-2">
      {midias.map((midia, indice) => (
        <li key={midia.id} className="flex flex-col gap-2">
          {midia.contentType.startsWith('video/') ? (
            <video
              controls
              preload="metadata"
              aria-label={`Vídeo do trabalho ${indice + 1}`}
              className="aspect-video w-full rounded-lg bg-borda/30"
            >
              <source src={midia.url} type={midia.contentType} />
              Seu navegador não consegue reproduzir este vídeo.
            </video>
          ) : (
            // eslint-disable-next-line @next/next/no-img-element
            <img
              src={midia.url}
              alt={`Foto do trabalho ${indice + 1}`}
              loading="lazy"
              className="aspect-video w-full rounded-lg object-cover"
            />
          )}
          {renderAcao?.(midia)}
        </li>
      ))}
    </ul>
  );
}
