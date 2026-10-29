# ADR-0013 — Separar arquivamento do status da contratação

| Campo | Valor |
|---|---|
| **ADR** | `0013` |
| **Título** | Separar o arquivamento da contratação (`arquivada` booleano) do status do ciclo de vida |
| **Autor** | Tayllor |
| **Data** | `2026-10-29` |
| **Tópico** | Backend · Banco |
| **Status** | Aceito |
| **LLM utilizada** | `Gemini 4 Argon (Antigravity)` |

---

## Contexto

Na implementação inicial do módulo `contratacoes`, a coluna `contratacao.status` e o enum `StatusContratacao` armazenavam apenas `ATIVA` e `ARQUIVADA`, usados pelo cliente para arquivar ou desarquivar contratações na sua listagem.

A História de Usuário 17 (Visualizar status do serviço contratado) exige que o cliente visualize o andamento real do serviço contratado (`EM_ANDAMENTO`, `CONCLUIDO`, `CANCELADO`) e acompanhe atualizações feitas pelo profissional.

Manter o arquivamento do cliente e o andamento do serviço na mesma coluna `status` causa perda de dados: quando o cliente arquiva ou desarquiva uma contratação, o valor de `status` é sobrescrito por `ARQUIVADA` ou `ATIVA`, apagando o estado do serviço (`EM_ANDAMENTO`, `CONCLUIDO` ou `CANCELADO`).

## Decisão

1. **Separamos os dois conceitos em colunas distintas na tabela `contratacao`:**
   - `contratacao.arquivada` (`BOOLEAN NOT NULL DEFAULT false`): controla se a contratação está arquivada na visão do cliente (alterada via `PATCH /api/contratacao/{id}/arquivar` e `/desarquivar`).
   - `contratacao.status` (`StatusContratacao`): representa exclusivamente o andamento do serviço (`EM_ANDAMENTO`, `CONCLUIDO`, `CANCELADO`).
2. **Padronizamos os nomes no masculino (`CONCLUIDO`, `CANCELADO`)** no enum `StatusContratacao` e no documento de modelo de dados, concordando com "status do serviço contratado" conforme os critérios de aceite da US 17.
3. **Transições de status do ciclo de vida:** toda contratação nasce em `EM_ANDAMENTO`; apenas o profissional responsável pode transicionar `EM_ANDAMENTO` para os estados finais imutáveis `CONCLUIDO` ou `CANCELADO`.

## Alternativas Consideradas

| Opção | Prós | Contras | Por que foi recusada |
|---|---|---|---|
| Manter tudo em `status` (`ATIVA`, `ARQUIVADA`, `EM_ANDAMENTO`, `CONCLUIDO`, `CANCELADO`) | Sem nova coluna no banco | Arquivar/desarquivar sobrescreve o andamento do serviço; esconde ou corrompe o estado real da contratação | Dois conceitos ortogonais não cabem em uma única coluna escalar |
| Remover o recurso de arquivar contratações do cliente | Modelo mais simples, sem coluna extra | Quebra funcionalidade já entregue de arquivamento pelo cliente | O cliente deve continuar podendo arquivar contratações |
| Criar coluna booleana `arquivada` e restringir `status` ao ciclo de vida | Conceitos ortogonais independentes; preserva tanto o histórico de andamento quanto a preferência de arquivamento do cliente | Exige migration Flyway para adicionar a coluna e converter registros legados | Escolhida |

## Consequências

### Positivas

- O cliente pode arquivar e desarquivar contratações a qualquer momento sem alterar nem perder o status do serviço (`EM_ANDAMENTO`, `CONCLUIDO`, `CANCELADO`).
- O ciclo de vida da contratação fica íntegro e auditável sob responsabilidade do profissional.

### Negativas

- Exige nova migration Flyway para adicionar `arquivada` e migrar linhas existentes com `ATIVA`/`ARQUIVADA` para `EM_ANDAMENTO`.

### Neutras

- As rotas `PATCH /api/contratacao/{id}/arquivar` e `PATCH /api/contratacao/{id}/desarquivar` permanecem com a mesma URL e contrato de chamada.

## Impacto na Arquitetura

Esta decisão altera `docs/arquitetura-sistema.json`?

- [ ] Sim
- [x] Não — `ContratacaoStatusController` e `ContratacaoStatusService` já estavam previstos em `directory_structure.backend.src/main/java.modulos/.regra_de_pasta_vs_arquivo.exemplo_modulo_composto`.

## Referências

- [`docs/modelo-dados.dbml`](../modelo-dados.dbml)
- [`docs/modelo-dados.md`](../modelo-dados.md)
