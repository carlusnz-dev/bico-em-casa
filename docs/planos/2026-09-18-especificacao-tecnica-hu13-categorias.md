# Plano — Especificação técnica: manutenção de categoria predefinida e correção de serviço pelo administrador (HU13 / RF027)

| Campo | Valor |
|---|---|
| **Plano** | Especificação técnica de `RF027` (HU13) |
| **Autor** | Claude (assistente) — a critério do dev responsável, o time assina como autor humano ao aprovar |
| **Data** | `2026-09-18` |
| **Escopo** | Backend |
| **Status** | Rascunho |
| **LLM utilizada** | `Claude Sonnet 5 (Claude Code)` |
| **ADRs relacionados** | ADR-0004 (estrutura modular), ADR-0007 (chave primária mista), ADR-0012 (paginação) |

---

## Contexto

`RF027` (registrado em `requisitos.json` v1.2.0) cobre a HU13 de
[`historias-usuario-administrador-servicos.md`](../historias-usuario-administrador-servicos.md):
o administrador mantém as categorias (`tag`) e corrige título/descrição/categorias de um serviço
cadastrado por um profissional.

**Por que isso é prioridade agora:** `RF010` (cadastro de serviço pelo profissional) já está
implementado — `POST /api/servico` existe e funciona. Mas a tabela `tag` só tem leitura
(`GET /api/tag`, em `TagController.java`) e **nenhum seed**. Sem categoria cadastrada, o
formulário de criar serviço não tem o que oferecer no campo de categoria. Este documento existe
para que o cadastro de serviço, que já compila, também funcione de ponta a ponta.

**Decisão em 2026-09-18: as categorias são predefinidas pelo backend, não criadas pelo
administrador pela API.** A lista de categorias nasce de dado semeado (seed) numa migration
Flyway, não de um `POST /api/tag`. Isso muda o escopo de "CRUD de categoria" desta HU13 para
"manutenção de categoria predefinida" — administrador continua podendo renomear e remover, mas
não cria uma categoria nova em tempo de execução.

Este documento é **especificação**, não implementação. Descreve contrato de endpoint, DTO e
regra de validação para o time escrever o código — controllers, services, repositories e a
migration de `log_acao` continuam sendo trabalho da equipe, conforme a Regra nº 3 do
`CLAUDE.md`.

## Objetivo

Ao final da implementação:

- A tabela `tag` nasce com uma lista predefinida de categorias, semeada por migration
- O administrador consegue renomear e remover uma categoria existente pela API — **não** criar
  uma nova em tempo de execução
- O administrador consegue corrigir título, descrição e categorias de qualquer serviço, com
  motivo obrigatório, e o profissional dono é notificado
- Nenhum dos dois fluxos acima está acessível a quem não tem perfil `ADMIN` — verificado no
  banco, nunca por claim do JWT (`RNF002`)

## Fora de Escopo

- **`RF028` e `RF029`** (faixa de preço por categoria, fila de aprovação) — dependem de ADR de
  schema ainda não escrito
- **Frontend** — a tela de administração de categorias só começa depois que este contrato
  estiver implementado e o time escrever o schema Zod em `src/api/` sobre ele
- **A notificação em si** (like o mecanismo de `RF012`) — HU13 CA4 exige "o profissional é
  notificado", mas o mecanismo de notificação (`notificacao`, de `RF012`) é assunto de outro
  documento; aqui só fica marcado como pré-requisito de integração

## Pré-requisitos

- [ ] **Migration de `log_acao`** — não existe hoje nenhuma migration nem entidade para essa
      tabela, embora `modelo-dados.dbml` já a projete (linha 147) e `RNF022` já a exija desde a
      v1.1.0. Sem ela, nem RF022 (já aprovado) nem RF026/RF027 conseguem gravar auditoria. É
      migration Flyway — trabalho da equipe. Convenção observada no código: o pacote real de
      núcleo compartilhado é `core/` (`core/excecao/`, `core/paginacao/`), não `comum/` como o
      ADR-0004 e o `CLAUDE.md` descrevem — **essa divergência de nome existe hoje no repositório
      e vale um ADR ou correção de documentação à parte**, fora do escopo deste plano. A entidade
      de auditoria, seguindo a nomeação real, provavelmente nasce em `core/auditoria/`
- [ ] **`RF026` implementado antes ou junto** — HU13 CA4 registra log de auditoria da mesma forma
      que HU12; faz sentido implementar as duas junto com a fundação de `log_acao`
- [ ] **Lista de categorias predefinidas e migration de seed** — o time decide *quais* categorias
      existem (ex.: "Elétrica", "Encanamento", "Limpeza"...) e escreve o `INSERT INTO tag (...)`
      na migration. É conteúdo de negócio, não escolha técnica, então não vem prescrito aqui.
      Sem isso, `RF010` (cadastro de serviço) continua sem categoria para oferecer no formulário

## Especificação

### 1. Manutenção de categoria predefinida (`tag`)

**Estado atual** (`TagController.java`, `TagService.java`, `TagServiceImpl.java`): só
`listarTodas()` → `GET /api/tag`. Schema (`V20260913223500__criar_tabelas_servico_e_tag.sql`) já
tem `uk_tag_nome` e `uk_tag_slug` — **nenhuma migration de schema nova é necessária**, só a
migration de **dado** (seed) do pré-requisito acima.

> [!NOTE]
> **HU13 CA1 já foi corrigido** em
> [`historias-usuario-administrador-servicos.md`](../historias-usuario-administrador-servicos.md)
> (2026-09-18) para descrever a categoria predefinida por seed, sem ação de criação pelo
> administrador. `RF027` ("administrador **mantém** as categorias") segue válido com a leitura
> de "manter" — renomear e remover, não criar.

Com a criação fora do endpoint, o CRUD encolhe para renomear e remover:

| Endpoint | Verbo | Autorização | Request | Response | Erro |
|---|---|---|---|---|---|
| `/api/tag/{id}` | `PUT` | `ADMIN` | `TagRequest { nome: String }` | `200` + `TagResponse` | `404` se id não existe; `409` em conflito de nome |
| `/api/tag/{id}` | `DELETE` | `ADMIN` | — | `204` | `409` se `servico_tag` tem alguma linha para essa tag (CA5 de HU13), com a contagem de serviços vinculados no corpo do `ProblemDetail` |

**`slug` não entra no `TagRequest`** — como a categoria não é mais criada pela API, e o slug é o
identificador de URL já indexado por quem usa a busca por categoria (`RF004`), a recomendação é
mantê-lo **imutável** depois do seed: renomear muda `nome`, não `slug`. Isso evita que uma troca
de nome quebre um link ou filtro salvo que aponta para a categoria pelo slug antigo. É uma
recomendação, não uma regra já registrada em HU13 — o time confirma.

**Regras que o service precisa cobrir** (`TagService`/`TagServiceImpl`):

- **Nome duplicado é case-insensitive e ignora espaço nas pontas** (CA2). A constraint
  `uk_tag_nome` do Postgres é *case-sensitive* — "Elétrica" e "elétrica" não colidem no banco.
  A checagem de duplicidade precisa normalizar (`trim` + `toLowerCase`, atenção a acentuação) e
  comparar **antes** do `UPDATE`, não confiar só na constraint
- **Renomear categoria vinculada não quebra vínculo nem filtro** (CA3) — como `servico_tag`
  referencia `tag_id` (não o nome), isso já é garantido pelo modelo relacional; não deveria
  exigir código extra além do próprio `UPDATE`
- **Remoção bloqueada com vínculo** (CA5) — precisa de uma consulta que conte quantos `servico`
  referenciam a tag (`TagRepository` ainda não tem esse método; hoje `Tag` não tem lado inverso
  do `@ManyToMany` mapeado)

### 2. Correção de serviço pelo administrador

**Estado atual** (`ServicoServiceImpl.java`): `editar(UUID id, ServicoRequest request, Long
usuarioId)` já existe, mas passa por `buscarServicoDoPerfil`, que **exige que o serviço pertença
ao perfil profissional do usuário autenticado** (`ServicoNaoPertenceAoPerfilException` se não
pertencer). Esse método não serve para o admin, que precisa editar o serviço de **qualquer**
profissional.

**Decisão de design em aberto** (múltiplas soluções defensáveis, o time escolhe):

| Opção | Vantagem | Custo |
|---|---|---|
| Endpoint novo, ex. `PUT /api/servico/{id}/moderar`, com seu próprio DTO (`ServicoModeracaoRequest { titulo, descricao, tagIds, motivo }`) e método próprio no service | Separa claramente a semântica de "dono edita" de "moderação edita", cada um com sua regra de autorização e efeito colateral (notificação + log) | Duplica um pouco de mapeamento de campo com `ServicoRequest` |
| Reaproveitar `PUT /api/servico/{id}` com um parâmetro/flag que pula a checagem de dono quando o usuário é `ADMIN` | Menos código novo | Mistura duas regras de autorização e dois efeitos colaterais diferentes no mesmo método, deixando `editar()` mais difícil de ler |

Recomendo a primeira opção — a favor por ser mais fácil de auditar depois (o método já deixa
óbvio, pelo nome, que é ação de moderação) — mas a escolha final é do time.

**Regras que o service precisa cobrir**, independente da opção escolhida:

- Verificar `ADMIN` via `perfilService.buscarPorUsuarioIdETipo(usuarioId, PerfilTipo.ADMIN)` —
  mesmo padrão já usado em todo o resto do código para `PROFISSIONAL`/`CLIENTE`, nunca claim do
  JWT (`RNF002`)
- `motivo` obrigatório, sem tamanho mínimo definido nas histórias (ao contrário de HU12, que pede
  10 caracteres) — **confirmar com o time se HU13 também exige um mínimo**
- Gravar em `log_acao`: autor (o admin), ação, alvo (`servico`, id), valor anterior e novo de
  cada campo alterado (CA4 pede isso explicitamente — é mais detalhado que o log de HU12)
- Disparar notificação ao profissional dono — depende do mecanismo de `RF012`, fora de escopo
  aqui (ver seção "Fora de Escopo")

## Riscos e Mitigações

| Risco | Impacto | Mitigação |
|---|---|---|
| `log_acao` não existir ainda bloqueia HU13 CA4 e também o já aprovado `RF022` | Alto | Priorizar a migration de `log_acao` antes ou junto deste CRUD |
| Confiar só na constraint `UNIQUE` do Postgres para duplicidade de nome de categoria | Médio | Checagem normalizada na camada de serviço, coberta por teste (Testcontainers, `RNF011`) |
| Reaproveitar `editar()` existente para moderação sem separar autorização | Médio | Preferir endpoint dedicado (ver seção 2) |

## Impacto na Arquitetura

Este plano altera `docs/arquitetura-sistema.json`?

- [ ] Sim
- [x] Não — usa módulo `servicos` já existente, nenhuma tabela nova, nenhuma fronteira nova.
      A migration de `log_acao` (pré-requisito) também não altera módulo, mas é boa prática
      confirmar com `arquiteto-sistema` se `core/auditoria/` é de fato o pacote pretendido antes
      de escrever, dado o desalinhamento de nome `core/` vs. `comum/` já registrado acima

## Verificação

1. `PUT /api/tag/{id}` renomeando para um nome que já existe em outra categoria (variando
   caixa/espaço) → `409 ProblemDetail`
2. `DELETE /api/tag/{id}` de categoria vinculada a serviço → `409 ProblemDetail` com contagem
3. Endpoint de moderação de serviço, chamado por usuário sem perfil `ADMIN` → `403 ProblemDetail`
4. Endpoint de moderação de serviço, chamado por `ADMIN` → serviço atualizado, linha nova em
   `log_acao`, profissional notificado
5. Migration de seed aplicada → `GET /api/tag` devolve a lista predefinida
6. `mvn test` (Testcontainers) cobrindo os casos acima

## Pendências

- [x] Categorias são predefinidas por seed de migration, não criadas pelo admin via API —
      decidido em 2026-09-18 (substitui a decisão anterior de slug informado pelo admin, que caiu
      junto: sem criação pela API, não há formulário de criação para pedir o slug)
- [x] Corrigir HU13 CA1 em `historias-usuario-administrador-servicos.md` — feito em 2026-09-18
- [ ] Time define a lista de categorias predefinidas e escreve a migration de seed
- [ ] Time confirma: `slug` fica imutável após o seed (recomendação desta especificação) ou pode
      ser editado junto do `nome`
- [ ] Time decide: endpoint de moderação dedicado ou flag no `editar()` existente (ver seção 2)
- [ ] Time confirma: motivo da correção de serviço (HU13) tem tamanho mínimo, como o de HU12?
- [ ] Migration de `log_acao` — quem escreve e quando, já que bloqueia `RF022`, `RF026` e `RF027`
- [ ] Resolver a divergência `core/` (código) vs. `comum/` (ADR-0004 e `CLAUDE.md`) — nome errado
      em algum dos dois lados
