# Relatório de Sessão — 2026-08-22

| Campo | Valor |
|---|---|
| **Sessão** | Consolidação do modelo de dados (v4), custeio do MVP e a regra de quem decide |
| **Autor** | Carlos Antunes |
| **Data** | `2026-08-22` |
| **Duração aproximada** | `~2h30` (estimada, não cronometrada) |
| **LLM utilizada** | `Claude Opus 5 (Claude Code)` |
| **Branch** | `docs/requisitos-e-matriz-rastreabilidade` |
| **Commits** | `139db6e`, `be096be`, `129e988`, `45c513d`, `eb2f12e` |
| **Plano relacionado** | [`2026-08-22-custeio-e-backlog-mvp.md`](../planos/2026-08-22-custeio-e-backlog-mvp.md) — criado nesta sessão |

---

## Resumo

A sessão nasceu de uma revisão de banco: o autor refez o modelo no dbdiagram por conta própria e
pediu análise, explicação de índice único × comum, custeio do MVP e a criação de cards no Trello.
Terminou com o modelo em **v4.0.0**, dois ADRs novos, um plano de custeio e uma regra de
governança no `CLAUDE.md` — e com a parte do Trello **não entregue**, por falta de acesso.

O modelo passou por duas rodadas. Na primeira, a revisão do autor foi incorporada ao modelo
canônico: entraram `ultimo_login`, `nome_usuario`, `slug_url`, `denuncia.contratacao_id` e
`refresh_token.substituido_por`, e a chave primária mista (`bigint` no cadastro, `uuid` no
transacional) virou o ADR-0007. Na segunda rodada o autor pediu a troca da convenção de nomes —
`tb_` + plural saiu, `snake_case` singular entrou — o que virou o ADR-0008 e propagou por 11
arquivos. O rascunho `modelo-dados-rascunho-2026-08-22.dbml` saiu do versionamento.

Três defeitos do rascunho eram bloqueantes e não sobreviveram: `NOT NULL` em
`cancelamento_perfil_id` (nenhum `INSERT` passaria), `UNIQUE` em `disponibilidade.perfil_id`
(um dia da semana por profissional, contra o `RF005`) e `preco_previo int` (não representa
R$ 120,50). Somaram-se `inet6` — tipo que não existe no PostgreSQL — e `lenght()` em vez de
`length()`.

O episódio mais instrutivo foi de processo, não de schema. No meio da segunda rodada o arquivo
`modelo-dados.dbml` foi sobrescrito no disco com o rascunho bruto. O pedido escrito era
"comentários demais e convenção de nomes diferente", mas o arquivo também revertia as duas
decisões que o autor havia tomado minutos antes — reintroduzir `endereco` e usar `varchar(64)`
em `alvo_id`. A escrita parou e a pergunta foi feita antes do commit. Isso aconteceu no mesmo dia
em que a **regra nº 2** foi escrita no `CLAUDE.md`, e foi o primeiro teste dela.

## O que foi feito

- **O modelo de dados chegou à v4.0.0**: 18 tabelas, 28 relacionamentos, 4 enums, todos usados,
  nenhuma FK sem `Ref` declarada
- **A chave primária virou mista e documentada** — `bigint` identity em `usuario`, `perfil`,
  `endereco` e `portfolio`; `uuid` nas outras 14. Colunas de alvo polimórfico
  (`log_acao.alvo_id`, `notificacao.alvo_id`, `denuncia.alvo_id`) passaram a `varchar(64)` sem FK
- **A nomenclatura passou a ser `snake_case` singular sem prefixo**, propagada por 11 arquivos
  vivos (~170 linhas), com os nomes de constraint acompanhando o nome da tabela
  (`uq_usuario_email`, não `uq_usuarios_email`)
- **`endereco` voltou ao modelo** — sem coordenada persistida o `RF013` não tem como existir
- **`token_recuperacao`, `tentativas_login`/`bloqueado_ate` e `familia_id` foram preservados** —
  são o que dá tabela a `RNF019`, `RNF020` e `RNF021`
- **`denuncia` ganhou `status` e o par `alvo_tipo`/`alvo_id`** — sem eles os enums
  `status_denuncia` e `tipo_alvo_denuncia` ficavam declarados e nunca usados, e o `RF020` não
  tinha fila de análise
- **`modelo-dados.md` §2 passou a explicar índice único × comum**, com a janela de corrida
  `SELECT`→`INSERT` e a diferença entre único composto e dois únicos
- **O MVP foi custeado**: 404 h em 7 fases e ~R$ 55–95/mês de infraestrutura, com o backlog
  quebrado em cards por épico
- **A semana de 24 a 30/08 foi planejada para a equipe de 6**, em seis trilhas paralelas, com a
  dependência `migrations → Testcontainers` explícita
- **`CLAUDE.md` ganhou a regra nº 2** — a LLM propõe, o desenvolvedor decide — e o checklist de
  cinco perguntas, deliberadamente fora do workflow
- **Resíduos de Supabase saíram do `CLAUDE.md`** (stack, `lib/supabase/`, `supabase-js`,
  `SUPABASE_SERVICE_ROLE_KEY`), que ainda contradiziam o ADR-0006

## Decisões tomadas

| Decisão | Motivo | ADR |
|---|---|---|
| Chave primária mista: `bigint` no cadastro, `uuid` no transacional | Densidade de índice nas tabelas mais referenciadas, id opaco onde adivinhar tem consequência de acesso | [ADR-0007](../adr/0007-chave-primaria-mista.md) |
| `alvo_id` polimórfico como `varchar(64)` sem FK | Consequência direta da PK mista: uma coluna tem um tipo só. Escolha do autor entre as três apresentadas | [ADR-0007](../adr/0007-chave-primaria-mista.md) |
| Tabelas em `snake_case` singular, sem prefixo `tb_` | O prefixo resolve um problema que o PostgreSQL não tem; o singular casa com o domínio falado e com a entidade JPA | [ADR-0008](../adr/0008-nomenclatura-de-tabelas.md) |
| Normalizar `servicos_tags` → `servico_tag`, `contratacao_anexos` → `contratacao_anexo`, `log_acoes` → `log_acao` | O rascunho misturava singular e plural; a convenção precisa ser derivável | [ADR-0008](../adr/0008-nomenclatura-de-tabelas.md) |
| Reintroduzir `endereco` com `latitude`/`longitude` | `distancia_km` a partir de `varchar` não existe. Escolha do autor entre manter o `RF013` ou tirá-lo do MVP | Não requer — o modelo já era o do ADR-0006 |
| Remover o rascunho do versionamento | O canônico incorporou tudo que vinha dele; duas cópias divergentes criam dúvida sobre qual vale | Não requer |
| Regra nº 2 no `CLAUDE.md` + checklist fora do workflow | Um check que a máquina responde sozinha deixa de ser lido | Não requer — governança, não arquitetura |
| Manter `denuncia.fotos` como `jsonb` | Evidência é escrita uma vez, nunca reordenada, nunca consultada foto a foto — ao contrário da galeria do portfólio | Não requer — documentado em `modelo-dados.md` §5 |

## Arquivos alterados

| Arquivo | Alteração |
|---|---|
| `docs/modelo-dados.dbml` | **Reescrito duas vezes** — v3.0.0 e depois v4.0.0 com a nomenclatura nova e os `Note` narrativos removidos |
| `docs/modelo-dados.md` | **Reescrito** — ganhou a §2 de índice único × comum e a §3 das três decisões estruturais |
| `docs/modelo-dados-rascunho-2026-08-22.dbml` | **Removido** (`git rm`) — 325 linhas |
| `docs/adr/0007-chave-primaria-mista.md` | **Criado** — 112 linhas |
| `docs/adr/0008-nomenclatura-de-tabelas.md` | **Criado** — 107 linhas |
| `docs/adr/README.md` | Alterado — índice com as duas entradas novas |
| `docs/planos/2026-08-22-custeio-e-backlog-mvp.md` | **Criado** — 360 linhas: custeio, backlog por épico e a semana 1 |
| `docs/arquitetura-sistema.json` | Alterado — `naming_conventions.tables`, `primary_keys`, `polymorphic_references` (novo), `claim_mapping`, `regras_de_acoplamento` |
| `docs/design-sistema.md` | Alterado — §5.2 e §4 espelhando o JSON, no mesmo commit |
| `docs/requisitos.json` | Alterado — 128 linhas: campo `entidades` renomeado nos 46 requisitos |
| `docs/requisitos.md` | Alterado — nomes de tabela e o número do ADR de geocodificação |
| `docs/matriz-rastreabilidade.md` | Alterado — 70 linhas de nomes de entidade |
| `docs/README.md` | Alterado — linha do rascunho removida do mapa de artefatos |
| `docs/planos/TEMPLATE.md` | Alterado — exemplo citava `tb_profissionais` |
| `CLAUDE.md` | Alterado — regra nº 2, checklist e limpeza dos resíduos de Supabase |
| `.claude/agents/revisor-codigo.md` | Alterado — uma citação de nome de tabela |

## Verificações executadas

| Comando / teste | Resultado |
|---|---|
| Script Python de integridade do DBML (v3.0.0) | ✅ 18 tabelas, 28 refs, 4/4 enums usados, nenhuma FK sem `Ref` |
| Script Python de integridade do DBML (v4.0.0, após renomear) | ✅ mesmos números, nenhum nome com aparência de plural |
| Script Python de integridade do DBML (v4.0.0 restaurada, final) | ✅ 18 tabelas, 28 refs, 4 enums, problemas: nenhum |
| `jq empty` em `arquitetura-sistema.json` e `requisitos.json` | ✅ os dois válidos |
| Cobertura entidades de `requisitos.json` (MVP) × tabelas do DBML, nos dois sentidos | ✅ nenhuma entidade sem tabela, nenhuma tabela sem requisito |
| `grep -rn "tb_"` fora de relatórios e ADR históricos | ✅ nenhum resíduo |
| `grep -rEn "(uq\|idx\|ck\|fk)_<radical plural>"` | ✅ nenhum, após a segunda passada |
| Checagem de links relativos `.md` em `docs/` e `CLAUDE.md` | ✅ único achado é o placeholder `NNNN-titulo.md` do `relatorios/TEMPLATE.md` |
| `date -d 2026-08-22` para datar a semana 1 | ✅ sábado; a semana planejada é 24 a 30/08 |
| `trelloReadBoard` (`list`, `get`, `search_boards`, `search_cards`) | ❌ `could not verify Trello workspace permissions` — nenhum card criado |
| `/mcp` reconnect do Trello, 3 tentativas | ❌ `CONNECTION_CLOSED` nas três |
| Renderização do DBML no dbdiagram.io | ⏭️ não executado — validado por script, não colado na ferramenta |
| Migrations do Flyway | ⏭️ não executado — não existem |
| Qualquer teste automatizado | ⏭️ não executado — não há código de aplicação |

## Problemas encontrados

**O arquivo canônico foi sobrescrito no disco no meio da tarefa.** `modelo-dados.dbml` voltou a
ser o rascunho bruto. O pedido escrito citava só comentários e nomes, mas o conteúdo revertia
`endereco`, `alvo_id varchar` e o `NOT NULL` que impede qualquer `INSERT` em `contratacao`. A
opção foi **parar antes do commit e perguntar**, listando o delta item a item, em vez de resolver
por conta própria em qualquer das duas direções.

> Lição: quando o pedido em texto e o estado do arquivo discordam, os dois são dados. Escolher
> silenciosamente um dos dois é decidir pelo outro sem avisar.

**O `\b` do regex não casa antes de `_`, e a primeira renomeação passou em branco nos nomes de
constraint.** O padrão `\b(uq_)usuarios\b` não casa em `uq_usuarios_email`, porque `s_` é
transição entre dois caracteres de palavra. As 151 linhas da primeira passada trocaram nomes de
tabela e **nenhum** nome de constraint. Só apareceu porque um `grep` de resíduo rodou depois; a
correção usou `(?=_|\b)` como lookahead.

> Isto é a mesma classe de erro do `index(.)` da sessão anterior: uma transformação que reporta
> sucesso sem ter feito o trabalho. Verificação independente depois da transformação não é zelo,
> é o que separa "rodou" de "funcionou".

**O ADR de geocodificação foi renumerado duas vezes na mesma sessão** — era `0007`, virou `0008`,
terminou `0009`, porque duas decisões estruturais reais chegaram antes dele. Referências em
`requisitos.md`, `modelo-dados.md` e no plano foram acertadas, mas os relatórios anteriores ainda
o chamam de `ADR-0007`. Números reservados para ADR que ainda não existe são frágeis.

**O ADR-0007 foi editado depois de marcado como Aceito.** O `adr/README.md` diz que ADR aceito
nunca é reescrito. A alteração foi mecânica — nomes de tabela e a versão do modelo nas
referências, sem tocar na decisão — e aconteceu na mesma sessão em que ele nasceu, numa branch
não mesclada. Ainda assim é um desvio da própria regra, registrado aqui de propósito.

**O Trello não foi acessível em nenhum momento.** O conector autentica como `carlosantunesdev` e
enxerga apenas o workspace `workspacedocabeto` (3 quadros). O quadro *Experiência Criativa*
(`2j4Iduux`) está em workspace onde o autor é convidado: o primeiro erro foi
`could not be found, or you do not have access`, e depois de fornecida a URL passou a
`could not verify Trello workspace permissions` — ou seja, o recurso resolve e a autorização não
cobre. As três tentativas de `/mcp` reconnect falharam com `CONNECTION_CLOSED`. **Nenhum card foi
criado.** O conteúdo dos cards e a semana 1 ficaram registrados no plano.

## Pendências

- [ ] **Nenhum card foi criado no Trello.** Depende de reautorizar o conector incluindo o
      workspace do quadro, ou de transcrever manualmente o §3 e o §4 do plano de custeio
- [ ] **Backlog do Trello está com 0 cards.** As 404 h não têm onde morar; o quadro hoje mostra o
      que está em curso, não o que falta
- [ ] **"Montar estrutura do banco" continua em *Em andamento*** — o modelo terminou, o que resta
      é a migration, que é card com outro critério de aceite
- [ ] **Três lugares de documentação** — Drive, Notion e repositório. Falta decidir qual é a fonte
      da verdade e o que os outros dois são
- [ ] **ADR-0009 — geocodificação.** `endereco` tem `latitude`/`longitude` e nada as preenche.
      `RF013` segue sendo o único requisito do MVP sem caminho técnico
- [ ] **Índice geoespacial.** B-tree em `(latitude, longitude)` não serve para busca por raio.
      Decidir junto do ADR-0009
- [ ] **`alvo_id` não é validado pelo banco** em `notificacao` e `denuncia`. Dívida assumida no
      ADR-0007, que exige teste de service cobrindo o par (`alvo_tipo`, `alvo_id`)
- [ ] **Migrations do Flyway.** O modelo está aprovado no papel; `V1__*.sql` não existe
- [ ] **`docker-compose.yml`** com PostgreSQL, MinIO e SMTP de desenvolvimento
- [ ] **PR desta branch para `main`** — o `docs-parity.yml` nunca rodou contra mudança real, e
      esta sessão alterou o par JSON/markdown três vezes
- [ ] **`.gitignore` está modificado e não commitado** (acrescenta `.mcp.json`) — veio da sessão
      anterior e não foi tocado aqui
- [ ] Pendências herdadas: `LICENSE` com 0 bytes; falta ADR do `utils/` no frontend; `.gitignore`
      ainda não cobre `target/` e `.next/`

## Próximos passos

1. **Abrir o PR da branch** e verificar o `docs-parity.yml`. São cinco commits de documentação
   parados numa branch, e o workflow que os protege nunca foi exercitado
2. **Escrever o ADR-0009 da geocodificação**, decidindo junto a estratégia de índice geoespacial
3. **Executar a semana 1** conforme o §4 do plano — a trilha A (migrations) é a que desbloqueia a
   trilha D, então ela começa segunda
4. **Resolver o acesso ao Trello** e povoar o Backlog com os épicos B a G, senão o custeio de
   404 h fica só no repositório e não vira acompanhamento
5. **Decidir a fonte da verdade da documentação** entre Drive, Notion e repositório antes que os
   três divirjam
