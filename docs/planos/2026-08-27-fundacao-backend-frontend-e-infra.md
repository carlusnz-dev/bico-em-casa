# Plano — Fundação do backend, do frontend e da infraestrutura local

| Campo | Valor |
|---|---|
| **Plano** | Fundação do backend, do frontend e da infraestrutura local |
| **Autor** | Carlos Antunes |
| **Data** | `2026-08-27` |
| **Escopo** | Backend · Frontend · Infra · Documentação |
| **Status** | Aprovado |
| **LLM utilizada** | `Claude Opus 5 (Claude Code)` — planejamento; execução prevista com 2 agentes `Claude Sonnet 5` |
| **ADRs relacionados** | ADR-0003, ADR-0004, ADR-0005, ADR-0006, ADR-0007, ADR-0008 |

---

## Contexto

O `bico-em-casa` tem 20 commits e **nenhuma linha de código de aplicação**. Tudo que existe é
documentação e arquitetura: `docs/arquitetura-sistema.json` (fonte da verdade) já descreve
`backend/`, `frontend/`, o pacote `br.com.bicoemcasa.api`, os 6 módulos v1, a lista de
dependências das duas pontas e a política de testes. ADR-0003 a ADR-0008 já decidiram Java 21 +
Spring Boot 4.1 + Maven, a estrutura modular por domínio, `src/api/` como fronteira única do
frontend, a saída do Supabase para infraestrutura própria (Postgres + MinIO em Docker,
identidade própria com JWT RSA + Argon2id), a chave primária mista e a nomenclatura de tabelas.

O que falta é o repositório **parar de só descrever e começar a existir**.

O levantamento de pendências feito na sessão de planejamento mostrou que **nenhuma pendência
estrutural das sessões de 22/08 foi fechada** — `docker-compose.yml`, migrations Flyway,
`.gitignore` incompleto e `LICENSE` vazio se repetem em 3 ou 4 relatórios seguidos, sem nenhuma
marca de resolução. Além disso, existe uma sessão de trabalho inteira que nunca foi commitada
nem relatada: `docs/historias-usuario-administrador-servicos.md`, com 5 histórias de usuário
completas (HU 11–15, RF025–029) produzidas em 23/08. É o maior risco de perda de trabalho no
repositório hoje.

### Fatos verificados na sessão de planejamento

Cada coordenada abaixo foi conferida em `repo1.maven.org` / `registry.npmjs.org`, e o `pom.xml`
real gerado pelo `start.spring.io` para esta combinação foi inspecionado antes de o plano ser
escrito. **Não presuma nada disto — está verificado, mas reconfira ao executar.**

**Backend — a documentação tem quatro imprecisões:**

| Documentado em `arquitetura-sistema.json` | Realidade verificada |
|---|---|
| `org.testcontainers:junit-jupiter:2.0.x` e `org.testcontainers:postgresql:2.0.x` | **Não existem.** O Testcontainers 2.0 renomeou os módulos para `testcontainers-junit-jupiter` e `testcontainers-postgresql` (confirmado no `testcontainers-bom:2.0.5`). O Initializr já emite os nomes novos, então o build não quebra — mas a fonte da verdade está errada |
| `flyway-core:13.3.x` | O BOM do Boot 4.1.1 gerencia **12.4.0**, e o Initializr usa `spring-boot-starter-flyway` + `flyway-database-postgresql`. **Decisão do usuário: seguir o BOM**, sem `<flyway.version>` |
| `spring-boot-starter-web` | **Deprecado no Boot 4.1** — o POM do próprio artefato diz *"deprecated in favor of spring-boot-starter-webmvc"*. Idem `spring-boot-starter-oauth2-resource-server` → `spring-boot-starter-security-oauth2-resource-server`. Os antigos ainda resolvem |
| `spring-boot-starter-test` + `org.springframework.security:spring-security-test` | O Boot 4.1 **modularizou os starters de teste** por fatia (`spring-boot-starter-webmvc-test`, `-security-test`, `-data-jpa-test`…). Os antigos ainda resolvem, mas o Initializr emite os modulares |

Verificados e **corretos**: Spring Boot 4.1.1, springdoc-openapi 3.1.0 (faixa `>=4.0.0 <4.2.0-M1`),
MapStruct 1.6.3, `bcprov-jdk18on` 1.85.2, `software.amazon.awssdk:s3` 2.54.5, Lombok 1.18.46,
`postgres:18.6-alpine`.

**Frontend — a documentação está correta.** `next@16.3.3`, `react@19.2.8`, `typescript@7.0.2`,
`tailwindcss@4.3.3`, `zod@4.4.3`, `zustand@5.0.15`, `@tanstack/react-query@5.102.7`,
`vitest@4.1.11`, `@playwright/test@1.62.1` — todos publicados e conferem com o que está escrito.

**`CLAUDE.md` contradiz a fonte da verdade:** diz que `lib/` tem `minio/`, `email/`,
`geocodificacao/`; `arquitetura-sistema.json:367-372` diz `armazenamento/` e `email/`, e
`geocodificacao/` **não existe** enquanto o ADR-0009 não escolher o fornecedor.

### Gaps preenchidos por este plano

Nome do banco, usuário e portas **nunca foram especificados** em documento nenhum. Este plano
fixa e **documenta na fonte da verdade**: banco `bicoemcasa`, usuário `bicoemcasa`, porta 5432,
MinIO 9000 (API) / 9001 (console). Nenhuma extensão Postgres é necessária — `gen_random_uuid()`
é nativo do PG13+, e isso passa a estar dito explicitamente em vez de implícito.

### Decisões já tomadas pelo usuário

1. O compose sobe **PostgreSQL + MinIO** (com init de buckets). Mailpit/SMTP fica para a sessão que implementar e-mail.
2. **Flyway herda a versão do BOM** (12.4.0).
3. Limpeza autorizada nos 4 itens: commitar as histórias de usuário, apagar `PBB_Template.pptx.png`, renomear `schema bico em casa.png` para kebab-case, completar o `.gitignore`.
4. Branch nova **`feat/fundacao-backend-e-infra`**, saindo de `docs/requisitos-e-matriz-rastreabilidade`. O PR #2 (só docs) fica intocado.
5. **O frontend também é fundado nesta execução**, por 2 agentes Sonnet em paralelo — um por ponta. *Isto reverte a instrução inicial de "frontend sem nada"; ver a nota abaixo.*

> **Nota sobre a reversão do escopo do frontend.** No começo do planejamento a instrução era
> deixar `frontend/` vazia ("ainda vou definir"). O pedido final passou a ser fundar as duas
> pontas com um agente Sonnet cada. Isso é sustentável **porque a stack do frontend já está
> decidida e verificada** — `arquitetura-sistema.json:123-186` fixa framework, versões,
> bibliotecas de estado, formulário, validação e teste, e ADR-0005 fixa a estrutura de pastas.
> O agente do frontend **implementa o que já está documentado**; ele não escolhe stack, não
> desenha telas e não inventa componente. O que ficou "a definir" é o produto visual, e isso
> continua fora deste plano.

## Objetivo

Ao final, o repositório tem `backend/` compilando com `./mvnw clean compile`, `frontend/` com
`npm run build` e `npx tsc --noEmit` passando, um `docker-compose.yml` que sobe PostgreSQL 18 e
MinIO com os 3 buckets criados, e a documentação (`arquitetura-sistema.json` +
`design-sistema.md` + `CLAUDE.md`) refletindo exatamente o que foi construído.

Verificável: alguém clona o repositório, roda `docker compose up -d`, `cd backend && ./mvnw test`
e `cd frontend && npm ci && npm run build`, e as três coisas funcionam.

## Fora de Escopo

- **Migrations Flyway `V1`–`V6`** — as 18 tabelas são a próxima task, não esta. `db/migration/` nasce vazia com `.gitkeep`
- **ADR-0009 (geocodificação)** — segue pendente; sem ele não se cria `lib/geocodificacao/`
- **Qualquer módulo de domínio** — nenhum controller, service, entidade ou endpoint de negócio
- **Telas e design do frontend** — só a fundação técnica; nenhum layout de produto
- **Mailpit/SMTP no compose** — decisão do usuário, fica para a sessão que implementar e-mail
- **`LICENSE` vazio** — pendência antiga, não é desta sessão
- **Review/merge do PR #2** e **push da branch nova** — nada é enviado ao remoto sem o usuário pedir

## Pré-requisitos

- [ ] **Estar no grupo `docker`.** Hoje `docker ps` falha com *permission denied on /var/run/docker.sock* e `id -nG` não lista `docker`. Rodar **em terminal próprio, fora do Claude Code** (`sudo` não funciona de dentro dele): `sudo usermod -aG docker $USER` e depois relogar, ou `newgrp docker`. **Sem isso, as etapas 9 e 10 não rodam.**
- [ ] Java 21 disponível (`java -version` → 21.0.11 ✅ já confirmado)
- [ ] Node ≥ 20.9 (`node -v` → v26.5.1 ✅ satisfaz o `engines` do `next@16.3.3`)
- [ ] Acesso à internet para `start.spring.io`, `repo1.maven.org` e `registry.npmjs.org`
- [ ] `mvn` **não** está instalado e **não precisa estar** — o projeto nasce com o Maven Wrapper

## Etapas

| # | Etapa | Arquivos afetados | Critério de aceite |
|---|---|---|---|
| 1 | Criar a branch `feat/fundacao-backend-e-infra` a partir de `docs/requisitos-e-matriz-rastreabilidade` | — | `git branch --show-current` retorna o nome novo |
| 2 | **Agente Sonnet C (limpeza)** — só `docs/`, **sem nenhum comando git** | `docs/PBB_Template.pptx.png` (apagado), `docs/schema bico em casa.png` → `docs/schema-bico-em-casa.png`, `docs/requisitos.{json,md}` | `PBB_Template.pptx.png` não existe; nenhuma referência ao nome antigo sobrou (`grep -r "schema bico em casa" docs/` vazio); RNF003 não cita mais Supabase |
| 3 | Completar o `.gitignore` (feito pela sessão principal, não pelo agente) | `.gitignore` | `target/`, `.next/`, `*.class`, `.idea/`, `.env*.local`, `coverage/`, `*.log`, `.DS_Store` presentes; `!.mvn/wrapper/maven-wrapper.jar` presente |
| 4 | **Agente Sonnet A (backend)** — gerar via Initializr e ajustar o `pom.xml` | `backend/**` | `cd backend && ./mvnw -q -B clean compile` sai com código 0 |
| 5 | **Agente Sonnet B (frontend)** — fundação Next.js | `frontend/**` | `cd frontend && npm run build` e `npx tsc --noEmit` saem com código 0 |
| 6 | `docker-compose.yml` + `.env.exemplo` na raiz | `docker-compose.yml`, `.env.exemplo` | `docker compose config` sai com código 0 |
| 7 | READMEs das duas pastas | `backend/README.md`, `frontend/README.md` | Cada um explica stack, como rodar, estrutura e as convenções que valem ali |
| 8 | Paridade da documentação — **JSON primeiro, MD no mesmo commit** | `docs/arquitetura-sistema.json`, `docs/design-sistema.md`, `CLAUDE.md` | `jq empty docs/arquitetura-sistema.json` passa; as 4 imprecisões corrigidas; seção `local_infrastructure` presente nos dois |
| 9 | Subir a infraestrutura | — | `docker compose up -d` e `docker compose ps` mostram `postgres` e `minio` de pé, e os 3 buckets criados |
| 10 | Teste de integração de fumaça | `backend/src/test/java/...` | `./mvnw -B test` passa contra Postgres 18 real via Testcontainers |
| 11 | Commits separados e relatório de sessão | `docs/relatorios/2026-08-27-*.md` | 9 commits em Conventional Commits; relatório gerado por `/relatorio-sessao` |

### Detalhamento das etapas que têm armadilha

#### Etapa 2 — por que o agente de limpeza não roda git

Dois processos commitando em paralelo disputam o `.git/index.lock` e um dos dois falha. O agente
**edita, move e apaga arquivos**; a sessão principal faz todo `git add`, `git mv` e `git commit`,
em série, depois que o agente terminar. `/revisar-matriz` também roda na sessão principal, não
no agente.

#### Etapa 4 — geração do backend

```bash
curl -sS https://start.spring.io/starter.zip \
  -d type=maven-project -d language=java -d bootVersion=4.1.1 \
  -d groupId=br.com.bicoemcasa -d artifactId=bico-em-casa -d name=bico-em-casa \
  -d packageName=br.com.bicoemcasa.api -d javaVersion=21 -d packaging=jar \
  -d description='API da plataforma Bico em Casa' \
  -d dependencies=web,validation,actuator,data-jpa,postgresql,flyway,security,oauth2-resource-server,lombok,mail,configuration-processor,springdoc-openapi,testcontainers \
  -o "$SCRATCH/backend.zip" && unzip -q "$SCRATCH/backend.zip" -d backend/
```

`name=bico-em-casa` faz o Initializr gerar a classe **`BicoEmCasaApplication`**, que é o nome
exigido por `arquitetura-sistema.json:365`. **Confirmar após descompactar.**

Ajustes no `pom.xml` gerado:

1. **MapStruct 1.6.3** — acrescentar `mapstruct` às dependências e `mapstruct-processor` **dentro
   dos blocos `annotationProcessorPaths` que já existem** nas execuções `default-compile` e
   `default-testCompile`. Não criar um `<configuration>` novo no nível do plugin: o parent do
   Boot amarra o compiler plugin a execuções nomeadas, e configuração solta é ignorada. Ambos com
   `<version>` explícita — **MapStruct não está no BOM do Boot**, sem versão o Maven não resolve.
2. **`lombok-mapstruct-binding` — não adicionar de saída.** Se o primeiro mapper reclamar de ordem
   de processadores, aí entra o `0.2.0`. Adicionar preventivamente é dívida que talvez nunca seja necessária.
3. **`org.bouncycastle:bcprov-jdk18on:1.85.2`** — exigido pelo `Argon2PasswordEncoder`. Versão explícita, não está no BOM.
4. **`software.amazon.awssdk:s3`** — importar o `software.amazon.awssdk:bom:2.54.5` em `dependencyManagement` e declarar o `s3` sem versão.
5. **Não declarar `<flyway.version>`** — decisão do usuário.
6. Preencher `<description>` e remover os blocos vazios (`<licenses/>`, `<developers/>`, `<scm/>`, `<url/>`) que o Initializr deixa como esqueleto.

**Estrutura de pacotes: não criar pastas vazias.** A convenção do projeto
(`arquitetura-sistema.json:398-421`, `CLAUDE.md`) diz que *pasta só existe quando há mais de um
arquivo daquele tipo*. Como não há nenhum arquivo de domínio ainda, **não se cria** `config/`,
`lib/`, `comum/` nem os 6 módulos. Criar `modulos/autenticacao/` vazia seria inventar estrutura
antes da necessidade — exatamente o que a convenção proíbe. Existem apenas
`BicoEmCasaApplication.java` e `src/main/resources/db/migration/.gitkeep`.

`application.yml` (base): `spring.application.name`, `jpa.hibernate.ddl-auto: validate` (o schema
é do Flyway, nunca do Hibernate), `jpa.open-in-view: false`, Flyway em `classpath:db/migration`,
Actuator expondo só `health`. `application-dev.yml` aponta para `localhost:5432/bicoemcasa`.
`application-prod.yml` nasce **só com variáveis de ambiente, nenhum valor literal**.

> Consequência a registrar no README, não esconder: com `ddl-auto: validate` e Flyway ligado,
> **a aplicação não sobe sem o banco de pé**. É o comportamento correto — falhar cedo em vez de
> subir com schema errado.

#### Etapa 5 — fundação do frontend

Escopo: **fundação técnica, não produto.** O que nasce:

- `package.json` com as dependências de `arquitetura-sistema.json:131-152` e `:177-186`
- `tsconfig.json` em **strict mode** (TypeScript 7.0.x)
- `next.config.ts`, configuração do Tailwind 4.3 e `globals.css`
- `src/app/layout.tsx` — root layout com o provider do TanStack Query
- `src/app/page.tsx` — landing mínima, placeholder textual, **sem design de produto**
- `src/api/client.ts` — wrapper do `fetch` nativo: base URL, injeção do access token, refresh transparente, normalização de erro
- `src/api/erros.ts` — tipo de erro único, traduzido do `ProblemDetail` (RFC 9457) do backend
- `src/api/contratos/` — pelo menos o schema Zod do `ProblemDetail`, com o tipo por `z.infer`
- `vitest.config.ts` e um teste de fumaça
- `frontend/README.md`

Regras inegociáveis (ADR-0005, `CLAUDE.md`):

- **Nenhum `fetch` fora de `src/api/`** — é a única fronteira com o backend
- Toda resposta do backend passa por schema Zod antes de entrar na aplicação
- Tipos são **inferidos** (`z.infer`), nunca escritos à mão em paralelo
- **`any` é proibido** — use `unknown` com narrowing
- **Não criar `types/`, `services/` nem `utils/`** — proibidas sem ADR
- Server Components por padrão; `'use client'` só onde há interatividade
- Mesma regra de pasta vs arquivo: `components/` e `hooks/` **só nascem quando houver conteúdo real** — não criar pastas vazias

#### Etapa 6 — `docker-compose.yml`

| Serviço | Imagem | Portas | Papel |
|---|---|---|---|
| `postgres` | `postgres:18.6-alpine` | 5432 | Banco `bicoemcasa`, volume `bec-postgres-dados`, healthcheck `pg_isready -U bicoemcasa -d bicoemcasa` |
| `minio` | `minio/minio:RELEASE.2025-09-07T16-13-09Z` | 9000 (S3), 9001 (console) | Armazenamento S3, volume `bec-minio-dados` |
| `minio-init` | `minio/mc` | — | Roda uma vez, cria `portfolios`, `anexos`, `avatares` (`arquitetura-sistema.json:213-217`), sai |

Armadilhas que a revisão adversarial pegou, e que **precisam** entrar:

- **Bloco `volumes:` no topo** declarando `bec-postgres-dados` e `bec-minio-dados` — sem ele o `docker compose config` falha na validação
- **`minio-init` espera o MinIO responder de fato.** Não confiar em healthcheck da imagem `minio/minio`: ela não traz `curl`, e um healthcheck que nunca fica verde trava o `depends_on` para sempre. Usar `depends_on: [minio]` + laço explícito `until mc alias set local ...; do sleep 1; done`
- **Bucket idempotente** — `mc mb --ignore-existing`. Sem isso o **segundo** `docker compose up` morre com *bucket already exists*
- **Imagens fixadas por tag específica**, nunca `latest`. Sem `version:` no topo (obsoleto no Compose v2)

Credenciais de desenvolvimento vêm de `.env` (já ignorado pelo git); um **`.env.exemplo`
versionado** documenta as chaves. Nenhuma chave RSA nem credencial real é commitada.

#### Etapa 8 — paridade da documentação

Regra nº 1 do projeto, e o workflow `docs-parity.yml` **falha o PR** se só um dos dois mudar.

Em `docs/arquitetura-sistema.json` (**alterar primeiro**):

- `project_metadata`: versão `2.0.0` → `2.1.0`, `updated_at` → `2026-08-27`
- `directory_structure.repository_root`: acrescentar `docker-compose.yml` e `.env.exemplo` — hoje o compose é citado em 6 documentos mas **não consta** da estrutura da raiz
- `backend.dependencies.core`: `spring-boot-starter-web` → `spring-boot-starter-webmvc`
- `backend.dependencies.security`: `spring-boot-starter-oauth2-resource-server` → `spring-boot-starter-security-oauth2-resource-server`
- `backend.dependencies.database_and_persistence`: `spring-boot-starter-flyway` + `flyway-database-postgresql`, versão **12.4.x herdada do BOM do Spring Boot 4.1.1**
- `backend.testing_modules`: starters de teste modulares do Boot 4.1 e os coordenados corretos `testcontainers`, `testcontainers-junit-jupiter`, `testcontainers-postgresql` em `2.0.x`
- `database`: acrescentar `name: bicoemcasa`, `user: bicoemcasa`, `port: 5432`, `extensions: "Nenhuma. gen_random_uuid() e nativo do PostgreSQL 13+"`
- **nova seção `local_infrastructure`**: serviços do compose, imagens fixadas, portas, volumes nomeados e os 3 buckets

Depois, espelhar **tudo** em `docs/design-sistema.md` — mesma seção, mesmos valores, **mesmo commit**.

E corrigir `CLAUDE.md`: a tabela de `lib/` passa a dizer `armazenamento/` e `email/`, removendo
`geocodificacao/` (não existe até o ADR-0009 decidir o fornecedor).

## Riscos e Mitigações

| Risco | Impacto | Mitigação |
|---|---|---|
| Usuário não está no grupo `docker` — etapas 9 e 10 não rodam | **Alto** | Pré-requisito explícito. Se não for resolvido, **reportar as etapas como não executadas, com o motivo** — nunca como "passou" |
| Flyway 12.4.0 recusar o PostgreSQL 18 (`Unsupported Database`) | Médio | É o único risco de versão que sobrou depois da opção pelo BOM. A etapa 10 é o que detecta. **Se acontecer: parar e perguntar ao usuário**, porque subir para 13.4.0 reverteria uma decisão dele |
| Os 2 agentes colidirem entre si ou com a sessão principal | Médio | Escopos disjuntos por pasta: agente A só `backend/`, agente B só `frontend/`, agente C só `docs/`. **Nenhum dos três roda comando git** — a sessão principal faz todos os commits em série |
| Initializr mudar o POM gerado desde a verificação | Médio | O POM foi inspecionado em 2026-08-27. **Reconferir o que veio** antes de aplicar os ajustes, em vez de aplicar um patch às cegas |
| Agentes criarem pastas vazias violando a regra de pasta vs arquivo | Médio | Está escrito explicitamente no prompt dos dois agentes. Conferir com `find backend frontend -type d -empty` antes de commitar |
| Frontend gerar `types/`, `services/` ou `utils/` por hábito de scaffold | Médio | Proibido por ADR-0005. Conferir com `ls frontend/src` — só `app`, `components`, `hooks`, `api` são permitidos |
| Agentes "melhorarem" coisas não pedidas | Médio | Pergunta 4 do checklist de revisão do `CLAUDE.md`. Revisar o diff de cada agente **linha a linha** antes do commit |

## Impacto na Arquitetura

Este plano altera `docs/arquitetura-sistema.json`?

- [x] **Sim** — exige atualização de `design-sistema.md` no mesmo commit
- [ ] Não

**Sobre ADR:** este plano **não** cria ADR novo. ADR-0006 já decidiu Postgres e MinIO em Docker
via compose; este trabalho é a implementação daquela decisão, e as convenções novas (nomes do
banco, portas, buckets, volumes) vão para a fonte da verdade, que é onde elas moram.

Se o usuário preferir formalizar a infraestrutura local em ADR, o número seria **0009** — e a
geocodificação, hoje reservada informalmente nesse número, escorreria para **0010**. Isso é
decisão dele, não do executor.

## Verificação

| # | Comando | Resultado esperado | Precisa de Docker? |
|---|---|---|---|
| 1 | `jq empty docs/arquitetura-sistema.json` | código 0 — é o que o CI roda | não |
| 2 | `cd backend && ./mvnw -q -B clean compile` | código 0 — toolchain, Java 21 e todos os coordenados resolvem | não |
| 3 | `cd backend && ./mvnw -B dependency:tree \| grep -E 'flyway\|testcontainers\|mapstruct\|bouncycastle\|awssdk'` | versões efetivas batem com o documentado (Flyway **12.4.0**) | não |
| 4 | `cd frontend && npx tsc --noEmit` | código 0 — strict mode limpo | não |
| 5 | `cd frontend && npm run build` | código 0 | não |
| 6 | `docker compose config` | código 0 — compose válido, volumes declarados | **não** |
| 7 | `docker compose up -d && docker compose ps` | `postgres` e `minio` de pé; `mc ls local` mostra os 3 buckets | sim |
| 8 | `cd backend && ./mvnw -B test` | passa — contexto Spring sobe contra Postgres 18 real | sim |
| 9 | `find backend frontend -type d -empty` | **vazio** — nenhuma pasta vazia violando a convenção | não |
| 10 | `ls frontend/src` | só `app`, `components`, `hooks`, `api` — nada de `types`/`services`/`utils` | não |
| 11 | `git status` | working tree limpo, sem `target/` nem `.next/` vazando | não |

**Sobre o teste de fumaça da etapa 10, com honestidade.** Ele sobe o contexto Spring contra um
`PostgreSQLContainer` (`postgres:18.6-alpine`) via `@ServiceConnection`, com zero migrations e
zero entidades. Ele **não** prova nada sobre schema, constraints ou mapeamento JPA — não há
schema ainda. O que ele prova, e não é pouco: o POM resolve, a autoconfiguração do Boot 4.1
sobe, o driver conecta, o Testcontainers 2.0 funciona nesta máquina, e **o Flyway 12.4.0 não
recusa o PostgreSQL 18**.

## Commits previstos

Conventional Commits em português, imperativo, sem ponto final, até 72 caracteres:

1. `docs(historias): registrar historias de usuario do administrador`
2. `chore(docs): remover export intermediario e renomear schema`
3. `docs(requisitos): remover mencao residual ao Supabase no RNF003`
4. `chore(git): ignorar artefatos de build de Java e Next.js`
5. `feat(backend): fundar projeto Spring Boot 4.1 com as dependencias do MVP`
6. `feat(frontend): fundar projeto Next.js 16 com a fronteira src/api`
7. `feat(infra): adicionar docker-compose com PostgreSQL 18 e MinIO`
8. `docs(arquitetura): registrar infraestrutura local e corrigir versoes` ← **JSON + MD juntos**
9. `docs(relatorios): registrar a sessao de fundacao das duas pontas`

## Pendências que este plano **não** fecha

Continuam abertas e devem ser repetidas no relatório da execução:

- [ ] **ADR-0009 (geocodificação)** — único requisito do MVP sem caminho técnico (RF013/RNF018). Bloqueia o épico de endereço
- [ ] **Migrations Flyway `V1`–`V6`** — as 18 tabelas. É a próxima task depois desta
- [ ] **PR #2 aberto sem review** desde 23/08 — `main` ainda não tem requisitos v1.1.0, modelo v4 nem ADR-0006/0007/0008
- [ ] **`LICENSE` vazio** (0 bytes) — arrastado por 3 relatórios
- [ ] **ADR do `utils/` no frontend** — `cn()` (clsx + tailwind-merge) não tem casa definida. Vai bater na primeira composição de classe do Tailwind
- [ ] **Node 24 (documentado) vs Node 26.5.1 (máquina)** — não bloqueia (`next@16.3.3` exige `>=20.9.0`), mas a doc está desatualizada. Decidir se atualiza a doc ou fixa a versão
- [ ] **`@tanstack/react-query`**: doc diz 5.101.x, publicado está 5.102.7 — deriva menor a alinhar
- [ ] **Remote `origin` em HTTPS** (era SSH) — mudança de contorno nunca revisada
- [ ] **Trello com 0 cards** para um backlog de 404h
- [ ] **Três fontes de documentação** (Drive, Notion, repositório) sem dono definido
- [ ] **Upload do mapa de contexto ao Drive** não confirmado

---

## Prompt para a próxima sessão

Copie o bloco abaixo inteiro como primeira mensagem da nova sessão.

````markdown
/role-dev

Execute o plano em `docs/planos/2026-08-27-fundacao-backend-frontend-e-infra.md`. Leia o plano
inteiro antes de tocar em qualquer arquivo — ele tem as armadilhas já mapeadas e versões já
verificadas contra o Maven Central e o npm. Leia também `docs/design-sistema.md`, os ADRs
0003 a 0008 e o `CLAUDE.md` do projeto.

## Antes de começar

1. Confirme o pré-requisito bloqueante: rode `docker ps`. Se der *permission denied*, **pare e
   me avise** — eu preciso rodar `sudo usermod -aG docker $USER` em terminal próprio e relogar.
   Você pode seguir com tudo que não precisa de Docker, mas me diga isso na hora, não no fim.
2. Crie a branch `feat/fundacao-backend-e-infra` a partir de
   `docs/requisitos-e-matriz-rastreabilidade`. Não mexa no PR #2.

## Como dividir o trabalho

Dispare **três agentes Sonnet com escopos de pasta disjuntos**, para não colidirem:

- **Agente A — backend** (`backend/` apenas): etapa 4 do plano. Gera o projeto pelo
  `start.spring.io` (não há `mvn` instalado — o Maven Wrapper vem do zip), aplica os 6 ajustes
  do `pom.xml` descritos no plano, escreve `application.yml` / `-dev` / `-prod`, o teste de
  fumaça com Testcontainers e o `backend/README.md`. **Não cria pastas vazias** de `config/`,
  `lib/`, `comum/` nem dos módulos — a convenção do projeto proíbe.
- **Agente B — frontend** (`frontend/` apenas): etapa 5 do plano. Fundação técnica do Next.js
  16.3 + TypeScript 7 strict + Tailwind 4.3, o `src/api/` como fronteira única com `client.ts`,
  `erros.ts` e os contratos Zod, o layout com o provider do TanStack Query, o `vitest.config.ts`
  e o `frontend/README.md`. **Proibido**: `fetch` fora de `src/api/`, `any`, tipo escrito à mão
  em vez de `z.infer`, e as pastas `types/`, `services/`, `utils/`. Sem design de produto —
  a landing é um placeholder textual.
- **Agente C — limpeza da documentação** (`docs/` apenas): etapa 2 do plano.

**Nenhum dos três agentes roda comando git.** Eles editam, criam e apagam arquivos; você faz
todo `git add`, `git mv` e `git commit` em série, depois que eles terminarem. Dois processos
commitando ao mesmo tempo disputam o `.git/index.lock`.

Você mesmo faz, na sessão principal: o `.gitignore` (etapa 3), o `docker-compose.yml` e o
`.env.exemplo` (etapa 6), a paridade da documentação (etapa 8), o `/revisar-matriz` depois do
agente C, e todos os commits.

## Regras do projeto que valem aqui

- **Paridade da documentação**: `docs/arquitetura-sistema.json` é a fonte da verdade.
  Alterar o JSON **primeiro**, espelhar em `docs/design-sistema.md` **no mesmo commit**. O
  workflow `docs-parity.yml` falha o PR se só um dos dois mudar.
- **Quem decide sou eu, não a LLM.** Se aparecer mais de uma solução defensável e a diferença
  for de preferência e não de correção, **pergunte antes de escrever**. Se a proposta contrariar
  um ADR, pergunte. Erro objetivo (coordenada que não resolve, YAML inválido) você corrige sozinho.
- **Pasta só existe quando há mais de um arquivo daquele tipo.** Nada de estrutura vazia.
- Tudo em português: código, comentários, Javadoc, TSDoc, documentação e commits.
- Commits em Conventional Commits, imperativo, sem ponto final, até 72 caracteres. Os 9 commits
  previstos estão listados no plano — use aqueles.

## Como reportar o resultado

Rode de fato a seção **Verificação** do plano (11 comandos) e me mostre a saída real. As
verificações 7 e 8 dependem do grupo `docker`: se não rodarem, reporte **⏭️ não executado com o
motivo**, nunca ✅. Teste que falhou se reporta como falhou, com a saída.

Antes de fechar, revise o diff de cada agente linha a linha e responda às 5 perguntas do
checklist do `CLAUDE.md` — em especial a nº 4: *houve nuance gerada pela LLM?* Qualquer coisa
que os agentes adicionaram ou "melhoraram" sem ter sido pedida ou vira decisão explícita, ou
volta atrás.

Se o Flyway 12.4.0 recusar o PostgreSQL 18 na verificação 8, **pare e me pergunte** — subir para
13.4.0 reverteria uma decisão que eu tomei.

## Ao final

Rode `/relatorio-sessao`. O relatório precisa registrar honestamente o que foi verificado de
fato, o que ficou bloqueado e por quê, as 4 imprecisões de documentação corrigidas, e repetir a
lista de pendências que este plano **não** fecha (está no final do plano).

**Não faça `git push` nem abra PR** — eu decido isso depois de revisar.
````
