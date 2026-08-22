# Documentação — Bico em Casa

Documentação viva do projeto. Tudo aqui é versionado junto com o código e revisado em Pull
Request como código.

---

## Regra número um

> [!IMPORTANT]
> **`arquitetura-sistema.json` é a única fonte da verdade da arquitetura.**
> `design-sistema.md` é o espelho legível dele.
>
> **Ordem obrigatória de alteração:**
> 1. Alterar `arquitetura-sistema.json`
> 2. Refletir a mesma alteração em `design-sistema.md` — **no mesmo commit**
> 3. Registrar o *porquê* em um ADR quando a mudança for estrutural
>
> Editar o markdown sem antes editar o JSON é proibido. Divergência entre os dois é bug.

Essa ordem existe porque o JSON é consumível por ferramenta (CI, geradores, validação) e o
markdown é consumível por gente. Se o markdown liderar, a máquina lê algo que ninguém garantiu.

---

## Mapa

| Artefato | O que é | Quando consultar |
|---|---|---|
| [`arquitetura-sistema.json`](./arquitetura-sistema.json) | **Fonte da verdade.** Estrutura, versões, dependências e convenções em formato de máquina | Para saber a versão exata de algo ou automatizar validação |
| [`design-sistema.md`](./design-sistema.md) | Espelho legível do JSON, com diagramas e exemplos | Para **entender** a arquitetura |
| [`adr/`](./adr/) | Decisões de arquitetura e seus porquês | Antes de questionar ou mudar uma escolha estrutural |
| [`planos/`](./planos/) | Planos de execução, escritos antes do trabalho | Ao iniciar um trabalho de mais de uma sessão |
| [`relatorios/`](./relatorios/) | Relatórios de sessão, escritos depois | Para reconstruir o que aconteceu e quando |

---

## Por onde começar

**Novo no projeto?** Leia nesta ordem:

1. [`design-sistema.md`](./design-sistema.md) §1 — visão geral e o desenho das três peças
2. [`adr/README.md`](./adr/README.md) — o índice de decisões, para entender o *porquê* de cada escolha
3. [`design-sistema.md`](./design-sistema.md) §10 — estrutura de diretórios, com as regras que mais pegam quem chega

**Vai escrever código?** As três regras que mais geram retrabalho quando ignoradas:

| Regra | Onde |
|---|---|
| Pasta só existe quando há **mais de um arquivo** daquele tipo | [§10.2](./design-sistema.md) |
| Interface em `contrato/`, implementação em `service/` com sufixo `Impl` | [§10.2](./design-sistema.md) |
| Nenhum `fetch` fora de `src/api/` | [§4.2](./design-sistema.md) |

---

## Os três artefatos de processo

Respondem perguntas diferentes e não se substituem:

| Artefato | Pergunta | Quando |
|---|---|---|
| **Plano** | *O que vamos fazer e em que ordem?* | Antes |
| **ADR** | *Por que decidimos assim?* | No momento da decisão |
| **Relatório** | *O que de fato aconteceu?* | Depois |

---

## Idioma

Toda a documentação, comentários, Javadoc, TSDoc e mensagens de commit são em **português**.
Nomes de código (classes, variáveis, pastas) seguem o domínio em português —
`ProfissionalService`, `modulos/contratacoes/` — exceto termos técnicos consagrados
(`Repository`, `Controller`, `DTO`).
