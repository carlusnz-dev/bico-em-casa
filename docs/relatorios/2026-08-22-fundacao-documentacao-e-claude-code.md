# Relatório de Sessão — 2026-08-22

| Campo | Valor |
|---|---|
| **Sessão** | Fundação da documentação e do harness do Claude Code |
| **Autor** | Carlos Antunes |
| **Data** | 2026-08-22 |
| **Duração aproximada** | ~35min (01:57 – 02:32) |
| **LLM utilizada** | `Claude Opus 5 (Claude Code)` |
| **Branch** | `main` |
| **Commits** | **Nenhum.** O único commit no histórico (`b73343b`, 00:56) é anterior a esta sessão. Todo o trabalho está no working tree, não commitado |
| **Plano relacionado** | `~/.claude/plans/zesty-rolling-blossom.md` (fora do repositório) |

---

## Resumo

Sessão de fundação: o repositório tinha um commit e estava praticamente vazio — `CLAUDE.md`,
`LICENSE` e `docs/README.md` com 0 bytes, `.claude/settings.json` em `{}`, e um único artefato
real, `docs/arquitetura-sistema.json`.

Esse JSON tinha três defeitos: era um template genérico (`enterprise-fullstack-system`, pacote
`com.company.app`), apontava para versões fora de suporte, e descrevia um recorte por camada
técnica que o usuário rejeitou. Os três foram corrigidos, e a arquitetura foi reescrita conforme
a especificação: `config/` + `lib/` + `comum/` + `modulos/` no backend, e exatamente
`src/{app,components,hooks,api}` no frontend.

Sobre essa base foram criados o markdown de paridade, cinco ADRs que dão rastro às decisões,
templates de plano e relatório, e o harness do Claude Code — incluindo um hook que transforma a
regra de paridade da documentação de convenção em enforcement.

O achado mais relevante da sessão foi de infraestrutura, não de organização: **a linha Spring Boot
3.5.x encerrou o suporte OSS em 30/06/2026**, então o JSON original iniciava o projeto numa linha
morta, sem correção de segurança.

## O que foi feito

- `docs/arquitetura-sistema.json` reescrito com identidade real do projeto, versões atuais
  verificadas por consulta web, e a estrutura de diretórios especificada pelo usuário
- `docs/design-sistema.md` criado como espelho legível 1:1 do JSON, com diagrama da arquitetura e
  exemplos lado a lado de módulo simples vs. módulo composto
- Cinco ADRs registram retroativamente as decisões que já haviam sido tomadas, com o campo
  **LLM utilizada** preenchido para rastreabilidade acadêmica
- Templates de ADR, plano e relatório, cada um com header em tabela e README explicando quando usar
- Regra de paridade JSON→MD passou a ter três camadas de reforço: documentada, um hook que lembra
  na hora da edição, e um workflow que falha o PR
- Harness do Claude Code montado: `settings.json`, skill `role-dev`, comando `/relatorio-sessao` e
  três agentes de escopo estreito
- `CLAUDE.md` da raiz preenchido com regras operacionais e a lista do que nunca fazer

## Decisões tomadas

| Decisão | Motivo | ADR |
|---|---|---|
| Supabase como BaaS: Postgres + Auth + Storage | Construir identidade do zero consome sprints e erra caro | [ADR-0002](../adr/0002-supabase-como-baas.md) |
| Spring Boot 4.1 com Java 21 | A 3.5.x saiu do suporte OSS em 30/06/2026 | [ADR-0003](../adr/0003-spring-boot-4-java-21-maven.md) |
| Maven como build tool | Resolve a ambiguidade "Gradle / Maven" do JSON original | [ADR-0003](../adr/0003-spring-boot-4-java-21-maven.md) |
| Estrutura modular por domínio | Recorte por camada não escala e não sinaliza violação de fronteira | [ADR-0004](../adr/0004-estrutura-modular-por-dominio.md) |
| Módulos v1: MVP enxuto (6 módulos) | Cobre o fluxo central sem inflar o escopo | [ADR-0004](../adr/0004-estrutura-modular-por-dominio.md) |
| `src/api` como única fronteira | Tipo e schema em lugares separados desincronizam | [ADR-0005](../adr/0005-src-api-como-unica-fronteira.md) |
| Remover `jjwt` | Consequência do Supabase Auth: o backend só valida via JWKS | [ADR-0002](../adr/0002-supabase-como-baas.md) |
| Remover `axios` | Contorna o cache e a revalidação do App Router do Next 16 | [ADR-0005](../adr/0005-src-api-como-unica-fronteira.md) |
| Interface sem prefixo `I` (`ProfissionalService` + `ProfissionalServiceImpl`) | Não é idiomático em Java; o nome limpo pertence ao contrato | [ADR-0004](../adr/0004-estrutura-modular-por-dominio.md) |
| Pasta dos ADRs em `docs/adr/` | Convenção estabelecida; o usuário não especificou o nome | Não requer |

As três primeiras linhas foram escolhas do usuário respondidas em `AskUserQuestion`. As demais
foram decisões técnicas tomadas na sessão e registradas em ADR.

## Arquivos alterados

| Arquivo | Alteração |
|---|---|
| `docs/arquitetura-sistema.json` | **Reescrito** — 445 linhas. Identidade, versões, governança, Supabase, estrutura |
| `docs/design-sistema.md` | **Criado** — 545 linhas. Espelho do JSON |
| `docs/README.md` | **Preenchido** (era 0 bytes) — índice e regra de paridade |
| `docs/adr/TEMPLATE.md`, `README.md` | **Criados** — template com header em tabela e índice |
| `docs/adr/0001..0005-*.md` | **Criados** — cinco ADRs |
| `docs/planos/TEMPLATE.md`, `README.md` | **Criados** |
| `docs/relatorios/TEMPLATE.md`, `README.md` | **Criados** |
| `CLAUDE.md` | **Preenchido** (era 0 bytes) — 90 linhas |
| `.claude/settings.json` | **Preenchido** (era `{}`) — permissions, env, hook |
| `.claude/hooks/paridade-docs.sh` | **Criado** — hook `PostToolUse`, executável |
| `.claude/skills/role-dev/SKILL.md` | **Criado** |
| `.claude/commands/relatorio-sessao.md` | **Criado** |
| `.claude/agents/{arquiteto-sistema,revisor-codigo,escritor-adr}.md` | **Criados** |
| `.github/workflows/docs-parity.yml` | **Criado** |

Totais: 13 markdowns e 1 JSON em `docs/` (1882 linhas), 7 arquivos em `.claude/` (399 linhas),
1 workflow (51 linhas), `CLAUDE.md` (90 linhas).

## Verificações executadas

| Comando / teste | Resultado |
|---|---|
| `jq empty docs/arquitetura-sistema.json` | ✅ JSON válido |
| Hook — pipe-test com o arquivo alvo | ✅ emitiu o JSON de lembrete, exit 0 |
| Hook — pipe-test com `design-sistema.md` | ✅ silêncio, exit 0 |
| Hook — pipe-test com payload sem `file_path` | ✅ silêncio, exit 0 |
| Hook — disparo real via `Edit` no JSON | ✅ injetou a regra de paridade no contexto |
| Edição de teste revertida (`2.0.0-teste-hook` → `2.0.0`) | ✅ revertida |
| `jq -e` no `settings.json` (sintaxe + nesting do hook) | ✅ exit 0 |
| Lógica do `docs-parity.yml` nos 5 cenários | ✅ 5/5 conforme esperado |
| Resíduo de template genérico (`com.company.app`, `enterprise-fullstack-system`) | ✅ zero |
| Menções a Spring Boot 3.x fora do ADR-0003 | ✅ zero |
| Paridade de 10 versões entre JSON e MD | ✅ presentes nos dois |
| Paridade dos 6 módulos entre JSON e MD | ✅ presentes nos dois |
| Frontmatter dos 5 artefatos `.claude` | ✅ todos com frontmatter |
| Hook executável (`-x`) | ✅ |
| **Workflow `docs-parity.yml` num PR real** | ⏭️ **não executado** — sem remote GitHub e sem PR. Só a lógica shell foi testada localmente |
| **Comando `/relatorio-sessao` como slash command** | ⏭️ **não executado** — este relatório foi escrito seguindo as instruções do comando manualmente, não invocando-o |
| **Build / testes de aplicação** | ⏭️ **não executado** — não existe código de aplicação ainda |

## Problemas encontrados

**Plan mode ativou no meio da execução.** O `arquitetura-sistema.json` já havia sido reescrito e
validado quando o plan mode entrou. Registrei isso explicitamente como "Etapa 0 — já concluída" no
plano em vez de fingir que o trabalho começaria do zero, e as edições pararam até a aprovação.

**Meu primeiro grep de resíduos deu falso positivo.** O padrão `3\.3\.x` casou com Flyway
**13**.3.x, e `Spring Boot 3` casou com o ADR-0003, onde a menção é o contexto histórico sendo
corrigido. Refiz com padrões precisos (`com\.company\.app|enterprise-fullstack-system` e um
`grep -v adr/0003`) antes de afirmar que estava limpo. A lição vale para o CI: um grep de
conformidade com padrão frouxo gera ruído que treina o time a ignorar o alerta.

**Ambiguidades no JSON original que exigiram decisão do usuário.** Três pontos não eram
resolvíveis por leitura do repositório — papel do Supabase, build tool, e recorte dos módulos.
Foram perguntados antes de escrever, não assumidos.

**Typo corrigido** em `docs/relatorios/README.md` ("aconteecu" → "aconteceu").

## Pendências

- [ ] **Nada foi commitado.** Todo o trabalho está no working tree, em `main`. O `design-sistema.md`
      contém a regra de que commits diretos em `main` são proibidos — vale decidir se a fundação
      entra por PR a partir de `develop` ou se `main` recebe o commit inicial antes da regra valer
- [ ] **`utils/` no frontend.** `src/` tem exatamente as quatro pastas especificadas, e `utils/`
      ficou registrado como `excluded_by_design`. Na prática `clsx` + `tailwind-merge` vão pedir um
      `cn()` sem casa definida. Precisa de ADR-0006
- [ ] **`.gitignore` incompleto** — tem apenas `node_modules`, `.vscode` e `.env`. Faltam `target/`,
      `.next/`, `build/`, `.mvn/`. Vai sujar no primeiro build
- [ ] **`LICENSE` está com 0 bytes**
- [ ] **Nenhuma skill além de `role-dev`** — o usuário sinalizou que ainda vai decidir quais
- [ ] Workflows `backend-ci.yml` e `frontend-ci.yml` estão declarados no JSON mas ainda não existem
      como arquivo. Só fazem sentido quando houver código

## Próximos passos

1. Decidir o fluxo de commit da fundação e commitar (a documentação só protege o projeto depois de
   estar versionada)
2. Modelagem do banco: entidades dos 6 módulos, migrations Flyway iniciais e a correlação
   `tb_usuarios.supabase_user_id` com o `sub` do JWT do Supabase
3. Backlog do MVP, derivado dos módulos já definidos
4. Fechar as pendências pequenas: `.gitignore`, `LICENSE`, ADR-0006 do `utils/`
