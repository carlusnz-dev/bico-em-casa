# Relatório de Sessão — 2026-08-27

| Campo | Valor |
|---|---|
| **Sessão** | Fundação do backend, do frontend e da infraestrutura local |
| **Autor** | Carlos Antunes |
| **Data** | `2026-08-27` |
| **Duração aproximada** | `3h` |
| **LLM utilizada** | `Claude Opus 5 (Claude Code)` — orquestração; 3 agentes `Claude Sonnet` em paralelo |
| **Branch** | `feat/fundacao-backend-e-infra` (saindo de `docs/requisitos-e-matriz-rastreabilidade`) |
| **Commits** | `0f26eff`, `1efc73e`, `743bb41`, `bde05fb`, `661698c`, `479077b`, `240b8b6`, `12327f9`, `f3710e3` |
| **Plano relacionado** | [`docs/planos/2026-08-27-fundacao-backend-frontend-e-infra.md`](../planos/2026-08-27-fundacao-backend-frontend-e-infra.md) |

---

## Resumo

O repositório parou de só descrever e passou a existir. Antes desta sessão eram 20 commits de
documentação e zero linha de código; agora `backend/` compila com `./mvnw clean compile`,
`frontend/` passa em `tsc --noEmit` e `npm run build`, e `docker-compose.yml` valida com
`docker compose config`.

O trabalho foi dividido em três agentes Sonnet com escopos de pasta disjuntos — backend, frontend
e limpeza de `docs/` — nenhum deles rodando comando git. A sessão principal fez o `.gitignore`, o
compose, o `.env.exemplo`, a paridade da documentação e todos os nove commits em série.

**Duas coisas mudaram de rumo no meio da sessão, a pedido do desenvolvedor.** A primeira: a
camada `frontend/src/api/` — que o agente B tinha escrito por completo, com `client.ts`,
`erros.ts`, contratos Zod e 8 testes passando — foi **apagada**. A segunda, que é a razão da
primeira: ficou registrado no `CLAUDE.md` como **regra nº 3** que o código de aplicação é escrito
pelos membros da equipe, não pela LLM, porque este é um trabalho de faculdade e o objetivo é o
time aprender as ferramentas. A LLM funda, configura, revisa e explica; a equipe escreve
controllers, services, entidades, `src/api/`, componentes, telas e as migrations.

**O que não foi verificado, e é preciso dizer com clareza:** o usuário não está no grupo `docker`,
então as duas verificações que dependem do daemon — subir a infraestrutura e rodar o teste de
integração — **não rodaram**. Em particular, **continua sem prova de que o Flyway 12.4.0 aceita o
PostgreSQL 18**, que era o principal risco de versão mapeado pelo plano.

## O que foi feito

- **`backend/` existe e compila.** Spring Boot 4.1.1 sobre Java 21, gerado pelo `start.spring.io`
  e ajustado: MapStruct 1.6.3 dentro dos `annotationProcessorPaths` que já existiam,
  `bcprov-jdk18on:1.85.2` para o `Argon2PasswordEncoder`, BOM do AWS SDK 2.54.5 importado.
  Nenhum override de versão — o Flyway herda 12.4.0 do BOM. Sem pastas vazias de `config/`,
  `lib/`, `comum/` ou `modulos/`.
- **`frontend/` existe e builda.** Next.js 16.3.3, TypeScript 7.0.2 em strict, Tailwind 4.3.3,
  provider do TanStack Query isolado no único `'use client'` do layout. Landing é placeholder
  textual. `src/` contém apenas `app/`.
- **`docker-compose.yml` sobe PostgreSQL 18 e MinIO** com os 3 buckets, volumes nomeados,
  imagens fixadas por tag e init idempotente.
- **A fonte da verdade deixou de mentir sobre quatro coordenadas** de dependência (detalhe na
  seção de problemas), e ganhou a seção `local_infrastructure`, o nome/usuário/porta do banco e a
  nota de que nenhuma extensão do Postgres é necessária.
- **Uma sessão inteira de trabalho foi resgatada:** `docs/historias-usuario-administrador-servicos.md`,
  com HU 11–15 produzidas em 23/08, estava sem versionamento havia quatro dias.
- **`CLAUDE.md` ganhou a regra nº 3** e teve duas divergências corrigidas.

## Decisões tomadas

| Decisão | Motivo | ADR |
|---|---|---|
| Código de aplicação é escrito pela equipe, não pela LLM | Trabalho de faculdade; o objetivo é aprender as ferramentas. Código gerado e aceito sem ser escrito não ensina | Não requer — regra de processo, vive no `CLAUDE.md` |
| Migrations Flyway também são da equipe | O ADR-0006 saiu do Supabase justamente para "permitir estudo aprofundado de banco de dados" | Não requer — mesma regra |
| Flyway herda 12.4.0 do BOM, sem override | Não manter override de biblioteca que o BOM já resolve, e não revalidar compatibilidade a cada bump do Boot | Nota de revisão adicionada ao [ADR-0003](../adr/0003-spring-boot-4-java-21-maven.md) |
| Manter TypeScript 7 com `npm run lint` quebrado | Não reverter uma decisão registrada por pressão de ferramenta defasada, nem manter dois TypeScript no projeto | Não requer — registrado na fonte da verdade e no README |
| Volume do Postgres monta em `/var/lib/postgresql` | Erro objetivo: a imagem do PG18 mudou o `PGDATA`; o caminho antigo não persiste dados | Não requer — correção factual |
| `local_infrastructure` entra como §7, renumerando quatro seções | Espelhar a ordem do JSON, como manda a regra nº 1 | Não requer |
| Deixar o nome antigo do schema na prosa histórica | Reescrever relatório de sessão passada falsifica o registro daquela data | Não requer |
| RNF003 fala em "tráfego cifrado com TLS", não "HTTPS" | A conexão backend↔PostgreSQL não é HTTP; exigir HTTPS nela seria requisito impossível como escrito | Não requer |

## Arquivos alterados

| Arquivo | Alteração |
|---|---|
| `backend/**` | Criado — projeto Spring Boot 4.1.1 (13 arquivos versionados) |
| `frontend/**` | Criado — fundação Next.js 16.3.3 (15 arquivos versionados) |
| `docker-compose.yml`, `.env.exemplo` | Criados — PostgreSQL 18, MinIO e init de buckets |
| `.gitignore` | Alterado — cobria só `node_modules`, `.vscode` e `.env`; ganhou `target/`, `.next/`, `*.class`, `coverage/`, `.idea/`, `*.log`, `.DS_Store` |
| `docs/arquitetura-sistema.json` | Alterado — versão 2.1.0, `local_infrastructure`, 4 coordenadas corrigidas, dados do banco |
| `docs/design-sistema.md` | Alterado — espelho do JSON, nova §7, renumeração §7–§10 → §8–§11 |
| `CLAUDE.md` | Alterado — regra nº 3; `lib/` e a regra de referência entre módulos corrigidas |
| `docs/adr/0003-*.md` | Alterado — nota de revisão sobre a faixa do Flyway |
| `docs/adr/0004-*.md`, `docs/adr/0005-*.md`, `docs/README.md` | Alterados — referências de seção renumeradas |
| `docs/requisitos.{json,md}` | Alterados — RNF003 e RNF010 sem Supabase; referências de seção |
| `docs/matriz-rastreabilidade.md` | Alterado — data de sincronização, referências, prosa da §4 |
| `docs/historias-usuario-administrador-servicos.md` | Criado — trabalho de 23/08 finalmente versionado |
| `docs/PBB_Template.pptx.png` | Removido — export intermediário, nunca versionado |
| `docs/schema bico em casa.png` | Renomeado para `docs/schema-bico-em-casa.png` |
| `frontend/src/api/**` | **Criado e depois removido** — 6 arquivos, por decisão da regra nº 3 |

## Verificações executadas

| Comando | Resultado |
|---|---|
| `jq empty docs/arquitetura-sistema.json` | ✅ exit 0 |
| `cd backend && ./mvnw -q -B clean compile` | ✅ exit 0 |
| `cd backend && ./mvnw -q -B clean test-compile` | ✅ exit 0 |
| `cd backend && ./mvnw -B dependency:tree` | ✅ Flyway **12.4.0**, Testcontainers **2.0.5**, MapStruct 1.6.3, bcprov 1.85.2, awssdk s3 2.54.5, springdoc 3.1.0 |
| `cd frontend && npx tsc --noEmit` | ✅ exit 0 |
| `cd frontend && npm run build` | ✅ exit 0 |
| `cd frontend && npm test -- --run` | ✅ exit 0 — **mas sem nenhum teste** (`passWithNoTests`); antes da remoção de `src/api/` eram 8 testes passando |
| `cd frontend && npm run lint` | ❌ exit 2 — `typescript-eslint does not support TS 7.0` |
| `docker compose config` | ✅ exit 0 |
| `docker compose up -d && docker compose ps` | ⏭️ **não executado** — `permission denied on /var/run/docker.sock`; o usuário não está no grupo `docker` |
| `cd backend && ./mvnw -B test` | ⏭️ **não executado** — mesmo motivo. **O Flyway 12.4.0 contra PostgreSQL 18 segue sem prova** |
| `find backend frontend -type d -empty` | ✅ vazio em código fonte |
| `ls frontend/src` | ✅ só `app` |
| `git status` | ✅ working tree limpo |
| Paridade `requisitos.json` ↔ `requisitos.md` | ✅ 46/46 descrições idênticas, 0 divergências |
| Referências `design-sistema.md §N` | ✅ todas apontam para seção existente |

## Problemas encontrados

**1. O volume do PostgreSQL 18 quase nasceu quebrado, em silêncio.** A imagem oficial do PG18
mudou o `PGDATA` para `/var/lib/postgresql/18/docker` e passou a declarar o `VOLUME` em
`/var/lib/postgresql`. Montar o volume nomeado em `/var/lib/postgresql/data` — o caminho que valeu
até o PG17 e que o plano assumia — cria um diretório que ninguém usa, e **os dados não sobrevivem
ao primeiro `docker compose down`**, sem erro nenhum. Confirmado lendo o `Dockerfile` do
`docker-library/postgres`. Corrigido antes do commit.

**2. `npm run lint` não roda, e isso vai reprovar o CI.** O `typescript-eslint`, dependência do
`eslint-config-next`, aborta em tempo de execução com `typescript-eslint does not support TS 7.0`
([#10940](https://github.com/typescript-eslint/typescript-eslint/issues/10940)). Não é aviso de
peer dependency — é barreira ativa. `typecheck`, `build` e teste não são afetados. O `.npmrc` com
`legacy-peer-deps=true` existe pelo mesmo motivo: sem ele o `npm install` nem termina.
**Consequência a não esquecer:** quando o `frontend-ci.yml` for escrito, a etapa de ESLint precisa
nascer desligada ou com `continue-on-error`.

**3. `application-prod.yml` não pôde ser escrito pela LLM.** O `.claude/settings.json` tem
`Read(./**/application-prod.yml)` no `deny`, e o harness aplica a regra de leitura também à
escrita. O agente A **não tentou contornar** — comportamento correto. O arquivo foi criado pelo
próprio desenvolvedor via `!`. **Seu conteúdo não foi verificado pela LLM**, porque a mesma regra
bloqueia a leitura.

**4. As quatro imprecisões da fonte da verdade, todas confirmadas contra o Maven Central:**

| Documentado antes | Realidade |
|---|---|
| `org.testcontainers:junit-jupiter:2.0.x` e `:postgresql:2.0.x` | **Não existem.** O Testcontainers 2.0 renomeou para `testcontainers-junit-jupiter` e `testcontainers-postgresql` |
| `flyway-core:13.3.x` | O BOM do Boot 4.1.1 gerencia **12.4.0**. A 13.3.0 existe, mas a decisão foi herdar o BOM |
| `spring-boot-starter-web` | Deprecado no Boot 4.1 em favor de `spring-boot-starter-webmvc`. Idem `-oauth2-resource-server` → `-security-oauth2-resource-server` |
| `spring-boot-starter-test` monolítico | O Boot 4.1 modularizou por fatia; `spring-boot-starter-security-test` substitui `spring-security-test` |

**5. O agente C recusou-se a zerar o `grep` do nome antigo do schema, e estava certo.** Sobraram 4
ocorrências de `schema bico em casa` em prosa histórica (2 relatórios, 2 no próprio plano). O
critério de aceite do plano pedia grep vazio, mas cumpri-lo exigiria reescrever relatório de
sessão passada — o que falsifica registro — e apagar o "antes" da tabela `X → Y` do plano, que
destrói o sentido dela. Decisão do desenvolvedor: **deixar como está**.

**6. Duas descobertas fora do escopo autorizado, ambas levadas ao desenvolvedor.** O RNF010 tinha
o mesmo resíduo de Supabase do RNF003 ("alterar schema pelo painel do Supabase é proibido"), num
requisito vigente — corrigido no mesmo commit. E a renumeração das seções do `design-sistema.md`
invalidou **19 referências** espalhadas por `requisitos`, matriz, `docs/README` e dois ADRs; a
primeira varredura usou padrão estreito demais e deixou 4 passarem, pegas numa segunda varredura.

**7. Nuances geradas pelos agentes, auditadas (pergunta nº 4 do checklist).** Nenhuma ficou sem
decisão explícita:

| Nuance | Destino |
|---|---|
| Agente A removeu `static/` e `templates/` vazias do Initializr | **Mantida** — a regra de pasta vazia exigia |
| Agente A trocou o `TestcontainersConfiguration` do Initializr pelo padrão `@Container static` | **Mantida** — a tarefa autorizava |
| Agente A escreveu "referencie pelo `id` (UUID)" no README | **Revertida** — contraria o ADR-0007 (chave mista) |
| Agente A registrou no README que o teste "não foi executado nesta máquina" | **Revertida** — fato temporal não vive em README permanente; foi para este relatório |
| Agente B subiu ESLint de `^9.38` para `^10.9.1` | **Mantida** — a 9.x resolvida vinha marcada como sem suporte |
| Agente B criou `.npmrc` com `legacy-peer-deps` | **Mantida e documentada** — sem ela o `npm install` não termina |
| Agente B pôs o provider em `src/app/provedores.tsx` em vez de `components/` | **Mantida** — `components/` não pode nascer com um arquivo só |
| Agente B ligou `noUncheckedIndexedAccess` | **Mantida** — coerente com "strict mode" |
| Agente C reescreveu o RNF003 em vez de trocar a palavra | **Mantida** — o argumento técnico procede |

## Pendências

Da lista do plano, **nenhuma foi fechada nesta sessão** — todas continuam abertas:

- [ ] **ADR-0009 (geocodificação)** — único requisito do MVP sem caminho técnico (RF013/RNF018). Bloqueia o épico de endereço
- [ ] **Migrations Flyway `V1`–`V6`** — as 18 tabelas. É a próxima task, e agora é **da equipe**, não da LLM
- [ ] **PR #2 aberto sem review** desde 23/08 — `main` ainda não tem requisitos v1.1.0, modelo v4 nem ADR-0006/0007/0008
- [ ] **`LICENSE` vazio** (0 bytes) — arrastado por quatro relatórios
- [ ] **ADR do `utils/` no frontend** — `cn()` (clsx + tailwind-merge) não tem casa definida
- [ ] **Node 24 (documentado) vs Node 26.5.1 (máquina)** — não bloqueia, mas a doc está desatualizada
- [ ] **`@tanstack/react-query`**: doc diz 5.101.x, instalado é 5.102.7 — deriva menor a alinhar
- [ ] **Remote `origin` em HTTPS** (era SSH) — mudança de contorno nunca revisada
- [ ] **Trello com 0 cards** para um backlog de 404h
- [ ] **Três fontes de documentação** (Drive, Notion, repositório) sem dono definido
- [ ] **Upload do mapa de contexto ao Drive** não confirmado

Abertas por esta sessão:

- [ ] **Entrar no grupo `docker`** — `sudo usermod -aG docker $USER` em terminal próprio, e relogar
- [ ] **Rodar `./mvnw test`** depois disso, para provar que o Flyway 12.4.0 aceita o PostgreSQL 18
- [ ] **`npm run lint` quebrado** com TypeScript 7 — o `frontend-ci.yml` precisa nascer com a etapa de ESLint desligada
- [ ] **`frontend/vitest.config.ts` tem `passWithNoTests: true`** — remover quando a equipe escrever o primeiro teste, para ausência de teste voltar a doer
- [ ] **`application-prod.yml` não foi conferido pela LLM** — a regra de `deny` bloqueia a leitura
- [ ] **A branch não foi enviada ao remoto e nenhum PR foi aberto** — por instrução explícita

## Próximos passos

1. **Entrar no grupo `docker` e rodar `docker compose up -d` e `cd backend && ./mvnw test`.** É a
   única verificação que falta da fundação, e é a que cobre o risco de versão que sobrou.
2. **Revisar os 9 commits** e decidir sobre o push e o PR.
3. **Fechar o PR #2**, que está parado há quatro dias e segura requisitos v1.1.0, modelo v4 e três
   ADRs fora da `main`.
4. **Escrever as migrations `V1`–`V6`** a partir de `docs/modelo-dados.dbml` — trabalho da equipe,
   com a LLM revisando.
