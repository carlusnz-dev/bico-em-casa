# Planos

Um **plano** é escrito **antes** do trabalho. Ele descreve o que será feito, em que ordem, e
como verificar que deu certo.

## Convenção de nome

`AAAA-MM-DD-titulo-em-kebab-case.md` — ex.: `2026-08-25-modulo-profissionais.md`

## Quando escrever um plano

**Escreva** quando o trabalho:

- Ocupa mais de uma sessão
- Toca mais de um módulo ou as duas pontas (backend e frontend)
- Envolve migration de banco ou mudança de contrato de API
- Precisa ser executado por outra pessoa, ou por você daqui a semanas

**Não escreva** para correção pontual, ajuste de texto ou tarefa que cabe num commit.

## Plano · ADR · Relatório

Os três artefatos respondem perguntas diferentes e não se substituem:

| Artefato | Pergunta | Quando |
|---|---|---|
| [**Plano**](.) | *O que vamos fazer e em que ordem?* | Antes |
| [**ADR**](../adr/) | *Por que decidimos assim?* | No momento da decisão |
| [**Relatório**](../relatorios/) | *O que de fato aconteceu?* | Depois |

Um plano de execução frequentemente **produz** ADRs no meio do caminho — decisões que
apareceram durante o trabalho. Registre-as na hora, não no final.

## Ciclo de vida

`Rascunho` → `Aprovado` → `Em execução` → `Concluído`

Plano cancelado permanece no repositório com status `Cancelado` e uma linha explicando o
motivo. Saber o que foi abandonado, e por quê, tem o mesmo valor de saber o que foi feito.
