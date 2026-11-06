'use client';

import { useEffect, useRef, useState } from 'react';

const BATIDAS = 6;
const PROFUNDIDADE_MAXIMA = 38;
const FRACAO_SUBIDA = 0.8;
const ELEVACAO_MARTELO_GRAUS = 75;

const OMBRO = { x: 122, y: 152 };
const ALCANCE_MARTELO = 103;
const FACE_ABAIXO_DO_CABO = 8;

const TOPO_TRONCO = 215;
const TOPO_PREGO = 170;
const PREGO_X = 218;

function limitar(valor: number, minimo: number, maximo: number): number {
  return Math.min(Math.max(valor, minimo), maximo);
}

function suavizarSaida(t: number): number {
  return 1 - (1 - t) ** 2;
}

interface Quadro {
  profundidade: number;
  anguloMartelo: number;
  batidasDadas: number;
  impacto: boolean;
}

/** Converte o progresso do scroll (0 a 1) na pose do martelo e na profundidade do prego. */
function calcularQuadro(progresso: number): Quadro {
  const ciclo = progresso * BATIDAS;
  const batidasDadas = Math.floor(ciclo);
  const fase = ciclo - batidasDadas;

  const golpe = fase > FRACAO_SUBIDA ? (fase - FRACAO_SUBIDA) / (1 - FRACAO_SUBIDA) : 0;
  const cravada = limitar((golpe - 0.85) / 0.15, 0, 1);
  const profundidade = ((batidasDadas + cravada) / BATIDAS) * PROFUNDIDADE_MAXIMA;

  // Ao bater, a face do martelo precisa encostar na cabeça do prego, que desce a cada batida.
  const cabecaDoPrego = TOPO_PREGO + profundidade;
  const senoDoGolpe = limitar(
    (cabecaDoPrego - OMBRO.y - FACE_ABAIXO_DO_CABO) / ALCANCE_MARTELO,
    -1,
    1,
  );
  const anguloDoGolpe = (Math.asin(senoDoGolpe) * 180) / Math.PI;

  const elevacao = fase <= FRACAO_SUBIDA ? suavizarSaida(fase / FRACAO_SUBIDA) : 1 - golpe ** 2;
  const anguloMartelo = anguloDoGolpe - ELEVACAO_MARTELO_GRAUS * elevacao;

  const impacto = golpe > 0.9 || (batidasDadas > 0 && fase < 0.1);

  return { profundidade, anguloMartelo, batidasDadas, impacto };
}

export function MarteloNoPrego() {
  const trilhaRef = useRef<HTMLDivElement>(null);
  const [progresso, setProgresso] = useState(0);

  useEffect(() => {
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      setProgresso(1);
      return;
    }

    let quadro = 0;

    function atualizar() {
      quadro = 0;
      const trilha = trilhaRef.current;
      if (!trilha) return;

      const { top, height } = trilha.getBoundingClientRect();
      const percurso = height - window.innerHeight;
      setProgresso(limitar(-top / percurso, 0, 1));
    }

    function aoRolar() {
      if (quadro === 0) quadro = requestAnimationFrame(atualizar);
    }

    atualizar();
    window.addEventListener('scroll', aoRolar, { passive: true });
    window.addEventListener('resize', aoRolar);

    return () => {
      cancelAnimationFrame(quadro);
      window.removeEventListener('scroll', aoRolar);
      window.removeEventListener('resize', aoRolar);
    };
  }, []);

  const { profundidade, anguloMartelo, batidasDadas, impacto } = calcularQuadro(progresso);
  const concluido = progresso >= 1;

  return (
    <div ref={trilhaRef} className="relative h-[400vh]">
      <div className="sticky top-0 flex h-dvh flex-col items-center justify-center gap-6 md:flex-row md:gap-12">
        <div className="max-w-sm text-center md:text-left">
          <p className="mb-2 text-sm font-medium uppercase tracking-widest text-accent">
            Prego por prego
          </p>
          <h2 className="font-heading text-3xl font-bold sm:text-4xl">
            Serviço bem feito se faz uma batida de cada vez.
          </h2>
          <p className="mt-4 text-texto-suave" aria-live="polite">
            {concluido
              ? 'Prego firme. Agora é com você.'
              : `Batida ${Math.min(batidasDadas + 1, BATIDAS)} de ${BATIDAS}`}
          </p>
          {progresso < 0.03 && (
            <p className="mt-6 animate-bounce text-sm text-texto-suave">↓ Role para baixo</p>
          )}
        </div>

        <svg
          viewBox="0 0 400 300"
          role="img"
          aria-label="Um profissional martelando um prego em um tronco conforme a página é rolada"
          className="w-full max-w-lg"
        >
          <defs>
            <clipPath id="recorte-acima-do-tronco">
              <rect x="0" y="0" width="400" height={TOPO_TRONCO} />
            </clipPath>
          </defs>

          <rect x="0" y="260" width="400" height="8" rx="4" className="fill-borda" />

          {/* Tronco */}
          <rect x="165" y={TOPO_TRONCO} width="150" height="45" rx="8" fill="#a0673a" />
          <path
            d="M180 232h110M175 246h120"
            stroke="#7d4f2a"
            strokeWidth="2"
            strokeLinecap="round"
            fill="none"
          />
          <ellipse cx="315" cy="237.5" rx="10" ry="22.5" fill="#c58b55" />
          <ellipse cx="315" cy="237.5" rx="5" ry="12" fill="none" stroke="#a0673a" strokeWidth="2" />

          {/* Prego: o recorte esconde a parte que já entrou no tronco */}
          <g clipPath="url(#recorte-acima-do-tronco)">
            <g transform={`translate(0 ${profundidade})`}>
              <rect x={PREGO_X - 2} y={TOPO_PREGO + 4} width="4" height="45" fill="#8a949c" />
              <rect x={PREGO_X - 7} y={TOPO_PREGO} width="14" height="5" rx="2" fill="#aab3ba" />
            </g>
          </g>

          {/* Profissional */}
          <rect x="98" y="198" width="10" height="62" rx="4" className="fill-navy" />
          <rect x="112" y="198" width="10" height="62" rx="4" className="fill-navy" />
          <rect x="94" y="140" width="32" height="62" rx="10" className="fill-primary" />
          <circle cx="110" cy="120" r="16" fill="#f1c9a5" />
          <circle cx="116" cy="118" r="2" className="fill-navy" />
          <path
            d="M113 126q4 3 8 0"
            stroke="#1f3a52"
            strokeWidth="1.6"
            strokeLinecap="round"
            fill="none"
          />
          <path d="M93 117A17 17 0 0 1 127 117Z" className="fill-accent" />
          <rect x="90" y="115" width="40" height="4" rx="2" className="fill-accent" />

          {/* Braço e martelo giram juntos em torno do ombro */}
          <g transform={`rotate(${anguloMartelo} ${OMBRO.x} ${OMBRO.y})`}>
            <line
              x1={OMBRO.x}
              y1={OMBRO.y}
              x2="172"
              y2={OMBRO.y}
              stroke="#f1c9a5"
              strokeWidth="9"
              strokeLinecap="round"
            />
            <line
              x1="172"
              y1={OMBRO.y}
              x2="222"
              y2={OMBRO.y}
              stroke="#8a5a2b"
              strokeWidth="5"
              strokeLinecap="round"
            />
            <rect x="212" y="142" width="26" height="18" rx="3" fill="#5b6770" />
            <circle cx="172" cy={OMBRO.y} r="5.5" fill="#f1c9a5" />
          </g>

          {/* Faíscas e onomatopeia no impacto */}
          <g opacity={impacto ? 1 : 0} className="transition-opacity duration-75">
            <path
              d={`M${PREGO_X - 18} ${TOPO_PREGO + profundidade - 4}l-10 -8M${PREGO_X + 18} ${TOPO_PREGO + profundidade - 4}l10 -8M${PREGO_X} ${TOPO_PREGO + profundidade - 14}v-12`}
              stroke="#ee712b"
              strokeWidth="3"
              strokeLinecap="round"
            />
            <text
              x={PREGO_X + 34}
              y={TOPO_PREGO + profundidade - 18}
              className="fill-accent font-heading text-[18px] font-bold"
            >
              TOC!
            </text>
          </g>
        </svg>
      </div>
    </div>
  );
}
