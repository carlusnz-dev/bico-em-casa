# ADR-0007 — Chave primária mista: `bigint` no cadastro, `uuid` no transacional

| Campo | Valor |
|---|---|
| **ADR** | `0007` |
| **Título** | Chave primária mista: `bigint` no cadastro, `uuid` no transacional |
| **Autor** | Carlos Antunes |
| **Data** | `2026-08-22` |
| **Tópico** | Banco |
| **Status** | Aceito |
| **LLM utilizada** | `Claude Opus 5 (Claude Code)` |

---

## Contexto

A convenção §5.2 do `design-sistema.md`, escrita antes de existir qualquer tabela, definia
`id UUID DEFAULT gen_random_uuid()` para **todas** as chaves primárias. A justificativa era a
regra de fronteira do [ADR-0004](./0004-estrutura-modular-por-dominio.md): módulo referencia
módulo pelo `id`, e um tipo único simplifica isso.

Ao refazer o modelo no dbdiagram, o autor chegou a uma distribuição diferente por conta própria:
`bigint` em `usuario`, `perfil` e `portfolio`; `uuid` no restante. Não foi descuido — as tabelas
que ficaram com `bigint` são exatamente as de cadastro, que quase toda consulta usa como alvo de
`JOIN`, e as que ficaram com `uuid` são as que nascem por evento e aparecem em URL.

As duas escolhas têm base técnica real, e a diferença aparece em três lugares concretos:

1. **Custo de índice.** `bigint` ocupa 8 bytes e é sequencial; `uuid` v4 ocupa 16 e é aleatório.
   Numa B-tree, o sequencial insere sempre na página mais à direita e mantém a árvore densa; o
   aleatório espalha a escrita e infla o índice. Em tabela de cadastro, referenciada por 12 FKs,
   isso se multiplica.
2. **Exposição em URL.** `/api/perfis/1`, `/api/perfis/2` permite varrer a base contando de 1 em
   1 e descobrir quantos usuários existem. `uuid` não permite.
3. **Referência polimórfica.** `tb_log_acoes.alvo_id` e `tb_notificacoes.alvo_id` apontam para
   entidades de tabelas diferentes. Uma coluna tem um tipo só, então com PK mista esses campos
   não podem ser nem `bigint` nem `uuid`.

O ponto 3 é o que força a decisão a virar ADR: ele não é uma preferência de estilo, é uma dívida
que passa a existir no schema.

## Decisão

A chave primária deste projeto é **mista**, por critério explícito:

| Tipo | Tabelas | Critério |
|---|---|---|
| `BIGINT GENERATED ALWAYS AS IDENTITY` | `tb_usuarios`, `tb_perfis`, `tb_enderecos`, `tb_portfolios` | **Cadastro** — cresce devagar, é alvo da maioria dos `JOIN` |
| `UUID DEFAULT gen_random_uuid()` | as outras 14 tabelas | **Transacional** — nasce por evento, cresce rápido, aparece em URL |

Toda coluna de **alvo polimórfico** é `varchar(64)` **sem foreign key**:
`tb_log_acoes.alvo_id`, `tb_notificacoes.alvo_id` e `tb_denuncias.alvo_id`. O par
(`alvo_tipo`, `alvo_id`) é validado pelo *service*, não pelo banco, e precisa de teste que o
cubra.

`tb_perfis` recebe `nome_usuario varchar(50) UNIQUE` e `tb_portfolios` recebe
`slug_url varchar(120) UNIQUE`, de forma que a URL pública use o *slug* e não o `bigint` —
isso neutraliza a enumeração no único lugar do MVP onde o id de cadastro sairia para o cliente.

## Alternativas Consideradas

| Opção | Prós | Contras | Por que foi recusada |
|---|---|---|---|
| **`uuid` em todas** (§5.2 original) | Tipo único; `alvo_id` vira `uuid` com tipagem real; nenhum id sequencial exposto | Índice maior e escrita espalhada nas tabelas mais referenciadas; `JOIN` de 16 bytes | Não foi recusada por estar errada — é defensável. Perdeu porque o autor quis a densidade de índice no cadastro, e porque o *slug* já resolve a exposição |
| **`bigint` em todas** | Índice mais denso em tudo; `alvo_id` volta a ter tipo; `JOIN` mais barato | `/api/contratacoes/1` expõe volume de negócio; id de contratação é adivinhável | O id de contratação circula em link e notificação. Adivinhável é risco de acesso indevido, não só de vazamento estatístico |
| **`bigint` PK + `uuid` público** em toda tabela | Melhor dos dois: índice denso e URL opaca | Duas colunas de identidade em 18 tabelas; todo repository precisa dos dois caminhos de busca; risco de vazar a coluna errada no DTO | Custo de disciplina alto demais para um MVP de disciplina acadêmica. Fica registrado como o caminho de evolução se a base crescer |

## Consequências

### Positivas

- Índice e `JOIN` mais baratos nas quatro tabelas mais referenciadas do modelo
- `tb_contratacoes`, `tb_avaliacoes` e `tb_denuncias` continuam com id opaco, que é onde a
  adivinhação de id teria consequência de acesso
- O modelo passa a refletir a intenção de quem o desenhou, e não uma convenção herdada de um
  template

### Negativas

- **`tb_notificacoes.alvo_id` perde a integridade referencial.** O banco não impede uma
  notificação apontar para uma contratação que não existe. É dívida assumida, coberta por teste
  de service, não por constraint
- **Toda comparação de `alvo_id` exige aspas.** `WHERE alvo_id = 42` falha; o correto é
  `WHERE alvo_id = '42'`. É o tipo de erro que só aparece em runtime
- O desenvolvedor precisa saber de cabeça qual tabela é `Long` e qual é `UUID` ao escrever DTO e
  assinatura de método. A tabela da §5.2 vira consulta obrigatória

### Neutras

- `tb_log_acoes.alvo_id` também fica sem FK, mas ali isso **já era desejável**: uma FK com
  `CASCADE` apagaria a prova junto com o registro auditado, e com `RESTRICT` impediria apagar
  qualquer linha já auditada
- A §5.2 deixa de ter uma linha de chave primária e passa a ter três

## Impacto na Arquitetura

Esta decisão altera `docs/arquitetura-sistema.json`?

- [x] Sim — campos alterados: `database.naming_conventions.primary_keys`,
      `database.naming_conventions.polymorphic_references` (novo),
      `security.claim_mapping`, `backend.modulos.regras_de_acoplamento`
- [ ] Não

Espelhado em `docs/design-sistema.md` §5.2, §4 (correlação de identidade) e nas regras de
acoplamento, no mesmo commit.

## Referências

- [`modelo-dados.md`](../modelo-dados.md) §3.1 — o mesmo trade-off explicado para quem vai codar
- [`modelo-dados.dbml`](../modelo-dados.dbml) — o modelo v3.0.0
- [ADR-0004](./0004-estrutura-modular-por-dominio.md) — a regra de referência por `id` entre módulos
- PostgreSQL 18 — `uuid` ocupa 16 bytes, `bigint` 8; `gen_random_uuid()` gera v4 (aleatório)
