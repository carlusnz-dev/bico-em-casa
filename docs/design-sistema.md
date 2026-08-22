# Design do Sistema — Bico em Casa

> [!IMPORTANT]
> **Este documento é o espelho legível de [`arquitetura-sistema.json`](./arquitetura-sistema.json).**
> O JSON é a **única fonte da verdade**. É proibido editar este markdown sem antes editar o JSON,
> e as duas alterações vão **no mesmo commit**. Divergência entre os dois é considerada bug.

| | |
|---|---|
| **Projeto** | `bico-em-casa` |
| **Versão do documento** | 2.0.0 |
| **Última atualização** | 2026-08-22 |
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
│   (App Router)  │   Bearer JWT (Supabase)  │   API REST           │
│                 │ ◄─────────────────────── │                      │
└────────┬────────┘      RFC 9457 errors     └──────────┬───────────┘
         │                                              │
         │ auth + upload assinado                       │ JDBC + JWKS + Storage API
         ▼                                              ▼
┌──────────────────────────────────────────────────────────────────┐
│                            Supabase                              │
│      Auth (identidade)  ·  PostgreSQL 18  ·  Storage (arquivos)  │
└──────────────────────────────────────────────────────────────────┘
```

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
| **Core** | `spring-boot-starter-web`, `spring-boot-starter-validation`, `spring-boot-starter-actuator` |
| **Persistência** | `spring-boot-starter-data-jpa`, `org.postgresql:postgresql`, `flyway-core:13.3.x`, `flyway-database-postgresql:13.3.x` |
| **Segurança** | `spring-boot-starter-security`, `spring-boot-starter-oauth2-resource-server` |
| **Produtividade** | `lombok`, `mapstruct:1.6.3`, `mapstruct-processor:1.6.3` |
| **Documentação** | `springdoc-openapi-starter-webmvc-ui:3.1.x` |
| **Integrações** | Spring `RestClient` para a Storage API do Supabase |

#### Removido em relação à versão 1.0.0

| Artefato | Motivo |
|---|---|
| `io.jsonwebtoken:jjwt-*` | A emissão e assinatura de token passou a ser responsabilidade do Supabase Auth. O backend apenas **valida** o JWT via JWKS como OAuth2 Resource Server, o que dispensa biblioteca própria de JWT. |

### 3.2 Segurança

| Item | Definição |
|---|---|
| Provedor de identidade | Supabase Auth |
| Estratégia | Stateless. O backend atua como **OAuth2 Resource Server** e valida o JWT emitido pelo Supabase contra o JWKS do projeto. |
| JWKS URI | `https://<project-ref>.supabase.co/auth/v1/.well-known/jwks.json` |
| Issuer | `https://<project-ref>.supabase.co/auth/v1` |
| Correlação de identidade | O claim `sub` do JWT (UUID do Supabase) é a chave de correlação com `tb_usuarios` pela coluna `supabase_user_id` |
| Autorização | Por papel (`CLIENTE`, `PROFISSIONAL`, `ADMIN`), resolvido no backend a partir de `tb_usuarios` — **nunca** confiando em claim editável pelo cliente |
| CORS | Restrito por ambiente via `CorsConfigurationSource`; origens definidas por profile |
| CSRF | Desabilitado (API stateless, sem cookie de sessão) |
| Encoder de senha | Não aplicável — credenciais são custodiadas pelo Supabase Auth |
| Segredos | Nenhuma chave em código. `SUPABASE_SERVICE_ROLE_KEY` **somente no backend**, jamais exposta ao frontend |

### 3.3 Testes

| Grupo | Artefatos |
|---|---|
| Unitário e integração | `spring-boot-starter-test`, `junit-jupiter`, `mockito-core`, `mockito-junit-jupiter`, `assertj-core` |
| Containers | `testcontainers:2.0.x`, `testcontainers:junit-jupiter`, `testcontainers:postgresql` |
| Segurança | `spring-security-test` |

**Política:** teste de integração sobe um PostgreSQL 18 real via Testcontainers. É **proibido**
validar regra de negócio contra banco em memória (H2) — o comportamento diverge do de produção
justamente nos pontos que importam.

---

## 4. Frontend

| Item | Valor |
|---|---|
| Framework | Next.js 16.3.x (App Router) |
| Runtime | Node.js 24 LTS |
| Linguagem | TypeScript 7.0.x (strict mode) |

**Por que TypeScript 7:** compilador nativo em Go, GA desde 08/07/2026. O typecheck e o feedback
no editor ficam ordens de grandeza mais rápidos, mantendo a mesma semântica de tipos da linha 5.x.

### 4.1 Dependências

| Grupo | Pacotes |
|---|---|
| **Core** | `next@16.3.x`, `react@19.2.x`, `react-dom@19.2.x`, `typescript@7.0.x` |
| **Estado e dados** | `@tanstack/react-query@5.101.x`, `zustand@5.0.x` |
| **Estilo e UI** | `tailwindcss@4.3.x`, `lucide-react`, `clsx`, `tailwind-merge`, `radix-ui` |
| **Formulários** | `react-hook-form`, `zod@4.4.x`, `@hookform/resolvers` |
| **Integração** | `@supabase/supabase-js`, `@supabase/ssr` |

#### Removido em relação à versão 1.0.0

| Pacote | Motivo |
|---|---|
| `axios` | O `fetch` nativo do Next 16 participa do cache e da revalidação do App Router; o axios contorna esse mecanismo. O cliente HTTP próprio vive em `src/api/client.ts`. |

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
| Hospedagem | Supabase (instância gerenciada) |
| Ferramenta de migration | Flyway 13.3.x |
| Caminho das migrations | `backend/src/main/resources/db/migration` |

**Política de migration:** toda alteração de schema nasce como migration versionada do Flyway.
É **proibido** alterar schema pelo painel do Supabase — a alteração some do histórico e o próximo
`flyway migrate` diverge.

### 5.1 Schemas

| Schema | Papel |
|---|---|
| `public` | Tabelas de domínio da aplicação, gerenciadas pelo Flyway |
| `auth` | Schema proprietário do Supabase Auth. Somente leitura para a aplicação; **jamais** versionado pelo Flyway |

### 5.2 Convenções de Nomenclatura

| Elemento | Convenção | Exemplo |
|---|---|---|
| Tabelas | `snake_case` plural com prefixo `tb_` | `tb_usuarios`, `tb_contratacoes` |
| Colunas | `snake_case` | `created_at`, `usuario_id` |
| Chave primária | `id UUID DEFAULT gen_random_uuid()` | `id` |
| Chave estrangeira | `fk_{tabela_origem}_{tabela_destino}` | `fk_contratacoes_profissionais` |
| Índice | `idx_{tabela}_{coluna}` | `idx_profissionais_cidade` |
| Constraint única | `uq_{tabela}_{coluna}` | `uq_usuarios_email` |
| Constraint de check | `ck_{tabela}_{regra}` | `ck_avaliacoes_nota_valida` |

---

## 6. Serviços Externos — Supabase

**Papel:** Backend as a Service — banco gerenciado, identidade e armazenamento de arquivos.

### 6.1 O que é usado

| Capacidade | Uso |
|---|---|
| **Database** | PostgreSQL 18 gerenciado, acessado pelo backend via JDBC e versionado por Flyway |
| **Auth** | Cadastro, login, refresh token e recuperação de senha. Emissor do JWT validado pelo backend |
| **Storage** | Bucket `portfolios` para as imagens de portfólio dos profissionais e bucket `avatares` para fotos de perfil |

### 6.2 O que **não** é usado

| Capacidade | Por que não |
|---|---|
| **RLS como autorização primária** | A autorização de negócio é responsabilidade do backend Spring. O RLS entra apenas como defesa em profundidade, **nunca** como única barreira. |
| **Acesso direto via PostgREST** | O frontend **não** acessa tabelas de domínio direto pelo `supabase-js`. Todo dado de negócio passa pela API Spring. |

**Escopo no cliente:** no frontend, o `supabase-js` é usado exclusivamente para o fluxo de
autenticação e para upload assinado no Storage.

**Onde vive o adapter:**

- Backend — `br.com.bicoemcasa.api.lib.supabase`
- Frontend — `src/api/` (cliente) e `src/middleware.ts` (sessão)

---

## 7. Git e Versionamento

### 7.1 Estratégia de Branches

| Branch | Papel |
|---|---|
| `main` | Produção. Deploy versionado por tags `vX.Y.Z`. **Commits diretos proibidos.** |
| `develop` | Staging/homologação. Branch de integração das features concluídas. |
| `feat/<ticket-id>-<descricao-curta>` | Feature, originada de `develop` e mesclada via Pull Request |
| `fix/<ticket-id>-<descricao-curta>` | Correção, originada de `develop` |
| `hotfix/<ticket-id>-<descricao-curta>` | Correção urgente, originada de `main` e retroportada para `develop` |
| `docs/<descricao-curta>` | Alterações exclusivas de documentação |

### 7.2 Conventional Commits

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

## 8. CI/CD — GitHub Actions

### `backend-ci.yml`

**Gatilhos:** `pull_request → develop`, `pull_request → main`

1. Checkout do repositório
2. Setup Java 21 (Temurin) com cache Maven
3. Verificar formatação com Spotless
4. Executar testes unitários e de integração com Testcontainers
5. Build do artefato JAR (`mvn -B package`)
6. Upload de relatório de cobertura (JaCoCo)

### `frontend-ci.yml`

**Gatilhos:** `pull_request → develop`, `pull_request → main`

1. Checkout do repositório
2. Setup Node.js 24 com cache de dependências
3. Executar ESLint e Prettier em modo check
4. Executar typecheck (`tsc --noEmit`)
5. Executar testes unitários com Vitest
6. Build do Next.js (`next build`)

### `docs-parity.yml`

**Gatilhos:** `pull_request → develop`, `pull_request → main`

1. Falhar o build se `docs/arquitetura-sistema.json` foi alterado sem alteração em `docs/design-sistema.md` no mesmo PR
2. Validar que o JSON é sintaticamente válido

---

## 9. Padrões de Código

### 9.1 Backend

| Item | Ferramenta |
|---|---|
| Formatador | Spotless com Google Java Style |
| Linter | SonarLint / Checkstyle |

**Regras:**

- Princípios SOLID e Clean Code rigorosos
- Tratamento global de exceções via `@RestControllerAdvice` com **RFC 9457** (`ProblemDetail`)
- Imutabilidade prioritária: `record` para DTOs, coleções não modificáveis
- Injeção de dependência estrita por construtor (campos `final`)
- Todo service público tem **interface em `contrato/`** e **implementação em `service/`** terminada em `Impl`
- Entidade JPA **nunca** cruza a fronteira do controller: sempre DTO
- Um módulo só acessa outro pela interface exposta em `contrato/`, nunca pelo repository alheio

### 9.2 Frontend

| Item | Ferramenta |
|---|---|
| Linter | ESLint com `eslint-config-next` e `@typescript-eslint/recommended` |
| Formatador | Prettier com `prettier-plugin-tailwindcss` |

**Regras:**

- Server Components por padrão; `'use client'` apenas para interatividade e hooks
- Componentes funcionais com tipagem explícita de props
- Validação de entrada e saída com schemas Zod na fronteira `src/api/`
- Proibido `any` — use `unknown` com narrowing por schema
- Nenhum `fetch` fora de `src/api/`

### 9.3 Comentários e Documentação

**Política:** código autoexplicativo substitui comentário redundante.
**Idioma:** comentários, Javadoc e TSDoc em português.

| Permitido | Proibido |
|---|---|
| Javadoc/TSDoc em interfaces públicas de `contrato/` e utilitários complexos | Código comentado (*dead code*) |
| Justificativa de decisão de negócio não trivial ou de complexidade algorítmica | Comentário narrando a sintaxe óbvia do código |
| `TODO`/`FIXME` vinculados a um issue explícito — ex.: `// TODO(BEC-123): revisar após migração V2` | |

---

## 10. Estrutura de Diretórios

### 10.1 Raiz do Repositório

```
bico-em-casa/
├── backend/              # Aplicação Spring Boot (Maven)
├── frontend/             # Aplicação Next.js
├── docs/                 # Documentação viva (design, ADRs, planos, relatórios)
├── .claude/              # Configuração do Claude Code
└── .github/workflows/    # Pipelines de CI
```

### 10.2 Backend

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
    │   │   │   └── supabase/    # SupabaseProperties, SupabaseStorageClient, JwtDecoder
    │   │   ├── comum/           # núcleo compartilhado entre módulos
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

#### `config/` vs `lib/` vs `comum/`

Três pastas transversais com responsabilidades que não se sobrepõem:

| Pasta | Responsabilidade | Regra |
|---|---|---|
| `config/` | Configuração **da nossa aplicação**: security, CORS, OpenAPI, beans | Só configuração, sem lógica |
| `lib/` | Adapters de serviços **externos** (Supabase, e-mail, mapas) | **Nada de regra de negócio.** Só tradução entre o mundo externo e tipos internos |
| `comum/` | Núcleo compartilhado entre módulos | Não depende de nenhum módulo; é dependido por todos |

O valor de `lib/` é o isolamento do fornecedor: se o Supabase for trocado amanhã, **só essa
pasta muda**.

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
- **Proibido** relacionamento JPA cruzando módulos — referencie pelo `id` (UUID)

A terceira regra é a que sustenta as outras duas: sem `@ManyToOne` cruzando fronteira, o
acoplamento acidental simplesmente não tem por onde entrar.

### 10.3 Frontend

```
frontend/
└── src/
    ├── middleware.ts             # refresh da sessão Supabase e proteção de rotas
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
