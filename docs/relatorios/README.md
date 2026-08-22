# Relatórios de Sessão

Um **relatório** é escrito **depois** do trabalho. Ele registra o que de fato aconteceu — não
o que era para acontecer.

## Convenção de nome

`AAAA-MM-DD-titulo-em-kebab-case.md` — ex.: `2026-08-22-fundacao-documentacao.md`

Mais de uma sessão no mesmo dia: acrescente um sufixo (`-2`, `-3`).

## Como gerar

Use o comando `/relatorio-sessao` ao final da sessão. Ele lê o histórico do git e o que foi
feito, preenche o `TEMPLATE.md` e salva o arquivo já nomeado.

Preencher à mão também funciona — o template é o mesmo.

## Regras

1. **Registre o que aconteceu, não o que deveria ter acontecido.** Etapa pulada é etapa pulada;
   escreva isso. Um relatório que só conta sucessos não serve para nada.
2. **A seção "Verificações executadas" só aceita comando de fato executado.** Marcar como
   passado algo que não foi rodado é pior que não ter a seção.
3. **Toda decisão estrutural sem ADR vira pendência.** O relatório é o último ponto de captura
   antes de a informação sumir.
4. **Relatório não é substituto de ADR.** O relatório diz *"decidimos usar X"*; o ADR diz
   *"por que X e não Y"*.

## Plano · ADR · Relatório

| Artefato | Pergunta | Quando |
|---|---|---|
| [**Plano**](../planos/) | *O que vamos fazer?* | Antes |
| [**ADR**](../adr/) | *Por que decidimos assim?* | No momento da decisão |
| [**Relatório**](.) | *O que de fato aconteceu?* | Depois |
