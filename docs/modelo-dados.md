# Modelo de Dados — Bico em Casa

| Campo | Valor |
|---|---|
| **Versão** | 2.0.0 |
| **Data** | 2026-08-22 |
| **Engine** | PostgreSQL 18 |
| **Fonte** | [`modelo-dados.dbml`](./modelo-dados.dbml) — cole em [dbdiagram.io](https://dbdiagram.io) |
| **Convenções** | [`design-sistema.md`](./design-sistema.md) §5.2 |
| **Tabelas** | 18 |

---

> [!IMPORTANT]
> **As migrations do Flyway ainda não existem.** Este modelo está aprovado no papel; o DDL é o
> próximo passo. Enquanto não houver `V1__*.sql`, o `.dbml` é a referência.

---

## 1. Visão por módulo

As 18 tabelas se distribuem pelos seis módulos do
[ADR-0004](./adr/0004-estrutura-modular-por-dominio.md). Nenhuma tabela existe sem requisito que
a justifique, e nenhum requisito do MVP ficou sem tabela — verificado nos dois sentidos.

| Módulo | Tabelas | Requisitos atendidos |
|---|---|---|
| `autenticacao` | `tb_usuarios`, `tb_refresh_tokens`, `tb_tokens_recuperacao` | RF001, RNF001, RNF019, RNF020, RNF021 |
| `usuarios` | `tb_perfis`, `tb_enderecos`, `tb_log_acoes` | RF002, RF021, RF022, RNF014, RNF022 |
| `profissionais` | `tb_portfolios`, `tb_portfolio_fotos`, `tb_disponibilidades` | RF003, RF005 |
| `servicos` | `tb_servicos`, `tb_tags`, `tb_servico_tags` | RF004, RF010, RF011 |
| `contratacoes` | `tb_contratacoes`, `tb_contratacao_status_historico`, `tb_contratacao_anexos`, `tb_notificacoes` | RF008, RF012, RF014, RF015, RF016, RF017, RF018 |
| `avaliacoes` | `tb_avaliacoes`, `tb_denuncias` | RF006, RF007, RF019, RF020 |

---

## 2. A distinção que mais confunde: `tb_servicos` × `tb_contratacoes`

São entidades diferentes com nomes parecidos. Cardápio e pedido.

| | `tb_servicos` | `tb_contratacoes` |
|---|---|---|
| O que é | A **oferta** do catálogo | Um **trabalho** específico |
| Exemplo | "Instalação de chuveiro — R$ 120" | "João contratou Pedro em 20/08 por R$ 100" |
| Dono | O profissional | Cliente **e** profissional |
| Existe sem o outro? | Sim, mesmo sem ninguém contratar | Não, sempre tem as duas pontas |
| Tem status? | Só `ativo` | Sim: `SOLICITADA` → … → `CONCLUIDA` |
| Cardinalidade | Um serviço gera **N** contratações | |

**Por que não podem ser uma tabela só:**

1. **Duplicação** — título, descrição e categoria se repetiriam em cada contratação
2. **Histórico de preço** — hoje o serviço custa R$ 120, mas uma contratação de março precisa
   guardar os R$ 100 combinados naquele dia. Por isso `valor_final` é **congelado** na
   contratação e não lê `tb_servicos` depois
3. **Status sem sentido** — uma oferta de catálogo não tem "em andamento"
4. **Exclusão destrutiva** — apagar um serviço apagaria o histórico (`RF016`) e as avaliações

No rascunho do `dbdiagram`, a tabela `servico` tinha `cliente_id`, `profissional_id` e
`preco_previo`: **já era uma contratação com o nome errado**, e a oferta do catálogo não existia
em tabela nenhuma.

---

## 3. Decisões deste modelo

### 3.1 `tb_usuarios` × `tb_perfis` — e como um papel duplo funciona

`tb_usuarios` guarda **credencial e identidade**: e-mail, hash de senha, CPF, se está ativo.
`tb_perfis` guarda **como a pessoa aparece em cada papel**: nome de exibição, telefone, foto, bio.

A chave é a UNIQUE composta:

```sql
CONSTRAINT uq_perfis_usuario_tipo UNIQUE (usuario_id, tipo)
```

Um usuário tem **N perfis, um por papel**. O mesmo João pode ter um perfil `CLIENTE` e um
`PROFISSIONAL` na mesma conta — que é o `RF002`. A UNIQUE impede dois perfis do mesmo tipo.

No rascunho, `perfil.usuario_id` era UNIQUE simples, o que travava em um papel por conta: um
pintor não conseguia contratar um chaveiro sem criar outra conta.

### 3.2 Chaves primárias — UUID em todas

O rascunho misturava `bigint` em três tabelas e `uuid` em duas. A convenção do projeto (§5.2) é
`uuid DEFAULT gen_random_uuid()` em **todas**, e a regra de fronteira entre módulos (`ADR-0004`)
depende disso: módulo referencia módulo pelo UUID.

### 3.3 Dinheiro é `numeric(10,2)`

`preco_previo int` do rascunho não dizia se era reais ou centavos. `float` seria pior — não
representa `0.10` exatamente e erra em soma. `numeric` é exato, que é o requisito de dinheiro.

### 3.4 `tb_log_acoes` é somente de escrita

Sua ideia, e ela ancorou o `RNF022`. Uma escolha deliberada: **a tabela não tem
`atualizado_em`**. Registro de auditoria que pode ser alterado não é auditoria. A aplicação só
faz `INSERT`; nunca `UPDATE` nem `DELETE`.

### 3.5 `tb_denuncias` — o que mudou do que você descreveu

Você propôs *(nome, réu, autor, data de criação e atualização)*. Duas alterações:

- **`nome` virou `motivo` + `descricao`** — "nome" de uma denúncia não diz nada ao administrador.
  `motivo` é categórico e filtrável; `descricao` é o relato livre
- **Quatro colunas de análise entraram:** `status`, `analisado_por_perfil_id`, `analisado_em`,
  `parecer`. Sem elas a denúncia entra no sistema e nunca sai — e `RF020` diz explicitamente
  *"e que o administrador analise"*

`alvo_tipo` + `alvo_id` permitem denunciar serviço, perfil **ou** avaliação, como `RF020` pede.

### 3.6 Tabelas que você não listou e por que entraram

| Tabela | Por que é obrigatória |
|---|---|
| `tb_avaliacoes` | `RF006`, `RF007` e `RF019` a exigem. **Não existia no rascunho** |
| `tb_refresh_tokens` | `RNF019`. Sem revogação no banco, logout não existe de verdade |
| `tb_tokens_recuperacao` | `RNF021`. Consequência do ADR-0006 |
| `tb_enderecos` | `RF013` precisa de latitude e longitude; `varchar(100)` não calcula distância |
| `tb_disponibilidades` | `RF005` pede horários, não só um booleano |
| `tb_contratacao_status_historico` | `RF008` pede "a duração e qual etapa" — a duração exige o histórico de transições |
| `tb_contratacao_anexos` | `RF015` |
| `tb_notificacoes` | `RF012` |
| `tb_portfolio_fotos` | Era `fotos_galeria jsonb`. Virou tabela: jsonb impede FK, impede ordenar e obriga a ler o documento inteiro para uma foto |
| `tb_servico_tags` | `RF004`. Com uma tag só por serviço, "reforma de banheiro" some do filtro de hidráulica **ou** do de alvenaria |

---

## 4. Erros do rascunho, corrigidos

| Onde | Era | Virou |
|---|---|---|
| `usuario.hash_senha` | Contradizia o ADR-0002 (Supabase custodiava a senha) | Correto agora — o [ADR-0006](./adr/0006-remover-supabase-infraestrutura-propria.md) trouxe a credencial para casa, com **Argon2id** |
| `tag.nome` | `check: nome.lenght > 0` — typo e SQL inválido | `CHECK (length(trim(nome)) > 0)` |
| Nomes de tabela | `usuario`, `perfil`, `servico` | `tb_usuarios`, `tb_perfis`, `tb_servicos` — §5.2 exige prefixo e plural |
| PKs | `bigint` e `uuid` misturados | `uuid` em todas |
| `perfil.email` | Duplicava `usuario.email` | Removido. Dado duplicado diverge |
| `servico` | Misturava oferta e contratação, sem `status` | Separado em `tb_servicos` e `tb_contratacoes` |
| Preço | `int` sem unidade | `numeric(10,2)` |
| Avaliação | Não existia | `tb_avaliacoes`, com `CHECK (nota BETWEEN 1 AND 5)` |

---

## 5. FK no banco × relacionamento JPA — não confunda

Isto costuma gerar dúvida, então fica explícito:

> **As FKs do banco cruzam módulo. Os relacionamentos JPA, não.**

O `design-sistema.md` proíbe **`@ManyToOne` cruzando módulo** — é regra de *código Java*, para
que o acoplamento acidental não tenha por onde entrar.

No **banco**, a FK entre `tb_contratacoes.cliente_perfil_id` e `tb_perfis.id` **deve existir**: é
ela que garante integridade referencial. Sem ela, uma contratação órfã é só uma linha com um UUID
que não aponta para lugar nenhum.

Na prática, dentro do módulo `contratacoes`:

```java
// ✅ correto — referência por UUID, FK existe no banco
@Column(name = "cliente_perfil_id", nullable = false)
private UUID clientePerfilId;

// ❌ proibido — relacionamento JPA cruzando a fronteira do módulo
@ManyToOne
@JoinColumn(name = "cliente_perfil_id")
private Perfil cliente;
```

Para ler os dados do perfil, o módulo `contratacoes` chama a interface publicada em
`usuarios/contrato/`. Nunca o repository alheio.

---

## 6. Política de exclusão

| Regra | Onde | Por quê |
|---|---|---|
| `CASCADE` | Tokens, fotos de portfólio, anexos, histórico de status | São dependentes puros. Sem o pai, não têm sentido |
| `RESTRICT` | Perfis referenciados por contratação e avaliação | Apagar um perfil apagaria o histórico da outra parte. `RNF015` resolve por **anonimização**, não por exclusão |
| `SET NULL` | `servico_id` na contratação, `analisado_por` na denúncia | O serviço pode sair do catálogo sem levar junto o trabalho já feito |

O `RESTRICT` é o que faz `RNF015` (LGPD) funcionar: exclusão de conta **anonimiza**
`tb_usuarios` e preserva contratações e avaliações, porque o histórico da contraparte também é
dado dela.

---

## 7. Pendências

- [ ] **Migrations do Flyway.** Este modelo ainda não virou `V1__*.sql`
- [ ] **`docker-compose.yml`** com PostgreSQL, MinIO e SMTP de desenvolvimento
- [ ] **ADR-0007 — geocodificação.** `tb_enderecos` já tem `latitude`/`longitude`, mas nada as
      preenche. `RF013` fica descoberto até esse ADR existir
- [ ] **Índice geoespacial.** Se `RF013` filtrar por raio, o índice B-tree em `(latitude,
      longitude)` não serve — vai precisar de PostGIS ou de `earthdistance`. Decidir junto do ADR-0007
- [ ] **`unidade_preco` está como `varchar`** com valores fixos. Vira `enum` se a lista estabilizar

---

## Referências

- [`modelo-dados.dbml`](./modelo-dados.dbml) — o modelo em formato executável
- [`design-sistema.md`](./design-sistema.md) §5 — engine, migrations e convenções de nomenclatura
- [`requisitos.md`](./requisitos.md) — os requisitos que cada tabela atende
- [`matriz-rastreabilidade.md`](./matriz-rastreabilidade.md) — requisito → módulo → tabela
- [ADR-0006](./adr/0006-remover-supabase-infraestrutura-propria.md) — por que a credencial mora aqui
