# Design do Sistema — Bico em Casa

> [!IMPORTANT]
> **Este documento é o espelho legível de [`arquitetura-sistema.json`](./arquitetura-sistema.json).**
> O JSON é a **única fonte da verdade**. É proibido editar este markdown sem antes editar o JSON,
> e as duas alterações vão **no mesmo commit**. Divergência entre os dois é considerada bug.

| | |
|---|---|
| **Projeto** | `bico-em-casa` |
| **Versão do documento** | 2.1.0 |
| **Última atualização** | 2026-08-27 |
| **Fonte da verdade** | `docs/arquitetura-sistema.json` |
| **Padrão arquitetural** | Arquitetura Modular por Domínio (Modular Monolith) com Ports & Adapters na fronteira |

---

## 1. Visão Geral

O **Bico em Casa** é uma plataforma de contratação de profissionais autônomos para serviços
rápidos. O cliente encontra o profissional, consulta o portfólio (opcional) e contrata sem
burocracia.

A arquitetura é um **monolito modular**: uma única aplicação Spring Boot dividida em módulos de
domínio autocontidos, com adapters isolando os serviços externos na borda. Essa escolha entrega
a simplicidade operacional de um monolito com fronteiras internas rígidas o bastante para
extrair um módulo em serviço próprio depois, se a necessidade aparecer.

```
┌─────────────────┐        HTTPS/JSON        ┌──────────────────────┐
│   Next.js 16    │ ───────────────────────► │   Spring Boot 4.1    │
│   (App Router)  │    Bearer JWT próprio    │   API REST           │
│                 │ ◄─────────────────────── │   + emissão de token │
└────────┬────────┘      RFC 9457 errors     └──────────┬───────────┘
         │                                              │
         │ upload por URL pré-assinada                  │ JDBC          │ S3 API / SMTP
         │                                              ▼               ▼
         │                                   ┌──────────────────┐  ┌──────────┐
         └──────────────────────────────────►│  PostgreSQL 18   │  │  MinIO   │
                                             │  (auto-hospedado)│  │  + SMTP  │
                                             └──────────────────┘  └──────────┘
```

**A identidade é do próprio backend.** O módulo `autenticacao` guarda a credencial, emite o
access token e o refresh token, e o mesmo backend os valida. Não há provedor externo de
identidade — ver [ADR-0006](./adr/0006-remover-supabase-infraestrutura-propria.md).

---

## 2. Governança da Documentação

### Ordem obrigatória de alteração

1. Alterar `docs/arquitetura-sistema.json`
2. Refletir a **mesma** alteração em `docs/design-sistema.md` **no mesmo commit**
3. Registrar o *porquê* em um ADR sob `docs/adr/` quando a mudança for estrutural

### Proibido

- Editar `design-sistema.md` sem antes editar `arquitetura-sistema.json`
- Commitar alteração no JSON sem atualizar o markdown
- Introduzir pasta ou camada não descrita aqui sem ADR de aprovação

### ADRs

| Item | Valor |
|---|---|
| Local | `docs/adr/` |
| Template | `docs/adr/TEMPLATE.md` |
| Numeração | `NNNN-titulo-em-kebab-case.md`, sequencial a partir de `0001` |
| Status possíveis | Proposto · Aceito · Rejeitado · Depreciado · Substituído por ADR-NNNN |

---

## 3. Backend

| Item | Valor |
|---|---|
| Runtime | Java 21 (LTS) |
| Framework | Spring Boot 4.1.x |
| Spring Framework | 7.0.x |
| Spring Security | 7.1.x |
| Build tool | Maven |
| Pacote base | `br.com.bicoemcasa.api` |

**Por que Spring Boot 4.1:** a linha 3.5.x encerrou o suporte OSS em 30/06/2026. A 4.1.x é a
linha GA suportada e mantém Java 21 como runtime válido (o baseline mínimo do Boot 4 é o Java 17).

**Por que Maven:** padrão dominante no ecossistema Spring, integração direta com o Spring
Initializr e menor curva de aprendizado para o time.

### 3.1 Dependências

| Grupo | Artefatos |
|---|---|
| **Core** | `spring-boot-starter-webmvc`, `spring-boot-starter-validation`, `spring-boot-starter-actuator` |
| **Persistência** | `spring-boot-starter-data-jpa`, `spring-boot-starter-flyway`, `org.postgresql:postgresql`, `flyway-database-postgresql` |
| **Segurança** | `spring-boot-starter-security`, `spring-boot-starter-security-oauth2-resource-server`, `org.bouncycastle:bcprov-jdk18on:1.85.2` (requerido pelo `Argon2PasswordEncoder`; versão explícita, não está no BOM) |
| **Produtividade** | `lombok`, `mapstruct:1.6.3`, `mapstruct-processor:1.6.3` (em `annotationProcessorPaths`, não como dependência solta) |
| **Documentação** | `springdoc-openapi-starter-webmvc-ui:3.1.x` |
| **Integrações** | `software.amazon.awssdk:s3` (versão vinda do `awssdk:bom:2.54.5` importado em `dependencyManagement`), `spring-boot-starter-mail` |

> [!IMPORTANT]
> **Os nomes mudaram no Boot 4.1, e a documentação anterior estava desatualizada.**
> `spring-boot-starter-web` foi depreciado em favor de `spring-boot-starter-webmvc`, e
> `spring-boot-starter-oauth2-resource-server` em favor de
> `spring-boot-starter-security-oauth2-resource-server`. Os antigos ainda resolvem, mas o
> Initializr emite os novos e é o que o projeto usa.

**Política de versão:** toda biblioteca gerenciada pelo BOM do Spring Boot 4.1.1 **herda a versão
do BOM** — nenhum override é declarado no `pom.xml`. Versão explícita só onde o BOM não gerencia
(MapStruct, BouncyCastle, springdoc-openapi) ou onde outro BOM é importado (AWS SDK). É por isso
que o Flyway é **12.4.0** e não a faixa 13.3.x que o [ADR-0003](./adr/0003-spring-boot-4-java-21-maven.md)
previa — ver a nota de revisão naquele ADR.

#### Removido

| Artefato | Motivo |
|---|---|
| `io.jsonwebtoken:jjwt-*` | Desnecessária. O Spring Security já traz Nimbus JOSE via `spring-security-oauth2-jose`, que **emite** (`NimbusJwtEncoder`) e **valida** (`NimbusJwtDecoder`) o token. Adicionar `jjwt` seria uma segunda biblioteca de JWT no mesmo classpath. |
| Supabase (todo o BaaS) | Removido pelo [ADR-0006](./adr/0006-remover-supabase-infraestrutura-propria.md). Identidade, banco e armazenamento passam a ser auto-hospedados. |

### 3.2 Segurança

| Item | Definição |
|---|---|
| Provedor de identidade | **Próprio** — módulo `autenticacao` |
| Estratégia | Stateless. O backend **emite e valida** JWT próprio, assinado com par de chaves RSA |
| Emissão do token | `NimbusJwtEncoder` do Spring Security, já disponível via `spring-security-oauth2-jose`. **Não requer biblioteca de JWT adicional** |
| Validação do token | `NimbusJwtDecoder` com a chave pública RSA local. O backend segue sendo **OAuth2 Resource Server**, agora contra emissor próprio |
| Access token | 15 minutos |
| Refresh token | 30 dias. Persistido em `refresh_token`, **rotacionado a cada uso** e revogável individualmente ou por usuário |
| Correlação de identidade | O claim `sub` é o `id` (`BIGINT`, ver [ADR-0007](./adr/0007-chave-primaria-mista.md)) de `usuario`. **Nenhum papel viaja dentro do token** |
| Autorização | Por papel (`CLIENTE`, `PROFISSIONAL`, `ADMIN`), resolvido no backend a partir de `perfil` — **nunca** confiando em claim editável pelo cliente |
| CORS | Restrito por ambiente via `CorsConfigurationSource`; origens definidas por profile |
| CSRF | Desabilitado (API stateless, sem cookie de sessão) |
| Encoder de senha | **Argon2id** via `Argon2PasswordEncoder`. O hash vive em `usuario.hash_senha`; a senha em claro **nunca** é persistida, logada nem devolvida |
| Recuperação de senha | Token opaco de uso único, expiração curta, **armazenado com hash** em `token_recuperacao` e invalidado no primeiro uso |
| Segredos | Nenhuma chave em código. Chave privada RSA, credenciais do MinIO e do SMTP vivem em variável de ambiente, **somente no backend** |

> [!IMPORTANT]
> **Por que o refresh token é rotacionado.** Sem rotação, um refresh token vazado vale 30 dias
> para o atacante e a vítima não percebe nada. Com rotação, o token usado é invalidado e um novo
> é emitido — se o atacante usar o antigo, o backend detecta o reuso e revoga a família inteira,
> derrubando as duas sessões. A vítima é forçada a logar de novo, que é o sinal de que algo houve.

> [!WARNING]
> **O que assumimos ao sair do Supabase Auth.** Hash de senha, emissão e rotação de token,
> expiração de sessão, recuperação de senha e envio de e-mail passam a ser código nosso — e
> código nosso tem bug nosso. Autenticação é a superfície onde bug custa mais caro. A decisão foi
> tomada com esse custo em vista e está registrada no
> [ADR-0006](./adr/0006-remover-supabase-infraestrutura-propria.md).

### 3.3 Testes

| Grupo | Artefatos |
|---|---|
| Fatias do Boot 4.1 | `spring-boot-starter-webmvc-test`, `-data-jpa-test`, `-security-test`, `-security-oauth2-resource-server-test`, `-validation-test`, `-actuator-test`, `-flyway-test`, `-mail-test` |
| Containers | `spring-boot-testcontainers`, `org.testcontainers:testcontainers-junit-jupiter:2.0.x`, `org.testcontainers:testcontainers-postgresql:2.0.x` |

**Por que a lista mudou.** O Boot 4.1 **modularizou** o antigo `spring-boot-starter-test` por
fatia. O monolítico ainda resolve, mas o Initializr emite os modulares e é o que o projeto usa;
`spring-boot-starter-security-test` substitui o antigo
`org.springframework.security:spring-security-test`.

E o Testcontainers 2.0 **renomeou os módulos**: os coordenados corretos são
`testcontainers-junit-jupiter` e `testcontainers-postgresql` — `org.testcontainers:junit-jupiter`
e `org.testcontainers:postgresql`, como a documentação anterior dizia, **não existem** nessa
linha. A versão (2.0.5 no Boot 4.1.1) é herdada do BOM.

**Política:** teste de integração sobe um PostgreSQL 18 real via Testcontainers. É **proibido**
validar regra de negócio contra banco em memória (H2) — o comportamento diverge do de produção
justamente nos pontos que importam.

---

## 4. Frontend

| Item | Valor |
|---|---|
| Framework | Next.js 16.3.x (App Router) |
| Runtime | Node.js >=20.9.0 (validado em v26.5.1) |
| Linguagem | TypeScript 7.0.x (strict mode) |

**Por que TypeScript 7:** compilador nativo em Go, GA desde 08/07/2026. O typecheck e o feedback
no editor ficam ordens de grandeza mais rápidos, mantendo a mesma semântica de tipos da linha 5.x.

### 4.1 Dependências

| Grupo | Pacotes |
|---|---|
| **Core** | `next@16.3.x`, `react@19.2.x`, `react-dom@19.2.x`, `typescript@7.0.x` |
| **Estado e dados** | `@tanstack/react-query@5.102.x`, `zustand@5.0.x` |
| **Estilo e UI** | `tailwindcss@4.3.x`, `lucide-react`, `clsx`, `tailwind-merge`, `radix-ui` |
| **Formulários** | `react-hook-form`, `zod@4.4.x`, `@hookform/resolvers` |
| **Integração** | Nenhuma. O acesso ao backend usa exclusivamente o `fetch` nativo encapsulado em `src/api/client.ts` |

#### Removido

| Pacote | Motivo |
|---|---|
| `axios` | O `fetch` nativo do Next 16 participa do cache e da revalidação do App Router; o axios contorna esse mecanismo. O cliente HTTP próprio vive em `src/api/client.ts`. |
| `@supabase/supabase-js`, `@supabase/ssr` | Consequência do [ADR-0006](./adr/0006-remover-supabase-infraestrutura-propria.md). A sessão passa a ser gerida pelo backend próprio: o access token fica em memória e o refresh token em cookie `httpOnly` emitido pela API. |

### 4.2 Política de Fronteira

> **O frontend não conhece nem confia na implementação do backend.**
> Todo contato atravessa `src/api/`, que é a única fronteira.

1. Somente `src/api/` conhece URLs, headers e formato de payload do backend
2. Toda resposta do backend é validada por schema Zod em `src/api/contratos/` **antes** de entrar na aplicação
3. Os tipos da aplicação são **inferidos** dos schemas Zod (`z.infer`), nunca escritos à mão em paralelo
4. Componentes e hooks **jamais** chamam `fetch` diretamente — consomem os hooks de `src/api/`
5. Erro de rede ou payload inválido é normalizado em `src/api/` para um tipo de erro único da aplicação

A regra 2 é o que dá sentido à palavra *fronteira*: se o backend mudar um campo sem avisar, o
erro estoura num único lugar previsível, com mensagem de schema, em vez de vazar como `undefined`
no meio de um componente.

### 4.3 Testes

| Grupo | Pacotes |
|---|---|
| Unitário e componente | `vitest@4.1.x`, `@testing-library/react`, `@testing-library/jest-dom`, `@testing-library/user-event`, `jsdom` |
| E2E | `@playwright/test@1.62.x` |

---

## 5. Banco de Dados

| Item | Valor |
|---|---|
| Engine | PostgreSQL 18 |
| Nome do banco | `bicoemcasa` |
| Usuário | `bicoemcasa` |
| Porta | `5432` |
| Extensões | **Nenhuma.** `gen_random_uuid()` é nativo do PostgreSQL 13+ |
| Hospedagem | **Auto-hospedada** — container Docker em desenvolvimento, instância dedicada em produção |
| Ferramenta de migration | **Flyway 12.4.x**, herdada do BOM do Spring Boot 4.1.1 |
| Caminho das migrations | `backend/src/main/resources/db/migration` |

**Sobre a versão do Flyway.** O [ADR-0003](./adr/0003-spring-boot-4-java-21-maven.md) previa a
faixa 13.3.x. Em 2026-08-27 a decisão foi **herdar a versão do BOM** em vez de fixar override no
`pom.xml`, e o BOM do Boot 4.1.1 gerencia o Flyway **12.4.0**. A nota de revisão está no próprio
ADR-0003.

**Política de migration:** toda alteração de schema nasce como migration versionada do Flyway.
É **proibido** alterar schema manualmente em qualquer ambiente — a alteração some do histórico e o
próximo `flyway migrate` diverge.

### 5.1 Schemas

| Schema | Papel |
|---|---|
| `public` | Tabelas de domínio da aplicação, **integralmente** gerenciadas pelo Flyway |

Com a saída do Supabase, o schema `auth` deixou de existir. **Toda** tabela do banco, credencial
inclusive, é versionada pelo Flyway e pertence à aplicação.

### 5.2 Convenções de Nomenclatura

| Elemento | Convenção | Exemplo |
|---|---|---|
| Tabelas | `snake_case` **singular**, sem prefixo ([ADR-0008](./adr/0008-nomenclatura-de-tabelas.md)) | `usuario`, `contratacao`, `servico_tag` |
| Colunas | `snake_case` **em português** | `criado_em`, `usuario_id` |
| Chave primária — cadastro | `id BIGINT GENERATED ALWAYS AS IDENTITY` | `usuario`, `perfil`, `endereco`, `portfolio` |
| Chave primária — transacional | `id UUID DEFAULT gen_random_uuid()` | as demais 14 tabelas |
| Referência polimórfica | `varchar(64)` **sem** FK — o alvo pode ser `bigint` ou `uuid` | `log_acao.alvo_id`, `notificacao.alvo_id`, `denuncia.alvo_id` |
| Chave estrangeira | `fk_{tabela_origem}_{tabela_destino}` | `fk_contratacao_profissionais` |
| Índice | `idx_{tabela}_{coluna}` | `idx_profissionais_cidade` |
| Constraint única | `uq_{tabela}_{coluna}` | `uq_usuario_email` |
| Constraint de check | `ck_{tabela}_{regra}` | `ck_avaliacao_nota_valida` |

---

## 6. Serviços Externos

O [ADR-0006](./adr/0006-remover-supabase-infraestrutura-propria.md) removeu o Supabase. Sobraram
dois serviços externos, ambos auto-hospedáveis e ambos isolados atrás de adapter em `lib/`.

### 6.1 MinIO — armazenamento de objetos

| Item | Definição |
|---|---|
| Papel | Armazenamento de objetos compatível com a **API S3**, auto-hospedado |
| Por quê | Sobe no `docker-compose` junto do PostgreSQL, tem custo zero, e fala a API S3 — que é o padrão de mercado. Trocar por S3 real depois muda **uma pasta** |
| Biblioteca | `software.amazon.awssdk:s3` |
| Adapter | `br.com.bicoemcasa.api.lib.armazenamento` |

| Bucket | Conteúdo |
|---|---|
| `portfolios` | Imagens de portfólio dos profissionais (`RF003`) |
| `anexos` | Fotos anexadas às solicitações de orçamento (`RF015`) |
| `avatares` | Fotos de perfil |

**Padrão de acesso:** o backend gera **URL pré-assinada** com expiração curta. O navegador faz
upload e download direto no MinIO, sem que os bytes trafeguem pela API. Isso mantém a aplicação
fora do caminho de arquivos grandes — que é o que derruba uma API primeiro sob carga.

### 6.2 SMTP — e-mail transacional

| Item | Definição |
|---|---|
| Papel | Envio de e-mail transacional |
| Por quê | A recuperação de senha (`RF001`) deixou de ser responsabilidade do Supabase Auth |
| Escopo | **Somente** transacional: recuperação de senha e confirmação de cadastro. Não é canal de marketing |
| Adapter | `br.com.bicoemcasa.api.lib.email` |

### 6.3 Pendente de decisão — geocodificação

`RF013` exige distância em quilômetros, o que depende de geocodificar endereço em latitude e
longitude. **O fornecedor ainda não foi escolhido** e precisa de ADR próprio antes de qualquer
implementação. Enquanto isso, `endereco` já nasce com as colunas de coordenada, para que a
migration não precise ser refeita.

---

## 7. Infraestrutura Local

O [ADR-0006](./adr/0006-remover-supabase-infraestrutura-propria.md) decidiu PostgreSQL e MinIO
auto-hospedados. Esta seção é **a implementação daquela decisão**: o `docker-compose.yml` na raiz
do repositório, que é o que faz `git clone && docker compose up -d` funcionar sem mais nada.

| Arquivo | Papel |
|---|---|
| `docker-compose.yml` | Definição dos serviços. **Sem chave `version:`** no topo — obsoleta no Compose v2 |
| `.env.exemplo` | Documenta as chaves de ambiente esperadas. O `.env` real é ignorado pelo git; **nenhum segredo é versionado** (`RNF004`) |

**Política de imagem:** toda imagem é fixada por **tag específica**, nunca `latest`. Build que
muda sozinho não é reproduzível, e a hora de descobrir isso nunca é boa.

### 7.1 Serviços

| Serviço | Imagem | Portas | Papel |
|---|---|---|---|
| `postgres` | `postgres:18.6-alpine` | 5432 | Banco `bicoemcasa`, usuário `bicoemcasa`, volume `bec-postgres-dados`, healthcheck `pg_isready -U bicoemcasa -d bicoemcasa` |
| `minio` | `minio/minio:RELEASE.2025-09-07T16-13-09Z` | 9000 (API S3), 9001 (console) | Armazenamento S3, volume `bec-minio-dados` em `/data` |
| `minio-init` | `minio/mc:RELEASE.2025-08-13T08-35-41Z` | — | Roda **uma vez** e sai. Cria `portfolios`, `anexos` e `avatares` |

**Volumes nomeados:** `bec-postgres-dados` e `bec-minio-dados`, declarados no bloco `volumes:` do
topo. Esse bloco não é decorativo — **sem ele o `docker compose config` falha na validação**.

### 7.2 Três armadilhas que estão no compose de propósito

**O volume do Postgres não monta em `/var/lib/postgresql/data`.** A imagem do PostgreSQL 18
mudou o `PGDATA` para `/var/lib/postgresql/18/docker` e passou a declarar o `VOLUME` em
`/var/lib/postgresql`. Montar no caminho de sempre — o que valeu até o PostgreSQL 17 — cria um
diretório que ninguém usa, e os dados **não sobrevivem a um `docker compose down`**. O erro é
silencioso: tudo funciona até a primeira vez que alguém derruba os containers.

**O `minio-init` espera com um laço, não com healthcheck.** A imagem `minio/minio` não traz
`curl`. Um healthcheck baseado nele nunca fica verde, e um `depends_on: service_healthy`
apontando para um healthcheck que nunca fica verde **trava para sempre**. Por isso o init usa
`depends_on: [minio]` e um `until mc alias set local ...; do sleep 1; done` explícito.

**Os buckets são criados com `mc mb --ignore-existing`.** Sem isso o **segundo**
`docker compose up` morre com *bucket already exists* — o primeiro funciona e dá a falsa
impressão de que está certo.

---

## 8. Git e Versionamento

### 8.1 Estratégia de Branches

| Branch | Papel |
|---|---|
| `main` | Produção. Deploy versionado por tags `vX.Y.Z`. **Commits diretos proibidos.** |
| `develop` | Staging/homologação. Branch de integração das features concluídas. |
| `feat/<ticket-id>-<descricao-curta>` | Feature, originada de `develop` e mesclada via Pull Request |
| `fix/<ticket-id>-<descricao-curta>` | Correção, originada de `develop` |
| `hotfix/<ticket-id>-<descricao-curta>` | Correção urgente, originada de `main` e retroportada para `develop` |
| `docs/<descricao-curta>` | Alterações exclusivas de documentação |

### 8.2 Conventional Commits

**Formato:** `<type>(<scope>): <descrição curta>`

| Tipo | Uso |
|---|---|
| `feat` | Nova funcionalidade para o usuário |
| `fix` | Correção de bug |
| `docs` | Alterações apenas em documentação |
| `style` | Formatação sem alteração lógica |
| `refactor` | Refatoração sem alterar comportamento |
| `perf` | Melhoria de performance |
| `test` | Adição ou correção de testes |
| `chore` | Build, dependências ou ferramentas de CI |

**Escopos:** `backend`, `frontend`, `db`, `docs`, `ci` ou o nome do módulo.

**Regras:**

- Descrição em português, modo imperativo
- Sem ponto final no título
- Limite de 72 caracteres no cabeçalho
- Alterações em `arquitetura-sistema.json` e `design-sistema.md` vão **no mesmo commit**

---

## 9. CI/CD — GitHub Actions

> **Só `docs-parity.yml` existe hoje.** `backend-ci.yml` e `frontend-ci.yml` estão descritos
> abaixo como especificação a implementar — nenhum dos dois está em `.github/workflows/`.
> Nenhuma etapa de lint, teste ou build roda automaticamente em PR neste momento.

### `backend-ci.yml` — PLANEJADO

**Gatilhos:** `pull_request → develop`, `pull_request → main`

1. Checkout do repositório
2. Setup Java 21 (Temurin) com cache Maven
3. Verificar formatação com Spotless (plugin ainda não está no `backend/pom.xml`)
4. Executar testes unitários e de integração com Testcontainers
5. Build do artefato JAR (`mvn -B package`)
6. Upload de relatório de cobertura (JaCoCo — plugin ainda não está no `backend/pom.xml`)

### `frontend-ci.yml` — PLANEJADO

**Gatilhos:** `pull_request → develop`, `pull_request → main`

1. Checkout do repositório
2. Setup Node.js na versão de §4 (Runtime), com cache de dependências
3. Executar ESLint e Prettier em modo check — a etapa de ESLint precisa nascer desligada ou
   com `continue-on-error`, pelo limite conhecido do `typescript-eslint` com TypeScript 7
4. Executar typecheck (`tsc --noEmit`)
5. Executar testes unitários com Vitest
6. Build do Next.js (`next build`)

### `docs-parity.yml` — IMPLEMENTADO

**Gatilhos:** `pull_request → develop`, `pull_request → main`

1. Falhar o build se `docs/arquitetura-sistema.json` foi alterado sem alteração em `docs/design-sistema.md` no mesmo PR
2. Validar que o JSON é sintaticamente válido

---

## 10. Padrões de Código

### 10.1 Backend

| Item | Ferramenta |
|---|---|
| Formatador | Spotless com Google Java Style — **planejado**, o plugin ainda não está no `pom.xml` |
| Linter | SonarLint / Checkstyle — **planejado**, nenhum dos dois está configurado |

**Regras:**

- Princípios SOLID e Clean Code rigorosos
- Tratamento global de exceções via `@RestControllerAdvice` com **RFC 9457** (`ProblemDetail`)
- Imutabilidade prioritária: `record` para DTOs, coleções não modificáveis
- Injeção de dependência estrita por construtor (campos `final`)
- Todo service público tem **interface em `contrato/`** e **implementação em `service/`** terminada em `Impl`
- Entidade JPA **nunca** cruza a fronteira do controller: sempre DTO
- Um módulo só acessa outro pela interface exposta em `contrato/`, nunca pelo repository alheio

### 10.2 Frontend

| Item | Ferramenta |
|---|---|
| Linter | ESLint com `eslint-config-next` e `@typescript-eslint/recommended` |
| Formatador | Prettier com `prettier-plugin-tailwindcss` |

> [!WARNING]
> **Limitação conhecida (2026-08-27): `npm run lint` não roda.** O `typescript-eslint`, do qual
> `eslint-config-next` depende, aborta com `typescript-eslint does not support TS 7.0` e o lint
> sai com código 2. `typecheck`, `build` e teste não são afetados.
> [typescript-eslint#10940](https://github.com/typescript-eslint/typescript-eslint/issues/10940).
>
> **Decisão:** manter o TypeScript 7 e conviver com o lint quebrado até o upstream alcançar, em
> vez de descer de versão ou manter dois TypeScript no projeto. **Consequência:** a etapa de
> ESLint do `frontend-ci.yml` precisa nascer desligada ou com `continue-on-error`, senão reprova
> todo PR.

**Regras:**

- Server Components por padrão; `'use client'` apenas para interatividade e hooks
- Componentes funcionais com tipagem explícita de props
- Validação de entrada e saída com schemas Zod na fronteira `src/api/`
- Proibido `any` — use `unknown` com narrowing por schema
- Nenhum `fetch` fora de `src/api/`

### 10.3 Comentários e Documentação

**Política:** código autoexplicativo substitui comentário redundante.
**Idioma:** comentários, Javadoc e TSDoc em português.

| Permitido | Proibido |
|---|---|
| Javadoc/TSDoc em interfaces públicas de `contrato/` e utilitários complexos | Código comentado (*dead code*) |
| Justificativa de decisão de negócio não trivial ou de complexidade algorítmica | Comentário narrando a sintaxe óbvia do código |
| `TODO`/`FIXME` vinculados a um issue explícito — ex.: `// TODO(BEC-123): revisar após migração V2` | |

---

## 11. Estrutura de Diretórios

### 11.1 Raiz do Repositório

```
bico-em-casa/
├── backend/              # Aplicação Spring Boot (Maven)
├── frontend/             # Aplicação Next.js
├── docs/                 # Documentação viva (design, ADRs, planos, relatórios)
├── docker-compose.yml    # Infraestrutura local: PostgreSQL 18 e MinIO (§7)
├── .env.exemplo          # Chaves de ambiente do compose; o .env real é ignorado
├── .claude/              # Configuração do Claude Code
└── .github/workflows/    # Pipelines de CI
```

### 11.2 Backend

```
backend/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/br/com/bicoemcasa/api/
    │   │   ├── BicoEmCasaApplication.java
    │   │   ├── config/          # SecurityFilterChain, CORS, OpenAPI, Jackson,
    │   │   │                    # beans globais e @ConfigurationProperties
    │   │   ├── lib/             # adapters de serviços EXTERNOS
    │   │   │   ├── armazenamento/  # ClienteArmazenamentoS3 (upload e URL pré-assinada)
    │   │   │   └── email/          # EnviadorEmail (e-mail transacional)
    │   │   ├── core/            # núcleo compartilhado entre módulos
    │   │   │   ├── excecao/     # exceções de domínio + @RestControllerAdvice (RFC 9457)
    │   │   │   ├── paginacao/   # tipos de paginação e ordenação da API
    │   │   │   └── auditoria/   # @MappedSuperclass com created_at / updated_at
    │   │   └── modulos/         # um pacote por domínio de negócio
    │   │       ├── autenticacao/
    │   │       ├── usuarios/
    │   │       ├── profissionais/
    │   │       ├── servicos/
    │   │       ├── contratacoes/
    │   │       └── avaliacoes/
    │   └── resources/
    │       ├── db/migration/         # V1__criar_usuarios.sql, V2__criar_profissionais.sql
    │       ├── application.yml
    │       ├── application-dev.yml
    │       └── application-prod.yml
    └── test/java/                    # estrutura espelhada, incluindo modulos/
```

#### `config/` vs `lib/` vs `core/`

Três pastas transversais com responsabilidades que não se sobrepõem:

| Pasta | Responsabilidade | Regra |
|---|---|---|
| `config/` | Configuração **da nossa aplicação**: security, CORS, OpenAPI, beans | Só configuração, sem lógica |
| `lib/` | Adapters de serviços **externos** (armazenamento, e-mail, mapas) | **Nada de regra de negócio.** Só tradução entre o mundo externo e tipos internos |
| `core/` | Núcleo compartilhado entre módulos | Não depende de nenhum módulo; é dependido por todos |

O valor de `lib/` é o isolamento do fornecedor: se o MinIO virar S3 da AWS amanhã, **só essa
pasta muda**. A saída do Supabase foi o primeiro teste dessa regra — e ela se pagou.

#### Estrutura interna de um módulo

| Pasta | Conteúdo |
|---|---|
| `controller/` | Endpoints REST (`@RestController`) |
| `service/` | Implementações das regras de negócio, sufixo `Impl` |
| `repository/` | Interfaces Spring Data JPA |
| `models/` | Entidades JPA e enums do domínio |
| `dto/` | `record` de entrada (request, com Jakarta Validation) e de saída (response) |
| `contrato/` | Interfaces que definem o que uma classe do módulo deve cumprir |
| `mapper/` | **Opcional.** Mapeadores MapStruct entre entidade e DTO, quando o mapeamento deixa de ser trivial |

**Convenção de nomes em `contrato/`:** a interface é `ProfissionalService` (em `contrato/`) e a
implementação é `ProfissionalServiceImpl` (em `service/`). Sem prefixo `I` — não é idiomático
em Java, e o nome limpo pertence ao contrato, não à implementação.

#### Regra: pasta só quando há mais de um arquivo

> **A pasta só existe quando há mais de um arquivo daquele tipo.**
> Com um único arquivo, ele fica na raiz do módulo.

**Módulo simples** — um arquivo de cada tipo, tudo na raiz:

```
modulos/avaliacoes/
├── AvaliacaoController.java
├── AvaliacaoService.java          # interface (contrato)
├── AvaliacaoServiceImpl.java      # implementação
├── AvaliacaoRepository.java
├── Avaliacao.java                 # entidade
└── dto/                           # request + response = 2 arquivos, vira pasta
    ├── AvaliacaoRequest.java
    └── AvaliacaoResponse.java
```

**Módulo composto** — mais de um arquivo por tipo, todas as pastas materializadas:

```
modulos/contratacoes/
├── controller/
│   ├── ContratacaoController.java
│   └── ContratacaoStatusController.java
├── service/
│   ├── ContratacaoServiceImpl.java
│   └── ContratacaoStatusServiceImpl.java
├── contrato/
│   ├── ContratacaoService.java
│   └── ContratacaoStatusService.java
├── repository/
│   └── ContratacaoRepository.java
├── models/
│   ├── Contratacao.java
│   └── StatusContratacao.java
└── dto/
    ├── ContratacaoRequest.java
    └── ContratacaoResponse.java
```

**Promoção:** ao surgir o segundo arquivo de um tipo, cria-se a pasta e movem-se **ambos** no
mesmo commit. Nunca deixe um na raiz e outro na pasta.

#### Regras de acoplamento entre módulos

- Módulo A chama módulo B **apenas** pela interface publicada em `B/contrato/`
- **Proibido** injetar o repository de outro módulo
- **Proibido** relacionamento JPA cruzando módulos — referencie pelo `id` (`Long` ou `UUID`, conforme [ADR-0007](./adr/0007-chave-primaria-mista.md))

A terceira regra é a que sustenta as outras duas: sem `@ManyToOne` cruzando fronteira, o
acoplamento acidental simplesmente não tem por onde entrar.

### 11.3 Frontend

```
frontend/
└── src/
    ├── middleware.ts             # refresh da sessão contra a API própria e proteção de rotas
    ├── app/                      # App Router — só roteamento, layout e composição
    │   ├── (publico)/            # landing, busca de profissionais, páginas abertas
    │   ├── (auth)/               # login, cadastro, recuperação de senha
    │   ├── (app)/                # área autenticada: painel do cliente e do profissional
    │   ├── layout.tsx            # root layout com providers (React Query, tema, sessão)
    │   └── page.tsx              # landing page pública
    ├── components/               # recebem dados por props; não buscam dados
    │   ├── ui/                   # atômicos reutilizáveis (Botao, Input, Modal, Card)
    │   ├── layout/               # estruturais (Cabecalho, Rodape, BarraLateral)
    │   └── forms/                # React Hook Form + schemas Zod de src/api/contratos
    ├── hooks/                    # comportamento e reatividade de UI, sem falar com o backend
    │                             # ex.: useDebounce, useMediaQuery, useDisclosure, useSessao
    └── api/                      # A FRONTEIRA — única camada que conhece o backend
        ├── client.ts             # wrapper do fetch: base URL, token, status, normalização de erro
        ├── erros.ts              # tipo de erro único, traduzido do ProblemDetail (RFC 9457)
        ├── contratos/            # schemas Zod por recurso + tipos inferidos por z.infer
        ├── profissionais.ts
        ├── servicos.ts
        ├── contratacoes.ts
        └── avaliacoes.ts
```

**A mesma regra de pasta vale aqui:** cada recurso é `<recurso>.ts` enquanto for um arquivo, e
vira a pasta `<recurso>/` ao surgir o segundo.

**Divisão de responsabilidade:** `app/` compõe, `components/` apresenta, `hooks/` reage,
`api/` conversa com o mundo. Um hook em `hooks/` que faz `fetch` está no lugar errado — ele
pertence a `api/`.

#### Pastas deliberadamente ausentes

| Pasta | Por que não existe |
|---|---|
| `types/` | Tipos globais não existem. Todo tipo de domínio nasce **inferido** dos schemas Zod em `src/api/contratos/`. Tipo escrito à mão em paralelo ao schema é duplicação que sai de sincronia. |
| `services/` | Substituída por `src/api/`, que concentra cliente HTTP, contratos e hooks de dados num só lugar. |
| `utils/` | Não definida nesta versão. Consulte `docs/adr/` antes de criar. |

---

## Referências

- [`arquitetura-sistema.json`](./arquitetura-sistema.json) — fonte da verdade
- [`adr/`](./adr/) — decisões de arquitetura e seus porquês
- [`planos/`](./planos/) — planos de execução
- [`relatorios/`](./relatorios/) — relatórios de sessão
