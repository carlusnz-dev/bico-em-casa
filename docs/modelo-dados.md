# Modelo de Dados — Bico em Casa

| Campo | Valor |
|---|---|
| **Versão** | 4.1.0 |
| **Data** | 2026-09-01 |
| **Engine** | PostgreSQL 18 |
| **Fonte** | [`modelo-dados.dbml`](./modelo-dados.dbml) — cole em [dbdiagram.io](https://dbdiagram.io) |
| **Convenções** | [`design-sistema.md`](./design-sistema.md) §5.2 |
| **Tabelas** | 18 · 28 relacionamentos · 4 enums |

---

> [!IMPORTANT]
> **As migrations do Flyway ainda não existem.** Este modelo está aprovado no papel; o DDL é o
> próximo passo. Enquanto não houver `V1__*.sql`, o `.dbml` é a referência.

> [!NOTE]
> A **4.0.0** consolida a revisão que o autor fez no dbdiagram em 2026-08-22. De lá vieram a
> chave primária mista ([ADR-0007](./adr/0007-chave-primaria-mista.md)), a nomenclatura singular
> sem prefixo ([ADR-0008](./adr/0008-nomenclatura-de-tabelas.md)), `ultimo_login`, `nome_usuario`,
> `slug_url`, `denuncia.contratacao_id`, `refresh_token.substituido_por` e `denuncia.fotos`.
> O arquivo de rascunho foi removido do versionamento: agora ele **é** a fonte da verdade, e
> manter uma segunda cópia divergente só criaria dúvida sobre qual das duas vale.

> [!NOTE]
> A **4.1.0** registra a exceção do [ADR-0009](./adr/0009-perfil-id-uuid.md): `perfil.id` passa
> de `bigint` para `uuid`, por preferência de consistência de tipo dentro do módulo `usuarios` —
> não por decisão relacionada a `JOIN` ou a exposição em URL, que o ADR-0007 já resolvia com
> `nome_usuario`. As 13 colunas de outras tabelas que referenciam `perfil.id` por FK
> acompanham a mudança.

---

## 1. Visão por módulo

| Módulo | Tabelas | Requisitos atendidos |
|---|---|---|
| `autenticacao` | `usuario`, `refresh_token`, `token_recuperacao` | RF001, RNF001, RNF019, RNF020, RNF021 |
| `usuarios` | `perfil`, `endereco`, `log_acao` | RF002, RF021, RF022, RNF014, RNF022 |
| `profissionais` | `portfolio`, `portfolio_foto`, `disponibilidade` | RF003, RF005 |
| `servicos` | `servico`, `tag`, `servico_tag` | RF004, RF010, RF011 |
| `contratacoes` | `contratacao`, `contratacao_historico`, `contratacao_anexo`, `notificacao` | RF008, RF012, RF014, RF015, RF016, RF017, RF018 |
| `avaliacoes` | `avaliacao`, `denuncia` | RF006, RF007, RF019, RF020 |

Nenhuma tabela existe sem requisito que a justifique, e nenhum requisito do MVP ficou sem tabela.

---

## 2. Índice único × índice comum

Os dois são a mesma estrutura de dados — uma B-tree. A diferença não é de desempenho, é de
**autoridade**: o índice comum é uma sugestão ao planejador; o único é uma **regra do banco**.

| | `UNIQUE` | comum |
|---|---|---|
| Acelera busca | Sim | Sim |
| Impede duplicata | **Sim** | Não |
| Onde a regra vive | No banco, para sempre | Em lugar nenhum |
| Efeito no `INSERT` | Pode falhar (`23505`) | Nunca falha por causa dele |
| `NULL` | Vários `NULL` convivem¹ | — |

¹ No PostgreSQL, `NULL` nunca é igual a `NULL`. Um `UNIQUE` numa coluna nulável aceita **infinitas**
linhas com `NULL`. É por isso que `cpf` pode ser único e opcional ao mesmo tempo.

**Por que isso importa mais do que parece.** Sem o `UNIQUE`, a unicidade vira responsabilidade
do código, e o código roda concorrente:

```
requisição A          requisição B
SELECT ... WHERE email = 'joao@x.com'   → 0 linhas
                      SELECT ... WHERE email = 'joao@x.com'   → 0 linhas
INSERT joao@x.com     ✅
                      INSERT joao@x.com     ✅   ← duas contas, mesmo e-mail
```

As duas requisições checaram antes de inserir, as duas viram "não existe", as duas inseriram.
`SELECT` seguido de `INSERT` **não é atômico**. Só o `UNIQUE` fecha essa janela, porque a
verificação acontece dentro da mesma operação que grava.

**A regra prática:** se a duplicata for um *bug de negócio*, o índice é `UNIQUE`. Se for apenas
uma consulta lenta, é comum.

Neste modelo:

| Índice | Tipo | O que a duplicata causaria |
|---|---|---|
| `uq_usuario_email` | único | Duas contas com o mesmo login |
| `uq_perfil_usuario_tipo` | único **composto** | Dois perfis `PROFISSIONAL` para a mesma pessoa |
| `uq_perfil_nome_usuario` | único | Duas pessoas disputando `/p/joao-encanador` |
| `uq_portfolio_slug_url` | único | Duas páginas no mesmo endereço público |
| `uq_avaliacao_contratacao_id` | único | Cliente irritado avalia dez vezes e afunda a média do `RF019` |
| `idx_contratacao_cliente` | comum | Nada. Um cliente **deve** ter várias contratações |
| `idx_disponibilidade_perfil_dia` | comum | Nada. Um profissional atende em vários dias |
| `idx_notificacao_destinatario_lida` | comum | Nada |

### 2.1 Único **composto** não é o mesmo que dois únicos

Este é o ponto que mais confunde:

```sql
UNIQUE (usuario_id, tipo)   -- ✅ o par não se repete
UNIQUE (usuario_id), UNIQUE (tipo)  -- ❌ um usuário só, e um CLIENTE no sistema inteiro
```

O composto restringe a **combinação**. É o que faz o `RF002` funcionar: o mesmo João pode ser
`CLIENTE` e `PROFISSIONAL`, mas não pode ser `PROFISSIONAL` duas vezes.

**A ordem das colunas importa para leitura.** Uma B-tree em `(a, b)` serve para `WHERE a = ?` e
para `WHERE a = ? AND b = ?`, mas **não** para `WHERE b = ?` sozinho — é a mesma razão pela qual
uma lista telefônica ordenada por sobrenome não ajuda a achar alguém pelo primeiro nome. Por isso
`idx_servico_tag_tag_id` existe: a PK composta `(servico_id, tag_id)` já cobre "quais tags tem
este serviço", mas não cobre "quais serviços têm esta tag" — que é justamente a busca do `RF004`.

---

## 3. As três decisões estruturais desta versão

### 3.1 Chave primária mista — `bigint` e `uuid` na mesma base

Registrado no [ADR-0007](./adr/0007-chave-primaria-mista.md), com uma exceção pontual no
[ADR-0009](./adr/0009-perfil-id-uuid.md).

| Tipo | Tabelas | Critério |
|---|---|---|
| `bigint` identity | `usuario`, `endereco`, `portfolio` | Cadastro: cresce devagar, é o alvo da maioria dos `JOIN` |
| `uuid` — exceção ao critério de cadastro ([ADR-0009](./adr/0009-perfil-id-uuid.md)) | `perfil` | Preferência de consistência de tipo dentro do módulo `usuarios`, não custo de `JOIN` |
| `uuid` | as outras 14 | Transacional: nasce por evento, cresce rápido, aparece em URL |

**Por que a mistura se sustenta.** `bigint` ocupa 8 bytes e é sequencial, então cada nível da
B-tree cabe mais denso e a inserção sempre acontece na ponta direita da árvore. `uuid` ocupa 16
bytes e é aleatório: cada `INSERT` cai numa página diferente, o que espalha a escrita. Em
`contratacao`, que aparece em URL e nasce a cada solicitação, o `uuid` vale mais — id sequencial
em URL pública permite varrer a base contando de 1 em 1. `perfil` é referenciado por FK em 7
tabelas — pelo critério original do ADR-0007, era candidato natural a `bigint` — mas o
ADR-0009 abre mão dessa densidade de índice em nome de um só tipo de PK no módulo `usuarios`.

**O preço, que é real e você paga em dois lugares:**

1. **Nenhuma coluna polimórfica pode ter FK.** `log_acao.alvo_id` e
   `notificacao.alvo_id` apontam ora para um `bigint`, ora para um `uuid`. Como uma coluna
   tem um tipo só, eles são `varchar(64)` guardando o id como texto. O banco **não valida** esses
   dois campos — a integridade deles depende do código.
2. **Toda leitura de `alvo_id` exige converter.** `WHERE alvo_id = '42'`, com aspas, sempre. Um
   `WHERE alvo_id = 42` compara texto com inteiro e o PostgreSQL recusa.

Em `log_acao` isso é aceitável de qualquer forma, porque **a auditoria não deveria ter FK**:
uma FK com `CASCADE` apagaria a prova junto com o crime, e com `RESTRICT` impediria apagar
qualquer linha já auditada. Em `notificacao` é o custo direto da mistura.

### 3.2 `NOT NULL` descreve o momento do `INSERT`, não a regra de negócio

O rascunho tinha `cancelamento_perfil_id bigint [not null]` em `contratacao`. A regra por
trás é correta — *toda contratação cancelada tem alguém que cancelou* — mas o `NOT NULL` diz
outra coisa: *toda contratação, desde o instante em que nasce, tem alguém que cancelou*. Como
nenhuma contratação nasce cancelada, **nenhum `INSERT` passaria**.

A regra é condicional, e o `NOT NULL` não sabe expressar condição. Quem sabe é o `CHECK`:

```sql
ALTER TABLE contratacao ADD CONSTRAINT ck_contratacao_cancelamento_coerente
  CHECK (status <> 'CANCELADA' OR cancelado_por_perfil_id IS NOT NULL);
```

Agora o banco aceita a contratação nova **e** recusa marcá-la como cancelada sem dizer por quem.

A leitura geral: **`NOT NULL` é para o que já é verdade quando a linha nasce.** Tudo que depende
de um evento futuro — `concluida_em`, `valor_orcado`, `analisado_em`, `lida_em` — é nulável, e a
coerência é `CHECK`.

### 3.3 `UNIQUE` numa FK muda a cardinalidade do relacionamento

`disponibilidade.perfil_id [unique]` no rascunho parecia inofensivo. Ele transforma 1:N em 1:1 —
e um índice `(perfil_id, dias_semana)` na mesma tabela vira uma contradição, porque só pode
existir uma linha por perfil.

O efeito prático: cada profissional podia declarar **um** dia da semana e **uma** faixa de
horário, para sempre. O `RF005` fala em "os horários em que ele atende", plural. Sem o `UNIQUE`,
"segunda a sexta 8h–12h e 14h–18h, sábado 8h–12h" são 11 linhas, e o modelo aceita.

A mesma pergunta vale para toda FK: **um pai tem um filho ou vários?** Onde a resposta é um —
`portfolio.perfil_id`, `avaliacao.contratacao_id` — o `UNIQUE` fica. Onde é vários, sai.

---

## 4. `servico` × `contratacao` — cardápio e pedido

São entidades diferentes com nomes parecidos.

| | `servico` | `contratacao` |
|---|---|---|
| O que é | A **oferta** do catálogo | Um **trabalho** específico |
| Exemplo | "Instalação de chuveiro — R$ 120" | "João contratou Pedro em 20/08 por R$ 100" |
| Dono | O profissional | Cliente **e** profissional |
| Existe sem o outro? | Sim, mesmo sem ninguém contratar | Não, sempre tem as duas pontas |
| Tem status? | Só `ativo` | Sim: `SOLICITADA` → … → `CONCLUIDA` |

**Por que não podem ser uma tabela só:**

1. **Duplicação** — título, descrição e tags se repetiriam em cada contratação
2. **Histórico de preço** — hoje o serviço custa R$ 120, mas uma contratação de março precisa
   guardar os R$ 100 combinados naquele dia. Por isso `valor_final` é **congelado** na
   contratação e não lê `servico` depois
3. **Status sem sentido** — uma oferta de catálogo não tem "em andamento"
4. **Exclusão destrutiva** — apagar um serviço apagaria o histórico (`RF016`) e as avaliações

---

## 5. Outras escolhas, em uma linha cada

| Escolha | Por quê |
|---|---|
| `preco_previo numeric(10,2)`, nunca `int` | `int` não representa R$ 120,50, e nada no schema diz se `120` é real ou centavo |
| `ip_origem inet` | Não existe tipo `inet6` no PostgreSQL. `inet` já cobre IPv4 e IPv6 |
| Rótulos de enum sempre em MAIÚSCULA | Rótulo é string sensível a caixa: `'ACEITA'` e `'aceita'` são valores diferentes |
| `RECUSADA` separada de `CANCELADA` | `RF017` recusa antes do aceite; `RF018` cancela depois. `RF012` precisa distinguir para notificar |
| `perfil` sem coluna `email` | Duplicar o e-mail obriga a decidir, em cada consulta, qual dos dois está certo quando divergirem |
| `denunciado_perfil_id`, não `culpado_id` | Quem decide culpa é a análise do admin, que acontece depois. Nome de coluna não antecipa veredito |
| `denuncia.contratacao_id` **não** é único | Cliente e profissional podem se denunciar pelo mesmo trabalho; as duas denúncias são legítimas |
| `denuncia.fotos` fica `jsonb` | Evidência é escrita uma vez, nunca reordenada, nunca consultada foto a foto — ao contrário da galeria do portfólio |
| `portfolio_foto` é tabela, não `jsonb` | A galeria **é** reordenável e consultada foto a foto; com `jsonb`, mudar a ordem reescreve o documento inteiro |
| `log_acao` sem `atualizado_em` | Registro de auditoria que pode ser alterado não é auditoria (`RNF022`) |
| `familia_id` **e** `substituido_por` em refresh token | `substituido_por` dá a corrente e serve para auditar; `familia_id` revoga o conjunto todo em um `UPDATE`, sem recursão (`RNF019`) |
| `length(trim(nome))`, não `lenght(...)` | A função do PostgreSQL é `length`. O typo derruba a migration |

---

## 6. FK no banco × relacionamento JPA — não confunda

> **As FKs do banco cruzam módulo. Os relacionamentos JPA, não.**

O `design-sistema.md` proíbe **`@ManyToOne` cruzando módulo** — é regra de *código Java*, para
que o acoplamento acidental não tenha por onde entrar.

No **banco**, a FK entre `contratacao.cliente_perfil_id` e `perfil.id` **deve existir**: é
ela que garante integridade referencial.

```java
// ✅ correto — referência por id, FK existe no banco
@Column(name = "cliente_perfil_id", nullable = false)
private Long clientePerfilId;

// ❌ proibido — relacionamento JPA cruzando a fronteira do módulo
@ManyToOne
@JoinColumn(name = "cliente_perfil_id")
private Perfil cliente;
```

Para ler os dados do perfil, o módulo `contratacoes` chama a interface publicada em
`usuarios/contrato/`. Nunca o repository alheio.

---

## 7. Política de exclusão

| Regra | Onde | Por quê |
|---|---|---|
| `CASCADE` | Tokens, fotos de portfólio, anexos, histórico de status, disponibilidades | Dependentes puros. Sem o pai, não têm sentido |
| `RESTRICT` | Perfis referenciados por contratação, avaliação e denúncia | Apagar um perfil apagaria o histórico da outra parte. `RNF015` resolve por **anonimização**, não por exclusão |
| `SET NULL` | `servico_id` na contratação, `analisado_por` na denúncia, `endereco_id` no perfil | O serviço pode sair do catálogo sem levar junto o trabalho já feito |
| **sem FK** | `log_acao.alvo_id`, `notificacao.alvo_id` | Coluna polimórfica (ADR-0007). No log é desejável: a auditoria sobrevive ao alvo |

O `RESTRICT` é o que faz `RNF015` (LGPD) funcionar: exclusão de conta **anonimiza**
`usuario` e preserva contratações e avaliações, porque o histórico da contraparte também é
dado dela.

---

## 8. Pendências

- [ ] **Migrations do Flyway.** Este modelo ainda não virou `V1__*.sql`
- [ ] **`docker-compose.yml`** com PostgreSQL, MinIO e SMTP de desenvolvimento
- [ ] **ADR-0009 — geocodificação.** `endereco` já tem `latitude`/`longitude`, mas nada as
      preenche. `RF013` fica descoberto até esse ADR existir
- [ ] **Índice geoespacial.** Se `RF013` filtrar por raio, o B-tree em `(latitude, longitude)`
      não serve — vai precisar de PostGIS ou de `earthdistance`. Decidir junto do ADR-0009
- [ ] **`unidade_preco` está como `varchar`** com valores fixos. Vira `enum` se a lista estabilizar
- [ ] **`alvo_id` como `varchar(64)` não é validado pelo banco.** Em `notificacao` isso é
      dívida assumida do ADR-0007 — o service precisa de teste que garanta o par
      (`alvo_tipo`, `alvo_id`)

---

## Referências

- [`modelo-dados.dbml`](./modelo-dados.dbml) — o modelo em formato executável
- [ADR-0008](./adr/0008-nomenclatura-de-tabelas.md) — por que as tabelas são singulares e sem prefixo
- [`design-sistema.md`](./design-sistema.md) §5 — engine, migrations e convenções
- [`requisitos.md`](./requisitos.md) · [`matriz-rastreabilidade.md`](./matriz-rastreabilidade.md)
- [ADR-0006](./adr/0006-remover-supabase-infraestrutura-propria.md) — por que a credencial mora aqui
- [ADR-0007](./adr/0007-chave-primaria-mista.md) — por que há `bigint` e `uuid` na mesma base
