# ADR-0005 — `src/api` como única fronteira do frontend

| Campo | Valor |
|---|---|
| **ADR** | `0005` |
| **Título** | `src/api` como única fronteira do frontend |
| **Autor** | Carlos Antunes |
| **Data** | 2026-08-22 |
| **Tópico** | Frontend |
| **Status** | Aceito |
| **LLM utilizada** | `Claude Opus 5 (Claude Code)` |

---

## Contexto

A versão 1.0.0 do `arquitetura-sistema.json` propunha `src/{app,components,hooks,services,types,utils}`,
com `services/{api,queries}` e `types/` guardando "interfaces TypeScript e schemas Zod
compartilhados".

Duas fragilidades nesse arranjo:

1. **Tipo e schema em lugares separados.** Um `types/Profissional.ts` escrito à mão e um schema
   Zod em outro arquivo saem de sincronia na primeira mudança de campo do backend. O
   TypeScript compila feliz, e o erro só aparece em runtime, longe da causa.
2. **Nada impede `fetch` espalhado.** Com `services/` sendo apenas mais uma pasta, um
   componente pode chamar a API direto. Quando o backend muda um contrato, a mudança precisa
   ser caçada em vários arquivos.

O usuário especificou `src/{app,components,hooks,api}` — exatamente quatro pastas — e foi
explícito sobre o princípio: o frontend deve trabalhar *"sem conhecer ou querer validar o que
o back-end faz, fazer por borda na interface, fronteira"*.

## Decisão

O frontend usa **exatamente quatro pastas** em `src/`: `app/`, `components/`, `hooks/` e `api/`.

**`src/api/` é a única fronteira com o backend.** Sua estrutura:

| Arquivo/Pasta | Papel |
|---|---|
| `client.ts` | Wrapper do `fetch` nativo: base URL, injeção do token Supabase, tratamento de status, normalização de erro |
| `erros.ts` | Tipo de erro único da aplicação, traduzido do `ProblemDetail` (RFC 9457) do backend |
| `contratos/` | Schemas Zod por recurso e tipos inferidos por `z.infer` — o contrato do backend expresso em TypeScript |
| `<recurso>.ts` | Funções de fetch e hooks TanStack Query por recurso |

### Regras

1. Somente `src/api/` conhece URLs, headers e formato de payload do backend
2. Toda resposta do backend é validada por schema Zod **antes** de entrar na aplicação
3. Os tipos são **inferidos** dos schemas (`z.infer`), nunca escritos à mão em paralelo
4. Componentes e hooks **jamais** chamam `fetch` — consomem os hooks de `src/api/`
5. Erro de rede ou payload inválido é normalizado para um tipo de erro único

### Pastas deliberadamente ausentes

- **`types/`** — todo tipo de domínio nasce inferido dos schemas Zod em `api/contratos/`
- **`services/`** — substituída por `api/`
- **`utils/`** — não definida nesta versão; exige novo ADR para ser criada

Divisão de responsabilidade: `app/` compõe, `components/` apresenta, `hooks/` reage,
`api/` conversa com o mundo.

## Alternativas Consideradas

| Opção | Prós | Contras | Por que foi recusada |
|---|---|---|---|
| `services/` + `types/` + `utils/` (proposta v1) | Familiar | Tipo e schema desincronizam; nada impede `fetch` espalhado | É o defeito que este ADR corrige |
| Tipos gerados do OpenAPI do backend (`openapi-typescript`) | Sincronia automática com o contrato real | Acopla o build do frontend ao backend rodando; tipos gerados não **validam** em runtime — só afirmam | Tipo gerado não protege contra o backend devolvendo algo diferente do que documenta |
| `supabase-js` lendo tabelas direto no frontend | Menos código | Regra de negócio migraria para políticas RLS; o backend perderia o sentido | Já recusado no [ADR-0002](./0002-supabase-como-baas.md) |
| Manter `axios` | Interceptors prontos; familiar | Contorna o cache e a revalidação do App Router do Next 16 | O `fetch` nativo participa do cache do framework; o axios anula esse ganho |

## Consequências

### Positivas

- Mudança de contrato do backend estoura num único lugar previsível, com mensagem de schema,
  em vez de virar `undefined` no meio de um componente
- Tipo e validação nunca divergem, porque o tipo **deriva** do schema
- Trocar cliente HTTP, base URL ou formato de erro afeta um arquivo
- Componentes ficam testáveis sem mock de rede: recebem dados por props

### Negativas

- Escrever schema Zod para cada resposta é trabalho manual e repetitivo, e a tentação de pular
  a validação num endpoint "simples" é real — basta um pulo para o benefício da regra 2 se perder
- Validação em runtime tem custo de CPU em respostas grandes (listagens longas)
- Sem `utils/`, helpers puros ficam sem casa definida — inclusive o `cn()` de
  `clsx` + `tailwind-merge`. Assumido conscientemente: quando a necessidade aparecer, entra por ADR
- Um `hooks/` que não pode fazer `fetch` é contraintuitivo para quem vem de outros projetos

### Neutras

- A regra de "pasta só com mais de um arquivo" do [ADR-0004](./0004-estrutura-modular-por-dominio.md)
  vale igualmente aqui: `<recurso>.ts` até surgir o segundo arquivo

## Impacto na Arquitetura

Esta decisão altera `docs/arquitetura-sistema.json`?

- [x] Sim — campos alterados: `directory_structure.frontend` (reescrito por completo),
  `frontend.boundary_policy`, `frontend.dependencies.removed`,
  `code_standards_and_style.frontend.rules`
- [ ] Não

## Referências

- [`design-sistema.md` §4.2 e §11.3](../design-sistema.md)
- [Zod — inferência de tipos](https://zod.dev/)
- [RFC 9457 — Problem Details for HTTP APIs](https://www.rfc-editor.org/rfc/rfc9457)
