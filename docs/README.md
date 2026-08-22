# Documentação — Bico em Casa

Documentação viva do projeto. Tudo aqui é versionado junto com o código e revisado em Pull
Request como código.

---

## Regra número um

> [!IMPORTANT]
> **O JSON manda, o markdown espelha, os dois mudam no mesmo commit.**
>
> A regra vale para **dois pares independentes**:
>
> | Fonte da verdade | Espelho legível | Derivado |
> |---|---|---|
> | `arquitetura-sistema.json` | `design-sistema.md` | — |
> | `requisitos.json` | `requisitos.md` | `matriz-rastreabilidade.md` |
>
> **Ordem obrigatória:** alterar o JSON → refletir no markdown → ADR se a mudança for estrutural.
> No caso dos requisitos, rodar `/revisar-matriz` para regenerar o derivado.
>
> Editar o markdown sem antes editar o JSON é proibido. Divergência entre os dois é bug.

Essa ordem existe porque o JSON é consumível por ferramenta (CI, geradores, validação) e o
markdown é consumível por gente. Se o markdown liderar, a máquina lê algo que ninguém garantiu.

Os dois pares são **separados de propósito**: arquitetura e requisito mudam por motivos
diferentes e em ritmos diferentes. Requisito novo não deveria sujar o diff da arquitetura, e
troca de versão de framework não deveria sujar o diff dos requisitos.

---

## Mapa

| Artefato | O que é | Quando consultar |
|---|---|---|
| [`arquitetura-sistema.json`](./arquitetura-sistema.json) | **Fonte da verdade da arquitetura.** Estrutura, versões, dependências e convenções em formato de máquina | Para saber a versão exata de algo ou automatizar validação |
| [`design-sistema.md`](./design-sistema.md) | Espelho legível do JSON, com diagramas e exemplos | Para **entender** a arquitetura |
| [`requisitos.json`](./requisitos.json) | **Fonte da verdade dos requisitos.** Um objeto por requisito, com módulos, entidades, status e origem | Para automatizar validação ou gerar a matriz |
| [`requisitos.md`](./requisitos.md) | Espelho legível dos requisitos, com o histórico da revisão | Para **entender** o escopo e o que mudou desde a tabela de aula |
| [`matriz-rastreabilidade.md`](./matriz-rastreabilidade.md) | **Derivado.** Requisito → módulo → entidade, e a leitura inversa | Antes de mexer num módulo, para saber o que ele precisa continuar cumprindo |
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
