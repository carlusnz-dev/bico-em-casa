# ADR-0008 — Nomenclatura de tabelas: `snake_case` singular, sem prefixo

| Campo | Valor |
|---|---|
| **ADR** | `0008` |
| **Título** | Nomenclatura de tabelas: `snake_case` singular, sem prefixo |
| **Autor** | Carlos Antunes |
| **Data** | `2026-08-22` |
| **Tópico** | Banco |
| **Status** | Aceito |
| **LLM utilizada** | `Claude Opus 5 (Claude Code)` |

---

## Contexto

A §5.2 do `design-sistema.md` foi escrita na sessão de fundação, antes de existir qualquer
tabela, e definiu `snake_case` **plural com prefixo `tb_`**: `tb_usuarios`, `tb_contratacoes`.
O prefixo é herança de bancos onde tabela, view e procedure dividem o mesmo espaço de nomes e
precisam ser distinguidas à vista. O PostgreSQL não tem esse problema.

Ao revisar o modelo no dbdiagram, o autor nomeou as tabelas de outra forma — `usuario`, `perfil`,
`servico`, `contratacao` — e essa é a forma que a equipe efetivamente usa ao falar do domínio.
Manter as duas convenções significaria traduzir mentalmente a cada consulta: a conversa diz
"contratação", o schema diz `tb_contratacoes`, a entidade JPA diz `Contratacao`.

O momento é o único barato para decidir: **não existe migration, não existe entidade, não existe
dado**. Depois de `V1__*.sql`, renomear tabela custa migration de rename, ajuste de toda
`@Table`, e um período em que os dois nomes circulam na cabeça do time.

## Decisão

Tabelas usam **`snake_case` singular, sem prefixo**.

| Elemento | Convenção | Exemplo |
|---|---|---|
| Tabela | `snake_case` singular, sem prefixo | `usuario`, `contratacao` |
| Tabela de junção | as duas pontas no singular, unidas por `_` | `servico_tag` |
| Tabela dependente | `pai_filho`, ambos no singular | `contratacao_anexo`, `portfolio_foto` |
| Constraint / índice | `uq_` · `idx_` · `ck_` · `fk_` + **o nome da tabela** | `uq_usuario_email`, `idx_contratacao_status` |

O nome da constraint acompanha o nome da tabela: como a tabela é `usuario`, a constraint é
`uq_usuario_email` — não `uq_usuarios_email`. Assim o nome continua sendo derivável do schema,
que é a única razão de existir uma convenção de nome de constraint.

**A tabela vira o nome da entidade JPA sem tradução:** `usuario` → `Usuario`,
`contratacao_anexo` → `ContratacaoAnexo`.

### Normalizações aplicadas ao rascunho do autor

O rascunho misturava singular e plural. Quatro nomes foram uniformizados para a regra acima:

| Rascunho | Adotado |
|---|---|
| `servicos_tags` | `servico_tag` |
| `contratacao_anexos` | `contratacao_anexo` |
| `log_acoes` | `log_acao` |
| `contratacao_historico` | `contratacao_historico` *(já conforme)* |

## Alternativas Consideradas

| Opção | Prós | Contras | Por que foi recusada |
|---|---|---|---|
| **`tb_` + plural** (§5.2 original) | Distingue tabela de view à vista; plural descreve a coleção, que é o que uma tabela é | Prefixo é ruído em 100% dos nomes para resolver um problema que o PostgreSQL não tem; obriga tradução mental entre schema, domínio e entidade | O custo é pago em toda linha de SQL e toda anotação `@Table`, para um ganho que não existe neste engine |
| **Plural sem prefixo** (`usuarios`) | Coleção descrita corretamente; sem ruído | Entidade JPA `Usuario` ≠ tabela `usuarios`, então toda `@Table(name=...)` vira explícita; plural em português tem irregularidade (`log_acoes`) que o singular não tem | Perde o casamento direto entre nome de tabela e nome de entidade |
| **Singular sem prefixo** | Casa com o domínio falado e com a entidade JPA; sem ruído; plural irregular deixa de ser problema | Uma linha é um registro, mas a tabela é uma coleção — o singular descreve a linha, não a tabela | **Escolhida.** O argumento do plural é semanticamente correto e perde para a praticidade de um nome só entre conversa, schema e código |

## Consequências

### Positivas

- Um nome só do domínio ao código: a conversa diz "contratação", o schema diz `contratacao`,
  a entidade diz `Contratacao`
- `@Table(name = ...)` deixa de ser obrigatória na maioria das entidades
- O plural irregular do português (`log_acoes`, `avaliacoes`) some do schema

### Negativas

- **Contraria a convenção que o próprio projeto registrou há poucas horas.** Quem ler apenas os
  relatórios das sessões anteriores vai encontrar `tb_usuarios` e não achar a tabela
- `usuario` é palavra comum; sem prefixo, um `SELECT * FROM usuario` num script solto dá menos
  pista de que se trata de tabela da aplicação

### Neutras

- Nenhum dado migra: a decisão acontece antes de existir `V1__*.sql`
- Os relatórios de sessão e os ADR-0002 e ADR-0006 continuam citando `tb_*`. São registro
  histórico datado e **não são reescritos** — refletem o que era verdade quando foram escritos

## Impacto na Arquitetura

Esta decisão altera `docs/arquitetura-sistema.json`?

- [x] Sim — campo alterado: `database.naming_conventions.tables`, além das referências a nomes de
      tabela em `backend.security_configuration`
- [ ] Não

Espelhado em `docs/design-sistema.md` §5.2 no mesmo commit. Propagado para `modelo-dados.dbml`,
`modelo-dados.md`, `requisitos.json`, `requisitos.md`, `matriz-rastreabilidade.md` e o plano de
custeio.

## Referências

- [`modelo-dados.dbml`](../modelo-dados.dbml) — o modelo v4.0.0, já na convenção
- [ADR-0007](./0007-chave-primaria-mista.md) — a outra metade da convenção §5.2
- PostgreSQL — tabelas, views e sequences compartilham o mesmo namespace de relações, mas o
  `\dt` e o `information_schema` já as separam por tipo; o prefixo não acrescenta informação
