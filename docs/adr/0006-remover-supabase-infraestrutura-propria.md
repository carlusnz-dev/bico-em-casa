# ADR-0006 — Remover o Supabase e assumir infraestrutura própria

| Campo | Valor |
|---|---|
| **ADR** | `0006` |
| **Título** | Remover o Supabase e assumir infraestrutura própria (Postgres, autenticação e armazenamento) |
| **Autor** | Carlos Antunes |
| **Data** | 2026-08-22 |
| **Tópico** | Infra · Backend · Banco |
| **Status** | Aceito |
| **Substitui** | [ADR-0002](./0002-supabase-como-baas.md) |
| **LLM utilizada** | `Claude Opus 5 (Claude Code)` |

---

## Contexto

O [ADR-0002](./0002-supabase-como-baas.md) adotou o Supabase como BaaS — PostgreSQL gerenciado,
Auth e Storage — com o argumento de que construir identidade do zero consome sprints e erra caro.
O argumento continua tecnicamente correto. **O que mudou foi o objetivo do projeto.**

O Bico em Casa é o trabalho de uma disciplina de Engenharia de Software. Seu produto final não é
só a plataforma: é o que a equipe aprende construindo. E o ADR-0002, ao terceirizar identidade,
persistência e armazenamento de uma vez, terceirizou junto **exatamente as três áreas que a
disciplina existe para ensinar**.

Quatro forças concretas:

1. **Estudo de banco de dados e suas arquiteturas.** Com o Supabase, o schema `auth` é território
   fechado — a equipe não modela credencial, não modela sessão, não decide índice, não vê o custo
   de uma consulta que ela mesma escreveu. Metade do domínio de um sistema de autenticação fica
   invisível.
2. **Customização.** Regras próprias de sessão, rotação de token, política de senha e auditoria de
   acesso esbarram no que o fornecedor expõe. O que o Supabase não oferece, não se faz.
3. **Controle de segurança.** Hoje a superfície de autenticação é uma caixa-preta: a equipe não
   consegue auditar, testar nem explicar o que acontece entre o login e o token. Numa disciplina
   em que isso será avaliado, "o fornecedor cuida" não é resposta.
4. **Controle de custo.** O plano gratuito tem limite de banco, de banda e de projeto pausado por
   inatividade. Um projeto acadêmico que fica semanas parado entre entregas bate exatamente nesse
   caso.

O gatilho imediato foi a revisão do schema: a tabela `usuario` tinha `hash_senha`, o que
**contradiz frontalmente o ADR-0002** — se o Supabase custodia a credencial, o backend não tem o
que fazer com um hash de senha. Em vez de remover a coluna, a equipe decidiu remover a premissa.

## Decisão

**O Supabase sai integralmente do MVP.** As três capacidades passam a ser auto-hospedadas:

| Capacidade | Antes | Agora |
|---|---|---|
| Banco | Supabase Postgres gerenciado | **PostgreSQL 18** em container Docker (dev) e instância dedicada (prod) |
| Identidade | Supabase Auth | **Módulo `autenticacao` próprio** |
| Armazenamento | Supabase Storage | **MinIO** via Docker, compatível com a API S3 |
| E-mail | Supabase Auth (recuperação de senha) | **SMTP** com `spring-boot-starter-mail` |

**Autenticação própria, em detalhe:**

- Senha com **Argon2id** via `Argon2PasswordEncoder` do Spring Security, exigindo
  `org.bouncycastle:bcprov-jdk18on`. O hash vive em `tb_usuarios.hash_senha`
- **JWT próprio** assinado com par de chaves RSA. Emissão por `NimbusJwtEncoder`, validação por
  `NimbusJwtDecoder` — ambos já disponíveis via `spring-security-oauth2-jose`, sem biblioteca de
  JWT adicional. O `jjwt` **continua fora**, agora por outro motivo
- **Access token de 15 minutos**, refresh token de 30 dias persistido em `tb_refresh_tokens`,
  **rotacionado a cada uso** e revogável. Reuso de token rotacionado revoga a família inteira
- **Nenhum papel viaja no token.** O claim `sub` é o `id` de `tb_usuarios`; a autorização lê o
  papel de `tb_perfis` a cada requisição
- Recuperação de senha por token opaco de uso único, **armazenado com hash**, expiração curta

**Armazenamento, em detalhe:** o backend gera URL pré-assinada e o navegador fala direto com o
MinIO. Os bytes não trafegam pela API. Adapter em `lib/armazenamento`, usando
`software.amazon.awssdk:s3` — a mesma API do S3 real.

**O Supabase vai para o backlog**, não para o lixo. Se o projeto sobreviver à disciplina e
precisar de operação real com equipe pequena, a decisão merece ser reaberta — e este ADR será
substituído por outro.

## Alternativas Consideradas

| Opção | Prós | Contras | Por que foi recusada |
|---|---|---|---|
| **Manter o Supabase integralmente** (status quo do ADR-0002) | Entrega mais rápida; identidade testada em produção por milhares de projetos; zero código de segurança nosso | Fecha o acesso justamente às áreas que a disciplina avalia; schema `auth` inauditável; teto de customização; projeto pausa por inatividade | O objetivo do projeto mudou. Velocidade de entrega deixou de ser o critério dominante |
| **Manter só o Auth, auto-hospedar banco e storage** | Preserva a parte mais arriscada de escrever; libera o estudo de banco | Mantém a caixa-preta na superfície mais relevante para a avaliação; ainda depende do fornecedor estar de pé; correlação entre `auth.users` e o banco próprio vira ponto de falha distribuído | O Auth é precisamente o que a equipe quer estudar. Manter só ele preserva o que mais incomoda |
| **Keycloak auto-hospedado** | Identidade madura, OIDC completo, sem escrever criptografia própria; auto-hospedado atende custo e controle | Um servidor a mais para operar; curva de aprendizado da administração do Keycloak compete com o tempo de estudar o próprio domínio; ainda é caixa-preta, só que nossa | Troca uma caixa-preta por outra. Não entrega o aprendizado de modelagem de credencial e sessão |
| **Auth0 / Clerk** | Melhor DX do mercado; menos código ainda que o Supabase | Custo cresce por usuário ativo; mesma perda de aprendizado; dependência comercial mais forte | Agrava todos os problemas que motivaram a saída do Supabase |

## Consequências

### Positivas

- A equipe modela credencial, sessão, token e revogação **como domínio próprio** — que é o
  objetivo declarado da disciplina
- **Todo** o banco passa a ser versionado por Flyway. Não sobra schema fora do controle
- Custo de infraestrutura vai a zero em desenvolvimento: `docker-compose` sobe Postgres e MinIO
- A API S3 do MinIO é o padrão de mercado. O que se aprende aqui transfere direto para S3, R2 ou
  GCS
- O teste de integração fica mais fiel: o Testcontainers já sobe um Postgres real, e agora ele é
  **o mesmo** banco de produção, sem schema `auth` ausente
- Valida a regra de `lib/`: a troca de fornecedor de armazenamento ficou contida em uma pasta

### Negativas

- **Autenticação passa a ser código nosso, e código nosso tem bug nosso.** É a superfície onde
  bug custa mais caro: um erro de comparação de hash ou de validação de token não falha ruidoso,
  falha silencioso e explorável
- Aumenta o escopo do MVP em pelo menos: cadastro com verificação, login, refresh com rotação,
  logout com revogação, recuperação de senha com e-mail e expiração de token de recuperação
- Passa a ser responsabilidade da equipe: rotação da chave RSA, política de senha, proteção contra
  força bruta no login e contra enumeração de usuário no fluxo de recuperação
- Mais infraestrutura para operar e para os colegas de equipe subirem localmente: Postgres, MinIO,
  e um SMTP de desenvolvimento
- **Perde-se o que o ADR-0002 comprou.** O argumento original — "consome sprints e erra caro" —
  continua verdadeiro; a decisão é pagar esse preço conscientemente

### Neutras

- A coluna `supabase_user_id` deixa de existir. O `id` de `tb_usuarios` volta a ser a identidade
  canônica, sem correlação externa
- O frontend perde `@supabase/supabase-js` e `@supabase/ssr`. O `src/api/client.ts` passa a
  gerenciar access token em memória e refresh via cookie `httpOnly` — o
  [ADR-0005](./0005-src-api-como-unica-fronteira.md) continua valendo intacto
- O backend segue sendo OAuth2 Resource Server. Muda o emissor, não o modelo

## Impacto na Arquitetura

Esta decisão altera `docs/arquitetura-sistema.json`?

- [x] **Sim** — campos alterados:
  - `backend.security_configuration` (reescrito por inteiro)
  - `backend.dependencies.security`, `.external_integrations`, `.removed`
  - `frontend.dependencies.backend_integration`, `.removed`
  - `database.hosting`, `.migration_policy`, `.schemas`, `.naming_conventions.columns`
  - `external_services` (reescrito: `supabase` → `minio` + `smtp` + `_pendente_decisao`)
  - `directory_structure.backend…lib/` (`supabase/` → `armazenamento/` + `email/`)
  - `directory_structure.frontend.src.app/.middleware.ts` e `.api/.client.ts`
- [ ] Não

> O JSON, o `design-sistema.md` e este ADR vão no mesmo commit.

## Pendências que esta decisão cria

- **Requisitos afetados:** `RNF001`, `RNF004` e `RNF017` descreviam o Supabase e foram reescritos.
  `RNF019` a `RNF022` foram criados para cobrir hash de senha, ciclo de vida do token, proteção
  contra força bruta e o armazenamento em MinIO
- **Geocodificação continua sem ADR.** `RF013` exige distância em km; o fornecedor não foi
  escolhido. Precisa de ADR próprio antes da implementação
- **`docker-compose.yml` não existe ainda.** Postgres, MinIO e um SMTP de desenvolvimento
  (MailHog ou Mailpit) precisam subir juntos

## Referências

- [ADR-0002](./0002-supabase-como-baas.md) — a decisão que este ADR substitui
- [ADR-0004](./0004-estrutura-modular-por-dominio.md) — o módulo `autenticacao` que passa a
  custodiar credencial
- [ADR-0005](./0005-src-api-como-unica-fronteira.md) — permanece válido; muda só o que o cliente
  injeta no header
- [`requisitos.md`](../requisitos.md) — `RNF001`, `RNF004`, `RNF017`, `RNF019`–`RNF022`
- [OWASP Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html) — base para a escolha de Argon2id
- [OAuth 2.0 Security Best Current Practice, §4.14](https://datatracker.ietf.org/doc/html/draft-ietf-oauth-security-topics) — rotação de refresh token e detecção de reuso
