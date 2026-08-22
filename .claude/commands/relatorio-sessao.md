---
description: Gera o relatorio desta sessao em docs/relatorios/ seguindo o template do projeto
argument-hint: "[titulo curto da sessao]"
allowed-tools: Read, Write, Glob, Bash(git status:*), Bash(git log:*), Bash(git diff:*), Bash(git branch:*), Bash(date:*)
---

# Relatório de Sessão

Registre o que aconteceu nesta sessão em `docs/relatorios/`.

## Contexto coletado automaticamente

- **Data de hoje:** !`date +%Y-%m-%d`
- **Branch atual:** !`git branch --show-current`
- **Status do working tree:** !`git status --short`
- **Commits desta sessão (últimas 24h):** !`git log --since="24 hours ago" --oneline --no-decorate || echo "nenhum commit nas ultimas 24h"`
- **Arquivos alterados vs. HEAD:** !`git diff --stat HEAD || echo "sem diff"`
- **Relatórios já existentes:** !`ls docs/relatorios/*.md 2>/dev/null | grep -v TEMPLATE || echo "nenhum ainda"`

## O que fazer

1. **Leia o template:** `docs/relatorios/TEMPLATE.md`.

2. **Determine o nome do arquivo:** `docs/relatorios/AAAA-MM-DD-<slug>.md`, onde o slug vem de
   `$ARGUMENTS` (em kebab-case) ou, se vazio, do tema dominante da sessão. Se já existir um
   relatório com esse nome hoje, acrescente sufixo `-2`, `-3`.

3. **Preencha o template com o que realmente aconteceu nesta conversa**, usando o contexto de git
   acima para as seções de branch, commits e arquivos alterados.

4. **Salve o arquivo.**

5. **Responda ao usuário** com o caminho do arquivo criado e um resumo de 3 a 5 linhas.

## Regras de honestidade — leia antes de escrever

Estas regras existem porque um relatório complacente é pior que nenhum relatório: ele cria uma
falsa memória do projeto.

- **Registre o que aconteceu, não o que era para acontecer.** Etapa pulada, abandonada ou
  bloqueada entra no relatório como tal.
- **A seção "Verificações executadas" só aceita comando que foi de fato executado nesta sessão,
  com o resultado que ele de fato deu.** Nunca marque como ✅ algo que não rodou — use ⏭️ e diga
  por que não rodou. Se um teste falhou, ele entra como ❌ com a saída.
- **Não invente commits, arquivos ou números.** Se o git não mostra, não aconteceu.
- **Toda decisão estrutural tomada sem ADR correspondente vira item em "Pendências".**
- **A seção "Problemas encontrados" é obrigatória quando houve problema.** É a parte mais útil do
  relatório meses depois — o problema costuma voltar.

Se a sessão foi curta ou improdutiva, o relatório reflete isso. Um relatório de três linhas que é
verdade vale mais que três páginas que não são.
