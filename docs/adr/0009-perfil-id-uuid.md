# ADR-0009 — `perfil.id` como `uuid`, exceção ao critério de cadastro do ADR-0007

| Campo | Valor |
|---|---|
| **ADR** | `0009` |
| **Título** | `perfil.id` como `uuid`, exceção ao critério de cadastro do ADR-0007 |
| **Autor** | Carlos Antunes |
| **Data** | `2026-09-01` |
| **Tópico** | Banco |
| **Status** | Aceito |
| **LLM utilizada** | `Claude Sonnet 5 (Claude Code)` |

---

## Contexto

O [ADR-0007](./0007-chave-primaria-mista.md) classifica `perfil` como tabela de **cadastro** e
prescreve `BIGINT GENERATED ALWAYS AS IDENTITY` para ela, junto com `usuario`, `endereco` e
`portfolio` — pelo critério de que são tabelas que crescem devagar e são alvo da maioria dos
`JOIN`s do sistema. `perfil` é, das quatro, a mais referenciada: 13 colunas em 7 tabelas
(`portfolio`, `disponibilidade`, `servico`, `contratacao` ×3, `contratacao_historico`,
`notificacao`, `avaliacao` ×2, `denuncia` ×3) apontam para `perfil.id` por FK.

Ao implementar a entidade JPA `Perfil` (`modulos/usuarios/models/Perfil.java`), o autor optou por
`@GeneratedValue(strategy = GenerationType.UUID)` em vez de `IDENTITY`, na contramão do que o
ADR-0007 definia para essa tabela. O motivo não é o argumento de exposição em URL que o ADR-0007
já trata para `perfil` via `nome_usuario VARCHAR(50) UNIQUE` (o *slug* público) — esse ponto
continua resolvido independente do tipo da PK. O motivo é **consistência de tipo dentro do
módulo `usuarios`**: preferir um único tipo de identificador (`UUID`) no código Java do módulo,
em vez de misturar `Long` (`Usuario`, `Endereco`) e `UUID` (`Perfil`) na mesma pasta de entidades.

Como a divergência já existia no código antes de virar ADR, este documento formaliza uma decisão
já tomada — não abre uma discussão nova.

## Decisão

`perfil.id` é **`UUID DEFAULT gen_random_uuid()`**, não `BIGINT GENERATED ALWAYS AS IDENTITY`
como as demais tabelas de cadastro. É uma exceção pontual ao critério do ADR-0007, que continua
valendo sem alteração para `usuario`, `endereco` e `portfolio`.

Toda coluna de outra tabela que referencia `perfil.id` por FK acompanha o tipo: `portfolio.perfil_id`,
`disponibilidade.perfil_id`, `servico.perfil_id`, `contratacao.cliente_perfil_id`,
`contratacao.profissional_perfil_id`, `contratacao.cancelado_por_perfil_id`,
`contratacao_historico.alterado_por_perfil_id`, `notificacao.destinatario_perfil_id`,
`avaliacao.autor_perfil_id`, `avaliacao.avaliado_perfil_id`, `denuncia.autor_perfil_id`,
`denuncia.denunciado_perfil_id` e `denuncia.analisado_por_perfil_id` — todas `UUID`, refletidas em
`docs/modelo-dados.dbml` v4.1.0.

## Alternativas Consideradas

| Opção | Prós | Contras | Por que foi recusada |
|---|---|---|---|
| **Reverter `perfil.id` para `bigint`, conforme ADR-0007** | Segue o critério original à risca; mantém a densidade de índice na tabela mais referenciada do modelo (13 FKs); `JOIN` mais barato em toda consulta que resolve perfil | Módulo `usuarios` mistura `Long` (`Usuario`, `Endereco`) e `UUID` (`Perfil`) nas assinaturas de repository/service | Foi a opção tecnicamente mais alinhada ao ADR-0007, mas o autor decidiu que a consistência de tipo pesa mais nesse módulo específico |
| **Manter `perfil.id` como `uuid`** (escolhida) | Um único tipo de id no módulo `usuarios`; menos chance de erro de assinatura (`Long` vs `UUID`) ao escrever DTO, repository e service | Reverte a densidade de índice que o próprio ADR-0007 justificava para `perfil`; propaga `UUID` para 13 colunas em 7 tabelas futuras que hoje nem existem | Aceita o custo de índice em troca de menos superfície de erro humano no código de um módulo que a equipe está aprendendo a escrever |

## Consequências

### Positivas

- Toda entidade do módulo `usuarios` usa `UUID` como PK — uma assinatura de método a menos para
  errar (`Long` vs `UUID`) ao escrever repository, service e DTO
- `perfil.nome_usuario` continua sendo o único identificador que sai em URL pública,
  independente do tipo da PK — nada muda na superfície de enumeração

### Negativas

- **Reverte, só para `perfil`, o argumento central do ADR-0007**: `perfil` é a tabela mais
  referenciada por FK do modelo (13 colunas em 7 tabelas), e é exatamente aí que a densidade de
  índice do `bigint` mais valeria
- **Propaga `UUID` para 13 colunas em 7 tabelas que ainda não existem** (`portfolio`,
  `disponibilidade`, `servico`, `contratacao`, `contratacao_historico`, `notificacao`,
  `avaliacao`, `denuncia`) — quem escrever essas migrations precisa saber que a FK para `perfil`
  é `UUID`, não `BIGINT` como as demais tabelas de cadastro
- Todo `JOIN` que resolve `perfil` a partir de outra tabela paga os 16 bytes do `UUID` em vez
  dos 8 do `BIGINT`, nas 7 tabelas acima

### Neutras

- `usuario`, `endereco` e `portfolio` continuam `BIGINT` — o ADR-0007 não muda para elas
- `docs/modelo-dados.dbml` sobe para v4.1.0; `docs/modelo-dados.md` §3.1 passa a ter três linhas
  de critério de PK em vez de duas

## Impacto na Arquitetura

Esta decisão altera `docs/arquitetura-sistema.json`?

- [x] Sim — campo alterado: `database.naming_conventions.primary_keys`
- [ ] Não

Espelhado em `docs/design-sistema.md` §5.2 (tabela de convenções de nomenclatura) e em
`docs/modelo-dados.dbml`/`docs/modelo-dados.md` §3.1, no mesmo commit.

## Referências

- [ADR-0007](./0007-chave-primaria-mista.md) — critério de chave primária mista que este ADR
  excepciona pontualmente para `perfil`
- [`modelo-dados.dbml`](../modelo-dados.dbml) — v4.1.0, com `perfil.id` e as 13 colunas
  dependentes em `uuid`
- `backend/src/main/java/br/com/bicoemcasa/api/modulos/usuarios/models/Perfil.java` — onde a
  decisão já estava implementada antes deste registro
