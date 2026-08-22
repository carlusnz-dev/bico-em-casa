# ADR-NNNN — Título da Decisão

| Campo | Valor |
|---|---|
| **ADR** | `NNNN` |
| **Título** | Título da decisão, em uma linha |
| **Autor** | Nome do autor |
| **Data** | `AAAA-MM-DD` |
| **Tópico** | Backend · Frontend · Banco · Infra · Processo |
| **Status** | Proposto · Aceito · Rejeitado · Depreciado · Substituído por ADR-NNNN |
| **LLM utilizada** | ex.: `Claude Opus 5 (Claude Code)` · `Nenhuma` |

---

## Contexto

Qual é a situação que exige uma decisão? Descreva as forças em jogo: restrição técnica,
prazo, requisito de negócio, dívida existente. Escreva em tempo presente e de forma neutra —
o contexto é o fato, não a opinião.

Não descreva a solução aqui. Se o leitor não entender **por que existe um problema**, o resto
do documento não vai convencer.

## Decisão

O que foi decidido, em voz ativa e afirmativa: *"Vamos usar X"*, não *"seria bom usar X"*.

Seja específico o bastante para ser verificável: versões, nomes de pacotes, caminhos de pasta.

## Alternativas Consideradas

| Opção | Prós | Contras | Por que foi recusada |
|---|---|---|---|
| Opção A | | | |
| Opção B | | | |

> Uma alternativa que nunca foi considerada de verdade não entra nesta tabela. ADR não é
> teatro de decisão.

## Consequências

### Positivas

- O que melhora concretamente

### Negativas

- O que piora, o que passa a custar mais caro, que porta se fecha

### Neutras

- O que muda sem ser bom nem ruim, mas que alguém precisa saber

## Impacto na Arquitetura

Esta decisão altera `docs/arquitetura-sistema.json`?

- [ ] Sim — campos alterados: `caminho.do.campo`
- [ ] Não

> Se sim, o JSON e o `design-sistema.md` devem ser atualizados **no mesmo commit** deste ADR.

## Referências

- Links para documentação, issues, benchmarks ou discussões que embasaram a decisão
