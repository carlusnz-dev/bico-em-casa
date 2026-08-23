# Architecture Decision Records (ADRs)

Um ADR registra **uma decisão de arquitetura e o porquê dela**. O código mostra *o que* foi
feito; o ADR preserva *por que* foi feito assim — a informação que evapora primeiro e que faz
alguém, seis meses depois, refazer uma discussão já encerrada.

## Índice

| # | Título | Tópico | Status | Data |
|---|---|---|---|---|
| [0001](./0001-adotar-adrs-para-decisoes-de-arquitetura.md) | Adotar ADRs para registrar decisões de arquitetura | Processo | Aceito | 2026-08-22 |
| [0002](./0002-supabase-como-baas.md) | Supabase como BaaS (Postgres + Auth + Storage) | Infra | ~~Substituído por [0006](./0006-remover-supabase-infraestrutura-propria.md)~~ | 2026-08-22 |
| [0003](./0003-spring-boot-4-java-21-maven.md) | Spring Boot 4.1 com Java 21 e Maven | Backend | Aceito | 2026-08-22 |
| [0004](./0004-estrutura-modular-por-dominio.md) | Estrutura modular por domínio no backend | Backend | Aceito | 2026-08-22 |
| [0005](./0005-src-api-como-unica-fronteira.md) | `src/api` como única fronteira do frontend | Frontend | Aceito | 2026-08-22 |
| [0006](./0006-remover-supabase-infraestrutura-propria.md) | Remover o Supabase e assumir infraestrutura própria | Infra · Backend · Banco | Aceito | 2026-08-22 |
| [0007](./0007-chave-primaria-mista.md) | Chave primária mista: `bigint` no cadastro, `uuid` no transacional | Banco | Aceito | 2026-08-22 |
| [0008](./0008-nomenclatura-de-tabelas.md) | Nomenclatura de tabelas: `snake_case` singular, sem prefixo | Banco | Aceito | 2026-08-22 |

## Como criar um ADR

1. Copie `TEMPLATE.md`
2. Numere sequencialmente a partir do último ADR existente: `NNNN-titulo-em-kebab-case.md`
3. Preencha o header em tabela por completo — inclusive o campo **LLM utilizada**
4. Abra com status **Proposto**; mude para **Aceito** quando a decisão for confirmada
5. Adicione a linha no índice acima
6. Se a decisão alterar a arquitetura, atualize `arquitetura-sistema.json` **e**
   `design-sistema.md` no mesmo commit

## Quando escrever um ADR

**Escreva** quando a decisão for cara de reverter ou quando alguém puder questioná-la depois:

- Escolha ou troca de framework, biblioteca ou serviço externo
- Mudança de estrutura de pastas ou de fronteira entre camadas
- Estratégia de autenticação, autorização ou persistência
- Adoção de padrão que restringe o time (proibir `any`, proibir H2 em teste)

**Não escreva** para escolhas triviais ou facilmente reversíveis: nome de variável, ordem de
campos num DTO, escolha de ícone.

## Status

| Status | Significado |
|---|---|
| **Proposto** | Em discussão. Ainda não vale como regra. |
| **Aceito** | Vigente. Vale como regra do projeto. |
| **Rejeitado** | Foi considerado e recusado. Fica no repositório para não ser rediscutido do zero. |
| **Depreciado** | Não vale mais, e nada o substituiu. |
| **Substituído por ADR-NNNN** | Foi trocado por outra decisão. Aponte o sucessor. |

> Um ADR **nunca é apagado nem reescrito** depois de aceito. Mudou de ideia? Escreva um novo
> ADR e marque o antigo como *Substituído*. O histórico das decisões é justamente o que dá
> valor à pasta.
