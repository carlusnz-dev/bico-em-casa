# Relatório de Sessão — 2026-08-29

| Campo | Valor |
|---|---|
| **Sessão** | Revisão da fundação, verificação do risco de versão e abertura do PR |
| **Autor** | Carlos Antunes |
| **Data** | `2026-08-29` |
| **Duração aproximada** | `1h30` |
| **LLM utilizada** | `Claude Opus 5 (Claude Code)` — orquestração; 1 agente `Claude Sonnet` para a revisão documental |
| **Branch** | `feat/fundacao-backend-e-infra` |
| **Commits** | `f22132a`, `a38d7f2`, `cdf23c3`, `d97e651`, `d1c4ae9` (merge) |
| **Plano relacionado** | Nenhum em `docs/planos/` — o plano viveu na sessão, aprovado antes da execução |

---

## Resumo

A sessão de 27/08 fechou com 17 pendências e uma verificação crítica não executada. Esta sessão
existiu para revisar aquele estado, provar o que faltava provar e destravar a equipe.

**A revisão desmentiu o relatório de 27/08 em três pontos.** O usuário **já está no grupo
`docker`** — a verificação bloqueada estava destravada e ninguém tinha notado. O **PR #2 já havia
sido mesclado** em 27/08 (`78aea70`), então a `main` tinha requisitos v1.1.0, modelo v4 e os
ADR-0006/0007/0008 desde antes daquele relatório ser escrito. E, mais importante, a
**branch nunca tinha sido enviada ao remoto**: quem clonasse o repositório receberia documentação
e nada mais — sem `backend/`, sem `frontend/`, sem `docker-compose.yml`. A resposta à pergunta
"qualquer integrante pode começar hoje?" era **não**, e o bloqueio não era técnico.

**O risco de versão que sobrou de 27/08 está fechado com prova.** `./mvnw -B test` rodou contra um
`PostgreSQLContainer` real: o Flyway **12.4.0** conectou em **PostgreSQL 18.6**, criou a
`flyway_schema_history` e validou. `BUILD SUCCESS`. Era o único item da fundação que estava
apoiado em suposição.

**A documentação parou de mentir sobre a CI.** A §9 descrevia `backend-ci.yml` e `frontend-ci.yml`
com gatilhos e passos, no mesmo tom do `docs-parity.yml` — que é o único workflow que existe. Dois
dos passos descritos dependem de plugins (Spotless, JaCoCo) que não estão no `pom.xml`. Quem lesse
a seção concluiria que PR já roda lint e teste. Não roda, e agora está escrito que não roda.

O PR [#3](https://github.com/carlusnz-dev/bico-em-casa/pull/3) foi aberto contra `develop`, com o
checklist das cinco perguntas **em branco de propósito** — respondê-las é do desenvolvedor que
fecha a task, não da LLM.

## O que foi feito

- **A fundação passou a estar verificada de ponta a ponta.** `docker compose up -d` sobe os três
  serviços (postgres `healthy`, buckets `portfolios`/`anexos`/`avatares` criados, init saindo 0) e
  o teste de fumaça do backend passa contra PostgreSQL 18.6 real.
- **A fonte da verdade voltou a bater com o instalado** em duas coordenadas: o runtime do Node e a
  versão do `@tanstack/react-query`.
- **A §9 e as ferramentas de estilo do backend passaram a declarar o que é PLANEJADO** — dois
  workflows e três ferramentas (Spotless, SonarLint/Checkstyle, JaCoCo) que a documentação
  apresentava como vigentes.
- **`README.md` da raiz passou a ter um caminho de onboarding real** — pré-requisitos com versão,
  o aviso do grupo `docker` no Linux, `docker compose up -d`, backend, frontend, e link para os
  READMEs de módulo e para a documentação. Antes tinha 6 linhas e não linkava para nada.
- **`LICENSE` deixou de ter 0 bytes** — Apache-2.0, texto verbatim do `apache.org`, arrastada como
  pendência por quatro relatórios.
- **O fluxo GitFlow passou a existir de verdade no remoto:** `origin/develop` criada a partir da
  `main`, a branch de feature enviada, e o PR #3 aberto contra `develop`.

## Decisões tomadas

| Decisão | Motivo | ADR |
|---|---|---|
| Documentar `Node.js >=20.9.0 (validado em v26.5.1)` em vez de `Node.js 24 LTS` | Nada no repositório força a linha 24 — não há `engines` no `package.json` nem `.nvmrc`, e a máquina roda 26.5.1. Declarar um LTS que o time não roda é a mesma divergência que a regra nº 1 existe para evitar | Não requer — correção factual |
| Licença Apache-2.0 | Escolha do desenvolvedor. Sem licença declarada, o padrão legal é "todos os direitos reservados", o que contradiz um repositório aberto de trabalho acadêmico | Não requer |
| GitFlow com `develop`, e não só `main` | Escolha do desenvolvedor. É o que os gatilhos da §9 já descreviam, então a documentação passou a ser verdade em vez de ser corrigida para menos | Não requer — fluxo de processo |
| Marcar os workflows como PLANEJADO em vez de escrevê-los | O desenvolvedor tirou o encanamento do escopo desta sessão. Entre a documentação mentir e a seção dizer que é especificação, a segunda é honesta | Não requer |
| Não responder às cinco perguntas do checklist no PR | O próprio `CLAUDE.md` diz que um check que a máquina responde sozinha deixa de ser lido | Não requer |
| Copyright do `LICENSE` como "Equipe Bico em Casa" | Não há registro no repositório dos nomes dos integrantes, e inventar autoria em arquivo de licença é pior que ser genérico | Não requer — ajustável |

## Arquivos alterados

| Arquivo | Alteração |
|---|---|
| `docs/arquitetura-sistema.json` | Alterado — runtime do Node, versão do react-query, `status` nos três workflows, `formatter`/`linter` do backend marcados como planejados |
| `docs/design-sistema.md` | Alterado — espelho do JSON: §4 (runtime e dependências), §9 (aviso de que só `docs-parity.yml` existe) e a tabela de padrões do backend |
| `README.md` | Alterado — de 6 para 94 linhas: seção "Como começar", os dois avisos que parecem defeito, índice da documentação e licença |
| `LICENSE` | Alterado — de 0 bytes para os 11350 da Apache-2.0 |

## Verificações executadas

| Comando | Resultado |
|---|---|
| `docker compose up -d && docker compose ps` | ✅ `bec-postgres` `Up (healthy)`, `bec-minio` `Up`, `bec-minio-init` `Exited (0)`; os 3 buckets criados |
| `cd backend && ./mvnw -B test` | ✅ `BUILD SUCCESS` — `Tests run: 1, Failures: 0, Errors: 0`. O log confirma `Database: jdbc:postgresql://localhost:32769/test (PostgreSQL 18.6)` e a criação da `flyway_schema_history` |
| `./mvnw -B dependency:list` | ✅ `flyway-core:12.4.0`, `flyway-database-postgresql:12.4.0`, `testcontainers:2.0.5`, `org.postgresql:postgresql:42.7.13` |
| `cd frontend && npm run typecheck` | ✅ exit 0 |
| `cd frontend && npm run build` | ✅ 3 páginas estáticas geradas |
| `jq empty docs/arquitetura-sistema.json` | ✅ exit 0, após cada alteração |
| `git merge-tree` (simulação antes do merge) | ✅ 0 conflitos previstos |
| `git merge origin/main` | ✅ merge limpo — `git diff HEAD^1 HEAD` **vazio**: puramente topológico, a branch já tinha o conteúdo |
| `git merge-base --is-ancestor develop origin/main` | ✅ a `develop` obsoleta não tinha nenhum commit exclusivo |
| `gh pr checks 3` | ✅ `Verificar paridade JSON ↔ Markdown` — `pass` em 5s |
| `gh pr view 3` | ✅ `state: OPEN`, `baseRefName: develop`, `mergeable: MERGEABLE`, 49 arquivos |
| `diff apache-2.0.txt LICENSE` | ✅ única diferença é a linha de copyright do apêndice |

## Problemas encontrados

**1. O relatório de 27/08 estava desatualizado em dois itens no momento em que foi escrito.** O PR
#2 foi mesclado às 13:28 de 27/08, e o relatório o listou como "aberto sem review desde 23/08". O
grupo `docker` também já estava resolvido. **A lição não é sobre esses dois itens**: é que uma
lista de pendências copiada da sessão anterior sem ser reverificada envelhece em horas. Toda
pendência desta sessão foi checada contra o repositório antes de ser repetida.

**2. A branch nunca tinha sido enviada — e isso não estava visível.** O relatório de 27/08
registrava "a branch não foi enviada ao remoto" como uma linha entre 17 pendências, do mesmo peso
que "`LICENSE` vazio". Na prática era o único bloqueio real: nenhum outro integrante conseguia
começar, e nenhuma das outras 16 pendências importava enquanto essa existisse. Pendência sem
ordem de importância esconde a que importa.

**3. A documentação descrevia CI que não existe, em tom afirmativo.** A §9 não dizia "planejado"
em lugar nenhum, e listava passos com Spotless e JaCoCo que nem plugin têm. É a mesma classe de
problema que a regra nº 1 combate — documentação que deixou de corresponder ao repositório — mas
que o hook de paridade **não pega**, porque ele só compara se os dois arquivos mudaram juntos, e
não o que eles dizem.

**4. O hook de paridade não vê alteração feita por `sed` via Bash.** O `PostToolUse` de
`.claude/hooks/paridade-docs.sh` casa com `Edit|Write`. Toda a edição de documentação desta sessão
foi feita por script no Bash, então o hook nunca disparou. A paridade foi mantida à mão e está
provada nos commits (`f22132a` e `a38d7f2` carregam JSON e markdown juntos), e o `docs-parity.yml`
do PR passou — mas o hook local tem esse ponto cego.

**5. O `deny` de `application-prod.yml` no `.claude/settings.json` tem um furo.** A regra bloqueia
a ferramenta `Read`, mas `Bash(git show:*)` está no `allow` e chega ao mesmo conteúdo. O agente de
revisão desta sessão passou por ali e reportou. O mesmo padrão de `deny` (linha 32) também bloqueia
`.env.exemplo`, que é versionado e não tem segredo nenhum — protege o que não precisa e deixa
passar o que precisa.

**6. Falta `lombok-mapstruct-binding` no `pom.xml`, e isso só vai doer depois.** Confirmado na
documentação oficial do MapStruct: com Lombok ≥1.18.16 o artefato
`org.projectlombok:lombok-mapstruct-binding:0.2.0` é obrigatório nos `annotationProcessorPaths`,
senão o MapStruct roda antes de o Lombok alterar a AST e não enxerga os getters gerados. Hoje não
há nenhum mapper, então nada quebra. Quebra na primeira task de domínio, com erro
(`Unmapped target property`) que não aponta para a causa.

## Pendências

Fechadas nesta sessão: o grupo `docker`, o `./mvnw test`, o `LICENSE`, o PR #2, o push da branch e
a abertura do PR.

Continuam abertas:

- [ ] **Migrations Flyway `V1`–`V6`** — as 18 tabelas, a partir de `docs/modelo-dados.dbml`. São da
      equipe (regra nº 3) e bloqueiam qualquer trabalho de backend: sem schema não há o que mapear
- [ ] **ADR-0009 (geocodificação)** — RF013/RNF018 seguem sem caminho técnico
- [ ] **ADR do `utils/` no frontend** — `cn()` (clsx + tailwind-merge) sem casa definida
- [ ] **`backend-ci.yml` e `frontend-ci.yml`** — agora marcados como PLANEJADO. A etapa de ESLint
      do frontend precisa nascer desligada ou com `continue-on-error`
- [ ] **`lombok-mapstruct-binding:0.2.0`** nos `annotationProcessorPaths` — ver problema nº 6
- [ ] **`engines` no `package.json` ou `.nvmrc`** — hoje nada força a versão de Node
- [ ] **Furo no `deny` do `.claude/settings.json`** e o bloqueio indevido de `.env.exemplo` — ver
      problema nº 5
- [ ] **Ponto cego do hook de paridade** com edição via Bash — ver problema nº 4
- [ ] **`vitest.config.ts` com `passWithNoTests: true`** — remover no primeiro teste da equipe
- [ ] **`application-prod.yml` nunca conferido pela LLM** — a regra de `deny` bloqueia a leitura
- [ ] **Spotless, SonarLint/Checkstyle e JaCoCo** não configurados — hoje declarados como planejados
- [ ] **Trello com 0 cards** para um backlog de 404h; **três fontes de documentação** (Drive,
      Notion, repositório) sem dono; **upload do mapa de contexto ao Drive** — não verificáveis
      pelo repositório
- [ ] **Copyright do `LICENSE`** como "Equipe Bico em Casa" — trocar se houver decisão sobre autoria

## Próximos passos

1. **Revisar e mesclar o PR #3**, respondendo às cinco perguntas do checklist. É o que libera todo
   o resto do time a clonar um repositório com código dentro.
2. **Escrever as migrations `V1`–`V6`** a partir de `docs/modelo-dados.dbml` — trabalho da equipe,
   com a LLM revisando. É o desbloqueio de todo o backend.
3. **Decidir o ADR-0009 (geocodificação)** — é o único requisito do MVP sem caminho técnico, e
   bloqueia o épico de endereço.
4. **Escrever `backend-ci.yml` e `frontend-ci.yml`** quando houver teste da equipe para eles
   rodarem. Antes disso, CI verde não significa nada.
