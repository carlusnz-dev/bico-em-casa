# Plano — Título do Plano

| Campo | Valor |
|---|---|
| **Plano** | Título curto |
| **Autor** | Nome do autor |
| **Data** | `AAAA-MM-DD` |
| **Escopo** | Backend · Frontend · Banco · Infra · Documentação |
| **Status** | Rascunho · Aprovado · Em execução · Concluído · Cancelado |
| **LLM utilizada** | ex.: `Claude Opus 5 (Claude Code)` · `Nenhuma` |
| **ADRs relacionados** | ADR-NNNN, ADR-NNNN · Nenhum |

---

## Contexto

Por que este trabalho existe. Qual problema resolve, o que o motivou, qual o estado atual do
que será alterado.

Escreva para alguém que vai executar o plano daqui a duas semanas sem ter participado da
conversa que o originou.

## Objetivo

O resultado esperado em uma ou duas frases. Deve ser verificável: alguém precisa conseguir
olhar o repositório no final e dizer *"sim, isso foi alcançado"* ou *"não foi"*.

## Fora de Escopo

O que este plano **não** faz, mesmo parecendo relacionado. É a seção que evita que o escopo
cresça durante a execução.

- Item deliberadamente deixado de fora, e por quê

## Pré-requisitos

O que precisa estar pronto antes de começar.

- [ ] Pré-requisito

## Etapas

| # | Etapa | Arquivos afetados | Critério de aceite |
|---|---|---|---|
| 1 | | | |
| 2 | | | |
| 3 | | | |

> Cada etapa deve ser pequena o bastante para caber num commit e ter um critério de aceite
> objetivo. "Implementar o módulo" não é etapa; "criar a entidade e a migration de
> `tb_profissionais`" é.

## Riscos e Mitigações

| Risco | Impacto | Mitigação |
|---|---|---|
| | Alto · Médio · Baixo | |

## Impacto na Arquitetura

Este plano altera `docs/arquitetura-sistema.json`?

- [ ] Sim — exige ADR e atualização de `design-sistema.md` no mesmo commit
- [ ] Não

## Verificação

Como comprovar, ao final, que o plano foi cumprido. Comandos concretos, não intenções.

1. `comando` → resultado esperado
2. `comando` → resultado esperado

## Pendências

O que ficou em aberto ao final da execução e precisa virar issue ou novo plano.
