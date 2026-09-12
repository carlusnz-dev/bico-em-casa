# Bico em Casa — Frontend

Frontend do **Bico em Casa**, plataforma de contratação de profissionais autônomos para serviços
rápidos. Consome exclusivamente a API própria do backend (Spring Boot) — não há provedor externo
de identidade, banco ou storage no lado do cliente.

Esta é a **fundação técnica** do frontend: toolchain, build, tipos, estilo e teste, prontos e
verificados. **Não há código de aplicação** — nem produto, nem a camada `src/api/`. A página
inicial é um placeholder textual, sem estilização.

> [!IMPORTANT]
> **O código de aplicação é escrito pela equipe, não pela LLM.** Este é um trabalho de faculdade
> e o objetivo é aprender as ferramentas. A fundação existe para que ninguém perca tempo com
> configuração de toolchain; o que vem depois — `src/api/`, componentes, hooks, telas — é da
> equipe. As convenções abaixo valem para esse código.

## Stack e versões

| Item | Versão |
|---|---|
| Next.js (App Router) | 16.3.3 |
| React / React DOM | 19.2.8 |
| TypeScript (strict, compilador nativo) | 7.0.2 |
| Tailwind CSS | 4.3.3 |
| Zod | 4.4.3 |
| Zustand | 5.0.15 |
| TanStack Query | 5.102.7 |
| Vitest | 4.1.11 |
| Playwright | 1.62.1 |

Node.js necessário: `>=20.9.0` (usado neste ambiente: v26.5.1).

## Como rodar

```bash
npm ci
npm run dev
```

Outros scripts:

| Script | O que faz |
|---|---|
| `npm run dev` | Servidor de desenvolvimento |
| `npm run build` | Build de produção |
| `npm run start` | Serve o build de produção |
| `npm run lint` | ESLint |
| `npm run typecheck` | `tsc --noEmit` |
| `npm test` | Testes com Vitest |

## Variáveis de ambiente

| Variável | Papel | Padrão |
|---|---|---|
| `NEXT_PUBLIC_API_URL` | URL base da API do backend | `http://localhost:8080` |

Nenhum segredo vive no frontend: a chave RSA de assinatura de JWT e as credenciais do MinIO
existem só no backend.

## Estrutura de pastas

```
src/
└── app/            # App Router (Server Components por padrão)
    ├── layout.tsx      # root layout, provider do TanStack Query
    ├── page.tsx        # placeholder textual da home
    ├── provedores.tsx  # único 'use client' do root layout
    └── globals.css     # `@import "tailwindcss"` (Tailwind 4 configura via CSS)
```

`api/`, `components/` e `hooks/` **não existem ainda** — nascem quando a equipe escrever o
primeiro arquivo de cada um, não antes.

### O que `src/api/` precisa ser quando a equipe criar

É a **única fronteira com o backend** ([ADR-0005](../docs/adr/0005-src-api-como-unica-fronteira.md)),
e a arquitetura já fixa o que mora lá:

| Arquivo | Papel |
|---|---|
| `client.ts` | Wrapper do `fetch` nativo: base URL, injeção do access token, refresh transparente, normalização de erro. O **único** lugar do projeto que chama `fetch` |
| `erros.ts` | Tipo de erro único da aplicação, traduzido do `ProblemDetail` (RFC 9457) do backend |
| `contratos/` | Schemas Zod por recurso e os tipos inferidos por `z.infer` |
| `<recurso>.ts` | Funções de fetch e hooks TanStack Query de cada recurso |

Detalhe completo em [`docs/design-sistema.md`](../docs/design-sistema.md), §4.2 e §11.3.

## Convenções deste projeto

- **Nenhum `fetch` fora de `src/api/`.** Toda chamada ao backend passa por `src/api/client.ts`.
- **Toda resposta da API é validada por schema Zod** em `src/api/contratos/` antes de entrar na
  aplicação. Resposta que não bate com o schema vira `ErroApi` (`erros.ts`), nunca `undefined`
  correndo solto pelo componente.
- **Tipos são inferidos dos schemas** (`z.infer<typeof schema>`), nunca escritos à mão em
  paralelo ao schema.
- **`any` é proibido.** Use `unknown` com narrowing (validação por schema, `typeof`, type guard).
- **Sem `src/types/`, `src/services/` ou `src/utils/`** — proibidas sem ADR (ver
  `docs/adr/`). O que pareceria ir em `utils/` mora perto de quem usa ou em `src/api/`.
- **Pasta só existe quando há mais de um arquivo daquele tipo.** Com um arquivo só, ele fica
  solto na pasta do módulo; ao nascer o segundo, os dois migram juntos para uma pasta nova.
- **Server Components por padrão.** `'use client'` só entra onde há interatividade de verdade
  (estado, evento de UI, hook de contexto). O provider do TanStack Query em
  `src/app/provedores.tsx` é o único client component do root layout.
- Sessão: o access token fica **em memória** (nunca em `localStorage`); o refresh token é cookie
  `httpOnly` emitido pela API — o frontend não o lê nem o manipula.

## Testes

`vitest.config.ts` está pronto (ambiente `jsdom`, alias `@` igual ao do `tsconfig.json`), mas
**não há nenhum teste** — o código de aplicação é da equipe, e o teste vem com ele.

Por isso o config traz `passWithNoTests: true`: sem essa flag o Vitest sai com código 1 em
*"No test files found"*, o que derrubaria o CI por ausência de teste em vez de por teste
quebrado. **Remova a flag quando o primeiro teste existir** — a partir daí, ausência de teste
deve doer.

## Limitação conhecida — `npm run lint` com TypeScript 7

TypeScript 7.0.2 é o compilador nativo (Go), GA desde 08/07/2026. Até este momento, o
`typescript-eslint` (do qual `eslint-config-next` depende) aborta com
`typescript-eslint does not support TS 7.0` — não é aviso de peer dependency, é uma barreira
ativa no código deles. `npm run typecheck` (via `tsc` puro), `npm run build` e `npm test` não são
afetados; só `npm run lint` quebra, com código de saída 2. Acompanhar
[typescript-eslint#10940](https://github.com/typescript-eslint/typescript-eslint/issues/10940).

**Decisão do desenvolvedor (2026-08-27):** manter o TypeScript 7 e conviver com o lint quebrado
até o upstream alcançar, em vez de descer de versão ou manter dois TypeScript no projeto. Isso
tem uma consequência a não esquecer: quando o `frontend-ci.yml` for escrito, a etapa de ESLint
precisa nascer desligada ou com `continue-on-error`, senão reprova todo PR.

O `.npmrc` com `legacy-peer-deps=true` existe pelo mesmo motivo — sem ele o `npm install` nem
termina. Ele sai junto quando o `typescript-eslint` atualizar a faixa de peer aceita.
