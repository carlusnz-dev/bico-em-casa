# Relatório de Sessão — 2026-09-01

| Campo | Valor |
|---|---|
| **Sessão** | Diagnóstico da FK endereco→perfil na migration V2 e ADR da exceção `perfil.id` em `uuid` |
| **Autor** | Carlos Antunes & Claude Code |
| **Data** | `2026-09-01` |
| **Duração aproximada** | `1h30` |
| **LLM utilizada** | `Claude Sonnet 5 (Claude Code)` |
| **Branch** | `feat/modulo-autenticacao` |
| **Commits** | `77663da`, `c0e923a` |
| **Plano relacionado** | Nenhum |

---

## Resumo

O desenvolvedor trouxe as entidades `Endereco`, `Perfil` e `PerfilTipo` (já escritas por ele) e a
migration `V2__criar-tabelas-endereco-perfil.sql` gerada pelo IntelliJ a partir delas, com um
problema: a FK customizada `perfil.endereco_id → endereco.id` definida via
`@ForeignKey(foreignKeyDefinition = "... ON DELETE SET NULL")` em `Perfil.java` não saiu com essa
cláusula na migration gerada.

A sessão teve duas partes. Na primeira, diagnosticou-se a causa raiz (ferramentas de diff de
schema, como o gerador do IntelliJ, não interpretam a string livre de `foreignKeyDefinition` —
só o próprio Hibernate faz isso, e só em runtime), e revisaram-se três divergências adicionais
entre `Perfil.java`/a migration e a fonte de verdade documentada (`ADR-0007`, `ADR-0008`,
`modelo-dados.dbml`): nome de FK fora do padrão, unique constraint referenciando uma coluna
inexistente, e `Perfil.id` como `UUID` em vez do `BIGINT` que o ADR-0007 prescreve para tabelas
de cadastro. O desenvolvedor corrigiu os três primeiros pontos por conta própria (Regra nº 3 —
migration e entidade são código da equipe).

Na segunda parte, o desenvolvedor decidiu manter `Perfil.id` como `UUID` por preferência de
consistência de tipo dentro do módulo `usuarios` (não por argumento técnico contra o ADR-0007) e
pediu o registro formal dessa exceção. A exploração mostrou que a mudança não é isolada: 13
colunas de FK em 7 tabelas ainda não criadas (`portfolio`, `disponibilidade`, `servico`,
`contratacao` ×3, `contratacao_historico`, `notificacao`, `avaliacao` ×2, `denuncia` ×3)
referenciam `perfil.id` e precisam acompanhar o tipo. O desenvolvedor confirmou que queria essas
13 colunas já refletidas no modelo, e o resultado foi o ADR-0009 mais a atualização de
`arquitetura-sistema.json`, `design-sistema.md`, `modelo-dados.dbml` e `modelo-dados.md` no mesmo
fluxo (JSON → markdown → ADR, conforme a Regra nº 1).

## O que foi feito

- **Diagnóstico da causa raiz da FK sem `ON DELETE SET NULL`**: explicado por que
  `foreignKeyDefinition` não é lido por ferramentas de diff de schema externas ao Hibernate, e
  apontada a linha exata a corrigir na migration.
- **Revisão de código que encontrou 3 divergências adicionais** entre `Perfil.java`/a migration
  V2 e a documentação de arquitetura vigente (nome de FK, unique constraint com coluna
  inexistente, tipo de `Perfil.id`) — reportadas, não corrigidas pela LLM (Regra nº 3).
- **`ADR-0009` criado**, registrando `perfil.id` como exceção pontual ao critério de cadastro do
  ADR-0007, com o motivo real (consistência de tipo no módulo `usuarios`) e as consequências
  (perda da densidade de índice na tabela mais referenciada do modelo; propagação de `uuid` para
  13 colunas em 7 tabelas futuras).
- **Paridade de documentação restaurada** no mesmo fluxo do ADR: `docs/arquitetura-sistema.json`
  (`database.naming_conventions.primary_keys`), `docs/design-sistema.md` (§5.2), e
  `docs/modelo-dados.dbml`/`docs/modelo-dados.md` (versão 4.0.0 → 4.1.0, `perfil.id` e as 13
  colunas dependentes viraram `uuid`).
- **Correção da migration V2 pelo desenvolvedor**: `ON DELETE SET NULL` na FK
  `fk_perfil_endereco`, renomeação da FK para o padrão `fk_{origem}_{destino}`, e correção do
  campo Java `perfilTipo`/coluna para `tipo` (elimina a referência à coluna inexistente no
  unique constraint e no índice).
- **Correção do import em `UsuarioRepository.java`**: `modulos.usuarios.dominio.Usuario` →
  `modulos.usuarios.models.Usuario`, refletindo o pacote real da entidade (histórico do
  relatório de 2026-08-31 também corrigido para o mesmo pacote).

## Decisões tomadas

| Decisão | Motivo | ADR |
|---|---|---|
| `Perfil.id` permanece `UUID DEFAULT gen_random_uuid()`, exceção ao ADR-0007 | Consistência de tipo de PK dentro do módulo `usuarios` (evitar misturar `Long`/`UUID` nas entidades da mesma pasta); não é argumento de custo de `JOIN` nem de exposição em URL — isso já era resolvido por `nome_usuario` | [ADR-0009](../adr/0009-perfil-id-uuid.md) |
| As 13 colunas de FK que apontam para `perfil.id` em tabelas ainda não criadas também usam `uuid` | Consistência de tipo entre PK e FK — evita que quem criar essas tabelas mais tarde use `bigint` por engano | [ADR-0009](../adr/0009-perfil-id-uuid.md) |

## Arquivos alterados

| Arquivo | Alteração |
|---|---|
| `backend/src/main/java/br/com/bicoemcasa/api/modulos/usuarios/models/Endereco.java` | Criado — entidade JPA `Endereco` |
| `backend/src/main/java/br/com/bicoemcasa/api/modulos/usuarios/models/Perfil.java` | Criado — entidade JPA `Perfil` (id `UUID`, FK para `Endereco`/`Usuario`) |
| `backend/src/main/java/br/com/bicoemcasa/api/modulos/usuarios/models/PerfilTipo.java` | Criado — enum `CLIENTE`/`PROFISSIONAL`/`ADMIN` |
| `backend/src/main/resources/db/migration/V2__criar-tabelas-endereco-perfil.sql` | Criado — migration das tabelas `endereco` e `perfil` |
| `backend/src/main/java/br/com/bicoemcasa/api/modulos/usuarios/repository/UsuarioRepository.java` | Alterado — corrigido import de `Usuario` para o pacote `models` |
| `docs/adr/0009-perfil-id-uuid.md` | Criado — ADR da exceção `perfil.id` em `uuid` |
| `docs/adr/README.md` | Alterado — índice com a linha do ADR-0009 |
| `docs/arquitetura-sistema.json` | Alterado — `primary_keys` reflete a exceção de `perfil` |
| `docs/design-sistema.md` | Alterado — §5.2, nova linha de chave primária para `perfil` |
| `docs/modelo-dados.dbml` | Alterado — `perfil.id` e as 13 colunas de FK dependentes viraram `uuid` |
| `docs/modelo-dados.md` | Alterado — versão 4.1.0, §3.1 com a linha de exceção, novo bloco `[!NOTE]` |
| `docs/relatorios/2026-08-31-inicio-modulo-usuarios-e-migration-v1.md` | Alterado — corrigido o pacote de `Usuario.java` (`dominio` → `models`) para refletir o estado real do código |

## Verificações executadas

Nenhum comando de build, teste ou lint foi executado nesta sessão — o trabalho foi de
diagnóstico, revisão e documentação, e as correções de código/migration foram feitas pelo próprio
desenvolvedor fora do que a LLM registrou em detalhe. Fica como pendência rodar
`./mvnw clean compile` e subir a migration antes da próxima sessão avançar sobre este módulo.

## Problemas encontrados

1. **`foreignKeyDefinition` do Hibernate não é interpretado por ferramentas de diff de schema
   externas** (o gerador de migration do IntelliJ, no caso). Só o próprio Hibernate lê essa
   string, e só quando ele mesmo gera o DDL em runtime. Qualquer cláusula além do que dá para
   inferir da metadata estrutural (`ON DELETE`, `ON UPDATE`, `DEFERRABLE`) precisa ser
   escrita/conferida à mão na migration gerada — não dá para confiar na geração automática para
   isso.
2. **Unique constraint referenciando coluna inexistente**: `Perfil.java` tinha
   `@UniqueConstraint(columnNames = {"usuario_id", "tipo"})` enquanto o campo mapeado se chamava
   `perfilTipo` com coluna física `perfil_tipo` — nem "tipo" existia. A migration gerada saiu com
   a constraint reduzida a uma coluna só (`UNIQUE (usuario_id)`), o que mudava a regra de negócio
   (um usuário só poderia ter um perfil, não um por tipo). Corrigido pelo desenvolvedor renomeando
   o campo para `tipo`, alinhando entidade, constraint, índice e coluna física.
3. **Achado durante a revisão final, ainda não corrigido**: a FK `perfil.usuario_id → usuario.id`
   saiu sem `ON DELETE CASCADE` na migration (`docs/modelo-dados.dbml` documenta
   `[delete: cascade]` para essa relação), e com nome fora do padrão
   (`FK_PERFIL_ON_USUARIO` em vez de `fk_perfil_usuario`) — mesma causa raiz do item 1. Reportado
   ao desenvolvedor, não corrigido nesta sessão.

## Pendências

- [ ] `FK_PERFIL_ON_USUARIO` (migration V2) não tem `ON DELETE CASCADE`, e o nome diverge do
      padrão `fk_{origem}_{destino}` — corrigir para `fk_perfil_usuario ... ON DELETE CASCADE`,
      conforme `docs/modelo-dados.dbml`.
- [ ] Índices `idx_perfis_tipo` e `idx_perfis_usuario_id` (migration V2 e `Perfil.java`) usam
      plural (`perfis`); `ADR-0008` e `modelo-dados.dbml` definem singular (`idx_perfil_*`).
- [ ] `uk_perfil_nome_usuario` e `uk_perfil_usuario_id_tipo` usam prefixo `uk_`; `ADR-0008` e
      `modelo-dados.dbml` definem `uq_`.
- [ ] Rodar `./mvnw clean compile` e subir a migration V2 no banco local — nenhuma verificação de
      build foi executada nesta sessão.
- [ ] `@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)` no campo `usuario` de
      `Perfil.java` não foi discutido nesta sessão, mas merece atenção numa próxima revisão:
      `CascadeType.ALL` num `@ManyToOne` propaga remoção do `Perfil` para o `Usuario` pai, o que
      provavelmente não é a intenção.

## Próximos passos

1. Corrigir os itens de convenção/FK listados em Pendências na migration V2 e em `Perfil.java`.
2. Rodar `./mvnw clean compile` e validar a migration V2 contra o PostgreSQL do `docker-compose`.
3. Seguir para o `UsuarioService`/DTOs de cadastro, conforme os próximos passos já registrados no
   relatório de 2026-08-31.
