# Relatório de Sessão — 2026-08-22

| Campo | Valor |
|---|---|
| **Sessão** | Revisão dos requisitos, matriz de rastreabilidade, saída do Supabase e modelagem do banco |
| **Autor** | Carlos Antunes |
| **Data** | `2026-08-22` |
| **Duração aproximada** | `6h` (sessão longa, com compactação de contexto no meio) |
| **LLM utilizada** | `Claude Opus 5 (Claude Code)` |
| **Branch** | `docs/requisitos-e-matriz-rastreabilidade` |
| **Commits** | `5f3aa82`, `8b7c773`, `304713a`, `0963c78`, `6767363` |
| **Plano relacionado** | Nenhum — a sessão nasceu de uma revisão, não de um plano prévio |

---

## Resumo

A sessão começou com um objetivo modesto — revisar os requisitos funcionais escritos em aula e
estruturá-los em documento — e terminou com três entregas que se encadearam: o conjunto de
requisitos revisado e versionado, a saída do Supabase do MVP, e o modelo de dados das 18 tabelas.

A revisão dos requisitos encontrou defeitos que impediam a matriz de rastreabilidade de existir.
O mais grave era um código duplicado: dois requisitos diferentes carregavam `RF016`, o que
significa dezessete requisitos disputando dezesseis códigos. Além disso, os módulos citados na
tabela original (*Portfólio*, *Perfil*, *Administrador*, *Pesquisa do serviço*) não correspondiam
a nenhum módulo real da arquitetura do ADR-0004, quatro requisitos estavam sem módulo, e **não
existia um único requisito não funcional**. Seis requisitos que o próprio grupo havia levantado no
canvas PBB nunca chegaram à tabela. Um deles expôs uma lacuna lógica: o `RF012` notificava o
cliente de um aceite que nenhum requisito permitia acontecer — virou `RF017`.

No meio da sessão veio a decisão mais cara do dia. Ao revisar o rascunho de schema, a coluna
`hash_senha` apareceu contradizendo o ADR-0002: se o Supabase custodia a senha, o backend não
guarda hash. Em vez de remover a coluna, o autor decidiu remover o Supabase — o motivo não foi
técnico, e o ADR-0006 registra isso explicitamente: o argumento do ADR-0002 continua correto, o
que mudou foi o **objetivo do projeto**. A disciplina existe para ensinar banco de dados, e
terceirizar identidade e persistência terceirizava justamente o que se quer aprender. O custo
apareceu em número: o módulo `autenticacao` saltou de 4 para 7 requisitos, e nasceram quatro RNFs
(`RNF019`–`RNF022`) que descrevem garantias que o fornecedor dava de graça.

A última parte da sessão foi de análise, não de escrita. O autor refez o modelo no dbdiagram por
conta própria, com 14 tabelas, e pediu a revisão. A análise apontou três acertos que o modelo
canônico não tinha e três defeitos bloqueantes — entre eles um `NOT NULL` em
`cancelamento_perfil_id` que torna impossível inserir uma contratação. Essa versão foi preservada
como rascunho, sem alteração, para ser incorporada depois.

## O que foi feito

- **Os requisitos passaram a ter fonte da verdade versionada.** `docs/requisitos.json` guarda 46
  requisitos (24 RF + 22 RNF), com chave por código e os campos `codigo`, `tipo`, `descricao`,
  `modulos`, `entidades`, `status`, `origem` e `data_criada`
- **A matriz de rastreabilidade existe e é derivada, não digitada.** As tabelas do markdown são
  geradas por `jq` a partir do JSON, o que garante que as 46 descrições batam literalmente entre
  os dois arquivos
- **O comando `/revisar-matriz` passou a existir**, com 12 diagnósticos automáticos, modo
  `--check` e regras de honestidade explícitas (não inventar rastro, não renumerar, não apagar
  requisito)
- **O Supabase saiu do MVP** e a decisão virou ADR-0006. O ADR-0002 foi marcado como substituído e
  preservado — os dois lidos em sequência mostram o trade-off entre velocidade de entrega e
  profundidade de aprendizado
- **O modelo de dados do MVP ficou definido em 18 tabelas**, com 26 relacionamentos e política de
  exclusão explícita em cada um
- **`tb_avaliacoes` passou a existir.** Não estava no rascunho original, apesar de `RF006`,
  `RF007` e `RF019` a exigirem — foi a lacuna mais séria encontrada no schema
- **A revisão de banco feita pelo autor no dbdiagram foi preservada** como rascunho datado, com o
  cabeçalho registrando o que ela traz de bom, o que ainda falta nela e os defeitos mapeados
- **O PDF de especificação e os `.docx` saíram do versionamento**; os dois PNG de aula entraram,
  porque `requisitos.md` os cita como origem de seis requisitos

## Decisões tomadas

| Decisão | Motivo | ADR |
|---|---|---|
| Remover o Supabase do MVP e assumir autenticação, storage e e-mail próprios | Aprender banco de dados é o objetivo da disciplina; terceirizar identidade e persistência terceiriza o aprendizado. Some-se controle de segurança e de custo | [ADR-0006](../adr/0006-remover-supabase-infraestrutura-propria.md) |
| Argon2id para senha e JWT RSA emitido pelo backend | Argon2id é o padrão atual para hash de senha; `NimbusJwtEncoder`/`Decoder` já vêm em `spring-security-oauth2-jose`, então não é preciso adicionar `jjwt` | Coberto pelo ADR-0006 |
| MinIO como storage de objetos, com URL pré-assinada | Compatível com S3, self-hosted, e os bytes não passam pela API | Coberto pelo ADR-0006 |
| Um usuário tem N perfis, um por papel, via `UNIQUE (usuario_id, tipo)` | O enum único do rascunho impedia que um pintor contratasse um chaveiro sem criar outra conta. Corrigir depois exigiria migrar dados | Não requer — decorre do `RF002` |
| `tb_servicos` e `tb_contratacoes` são tabelas distintas | Catálogo e trabalho contratado são entidades diferentes. `valor_final` congelado na contratação impede que reajuste de preço reescreva o histórico do `RF016` | Não requer — documentado em `modelo-dados.md` §2 |
| `tb_log_acoes` é somente de escrita, sem `atualizado_em` | Registro de auditoria que pode ser alterado não é auditoria | Não requer — `RNF022` |
| Manter o `RF013` (distância em km) no MVP | Decisão de escopo do autor. Exige coordenadas persistidas e geocodificação — **ainda sem ADR** | ⚠️ Pendente — vira ADR-0007 |
| A regra nº 1 passou a valer para **dois pares** de arquivos | Arquitetura e requisito mudam por motivos e em ritmos diferentes; requisito novo não deve sujar o diff da arquitetura | Não requer — registrado em `docs/README.md` |

## Arquivos alterados

| Arquivo | Alteração |
|---|---|
| `docs/requisitos.json` | Criado — 46 requisitos, fonte da verdade. Depois elevado a 1.1.0 pelo ADR-0006 |
| `docs/requisitos.md` | Criado — espelho legível, com a seção §4 destinada ao grupo |
| `docs/matriz-rastreabilidade.md` | Criado — requisito → módulo → entidade e a leitura inversa |
| `.claude/commands/revisar-matriz.md` | Criado — comando de manutenção da matriz |
| `docs/adr/0006-remover-supabase-infraestrutura-propria.md` | Criado — a decisão do dia |
| `docs/adr/0002-supabase-como-baas.md` | Alterado — marcado como substituído, conteúdo preservado |
| `docs/adr/README.md` | Alterado — índice atualizado |
| `docs/arquitetura-sistema.json` | Alterado — segurança, dependências, hosting, serviços externos e `lib/` reescritos sem Supabase |
| `docs/design-sistema.md` | Alterado — 8 pontos espelhando o JSON, incluindo o diagrama da arquitetura |
| `docs/modelo-dados.dbml` | Criado — 18 tabelas, 26 refs, 4 enums |
| `docs/modelo-dados.md` | Criado, depois alterado — o modelo explicado; ganhou a pendência de incorporar o rascunho |
| `docs/modelo-dados-rascunho-2026-08-22.dbml` | Criado — a revisão do autor, preservada sem alteração |
| `docs/README.md` | Alterado — mapa de artefatos e regra nº 1 reescrita para dois pares |
| `.gitignore` | Alterado — `docs/*.pdf` e `*.docx` |
| `docs/PBB_bico-em-casa.png`, `docs/schema bico em casa.png` | Criados — versionados por serem citados como origem de requisitos |
| `~/Documentos/pessoal/00_Inbox/MOC - Bico em Casa.md` | Alterado **fora do repositório** — reescrito no estilo do vault |
| `~/Documentos/pessoal/00_Inbox/Rascunho de requisitos - Bico em Casa (2026-08-19).md` | Criado **fora do repositório** — congela o rascunho original |

## Verificações executadas

| Comando | Resultado |
|---|---|
| Paridade `requisitos.json` × `requisitos.md` × matriz, por `jq` + `diff` | ✅ 46 códigos idênticos nos três arquivos |
| Comparação literal das 46 descrições entre JSON e markdown | ✅ 46/46 batem byte a byte |
| Chave do JSON igual ao campo `codigo` de cada requisito | ✅ nenhuma divergência |
| Módulos validados contra `_meta.modulos_validos` | ✅ nenhum módulo inválido |
| Buracos na numeração `RF`/`RNF` | ✅ nenhum |
| Cobertura entidade × tabela nos dois sentidos, com `LC_ALL=C comm` | ✅ nenhuma entidade de requisito MVP sem tabela; nenhuma tabela sem requisito |
| Refs do DBML resolvem para tabelas existentes | ✅ 26/26 |
| Colunas citadas nas refs existem nas tabelas | ✅ todas |
| Enums usados estão definidos | ✅ 4/4 |
| Caminhos de chave do JSON antes e depois de cada transformação `jq` | ✅ nenhuma chave espúria criada |
| Resíduos de "supabase" em JSON e markdown, por `grep -i` | ✅ restaram apenas referências históricas intencionais |
| `/revisar-matriz` contra um JSON deliberadamente sabotado | ✅ os 6 defeitos plantados foram detectados |
| `git status` ao final | ✅ árvore limpa |
| Renderização do DBML no dbdiagram.io | ⏭️ não executado — a sintaxe e a integridade referencial foram validadas por script, mas o arquivo não foi colado na ferramenta |
| Migrations do Flyway | ⏭️ não executado — não existem |
| Qualquer teste automatizado | ⏭️ não executado — não há código de aplicação no repositório |

## Problemas encontrados

**Três diagnósticos do `/revisar-matriz` passavam em falso.** Os checks que usavam `index(.)` e
`IN()` dentro de `select` referenciavam o array, não o item — o check de módulo inválido **nunca**
reportaria nada. Dois deles só imprimiam "nenhum" por causa do fallback `|| echo`. A primeira
tentativa de correção também estava errada (`$v|IN($m)`, com os operandos invertidos). A correção
final captura a variável corretamente, e só foi confiável depois de rodar o comando contra um JSON
sabotado de propósito com seis defeitos — todos foram pegos.

> Lição: um diagnóstico que nunca acusa nada é indistinguível de um sistema saudável. Todo check
> novo precisa ser testado contra uma falha real antes de ser considerado pronto.

**Um bug de padding em `jq` imprimia `RF7` em vez de `RF007`.** A comparação `if . < 10` acontecia
depois de `tostring`, e comparar string com número em `jq` é sempre falso.

**O `requisitos.json` foi escrito sem acentuação na primeira versão.** Num projeto que exige
português é defeito por si só, mas o efeito colateral era pior: toda comparação de descrição entre
JSON e markdown acusaria divergência falsa. O arquivo foi reescrito inteiro.

**`tb_profissionais` sobrou no `RNF005`** depois que o conceito virou `tb_perfis`. Foi pego pelo
check de cobertura entidade × tabela — que é exatamente o cenário para o qual esse check existe.

**A nota do Obsidian não estava sob controle de versão.** Sobrescrevê-la teria destruído o
original em definitivo. Foi copiada e arquivada como nota datada antes de qualquer alteração.

**Avisos de ordenação em `comm`** por causa da collation do locale (`tb_servico_tags` antes de
`tb_servicos`). Resolvido com `LC_ALL=C sort`.

**A delegação para o `agy-bridge` foi rejeitada.** A regra global do `CLAUDE.md` manda delegar
arquivos com mais de 200 linhas, mas as chamadas foram interrompidas. O trabalho seguiu com
leitura direta via `sed`/`grep`.

## Pendências

- [ ] **ADR-0007 — geocodificação.** O `RF013` foi mantido no MVP, `tb_enderecos` já tem
      `latitude`/`longitude`, mas nada as preenche. É decisão estrutural tomada sem ADR
- [ ] **Índice geoespacial.** Se o `RF013` filtrar por raio, o B-tree em `(latitude, longitude)`
      não serve — precisa de PostGIS ou `earthdistance`. Decidir junto do ADR-0007
- [ ] **Migrations do Flyway.** O modelo está aprovado no papel; `V1__*.sql` não existe
- [ ] **`docker-compose.yml`** com PostgreSQL, MinIO e SMTP de desenvolvimento. Sem ele, ninguém
      da equipe sobe o projeto depois do ADR-0006
- [ ] **Incorporar os três acertos do rascunho** ao modelo canônico: `ultimo_login`,
      `denuncia.contratacao_id` e `slug_url` único
- [ ] **O rascunho ainda não tem controle de login, auditoria e endereço.** O autor atualiza numa
      próxima rodada
- [ ] **O MOC do Obsidian está desatualizado em relação ao ADR-0006.** Foi escrito na primeira
      metade da sessão e ainda lista o Supabase na tabela de stack e nas pendências de banco
- [ ] **Corrigir o quadro "é – não é – faz – não faz"** do PDF: promete chat, que saiu do MVP
- [ ] **Reescrever os critérios de aceite** das User Stories — sete das oito repetem literalmente
      os critérios do exemplo de filtro por data
- [ ] **Validar o `RNF005`** (busca em 2 s no p95) quando houver código. É meta declarada, não medida
- [ ] **Abrir PR desta branch para `main`.** A §7.1 proíbe commit direto em `main`, e os cinco
      commits estão em `docs/requisitos-e-matriz-rastreabilidade`
- [ ] Pendências herdadas da sessão anterior: `LICENSE` está com 0 bytes; falta ADR para `utils/`
      no frontend; `.gitignore` ainda não cobre artefatos de build (`target/`, `.next/`)

## Próximos passos

1. **Abrir o PR da branch** e verificar se o workflow `docs-parity.yml` passa — ele nunca rodou
   contra uma mudança real de documentação
2. **Escrever o ADR-0007 da geocodificação**, decidindo junto a estratégia de índice geoespacial.
   É o único requisito do MVP hoje sem caminho técnico definido
3. **Incorporar os três acertos do rascunho** ao `modelo-dados.dbml` e corrigir os bloqueantes
   apontados na análise
4. **Escrever as migrations do Flyway** a partir do modelo consolidado, e só então o
   `docker-compose.yml` — sem banco de pé, a migration não é testável
5. **Atualizar o MOC do Obsidian** para refletir o ADR-0006
