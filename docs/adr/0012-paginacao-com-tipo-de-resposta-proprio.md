# ADR-0012 — Paginação com tipo de resposta próprio

| Campo | Valor |
|---|---|
| **ADR** | `0012` |
| **Título** | `PaginaResponse<T>` em `core/paginacao/`, não `Page<T>` do Spring Data exposto direto |
| **Autor** | Carlos Antunes |
| **Data** | `2026-09-14` |
| **Tópico** | Backend |
| **Status** | Aceito |
| **LLM utilizada** | `Claude Opus 5 (Claude Code)` |

---

## Contexto

RF009 exige listagem paginada, e o CRUD de serviços é o primeiro endpoint do projeto a precisar
de paginação. `docs/design-sistema.md` já reservava `core/paginacao/` desde a fundação do projeto
("tipos de paginação e ordenação padronizados da API"), mas nada tinha sido escrito ali —
nenhum módulo existente até agora precisou paginar uma listagem.

Havia duas formas razoáveis de resolver isso, e a escolha não é sobre serviços especificamente:
é sobre o contrato de paginação que **todo** módulo futuro com listagem (profissionais,
contratações) vai seguir.

## Decisão

**Criamos `PaginaResponse<T>` em `core/paginacao/`**, um `record` com `conteudo`, `pagina`,
`tamanho`, `totalElementos` e `totalPaginas`, com dois métodos estáticos `de(Page<T>)` e
`de(Page<E>, Function<E, T>)` para montá-lo a partir de um `Page` do Spring Data na borda do
service, antes de a resposta sair para o controller.

O controller de serviços expõe `GET /api/servico?pagina=&tamanho=`, público, e devolve
`PaginaResponse<ServicoResponse>` — nunca a entidade `Servico` nem o `Page` do Spring Data.

## Alternativas Consideradas

| Opção | Prós | Contras | Por que foi recusada |
|---|---|---|---|
| Expor `org.springframework.data.domain.Page<T>` direto no controller | Zero código novo | Vaza um tipo do Spring Data no contrato público da API; serialização do `Page` real inclui campos internos (`pageable`, `sort` como objeto) que não são o formato que o frontend deveria consumir; trocar Spring Data um dia vira migração de contrato | O contrato da API não deveria depender da biblioteca de persistência escolhida |
| `PaginaResponse<T>` próprio em `core/paginacao/` | Contrato estável e enxuto, independente do Spring Data; já é o que `design-sistema.md` descrevia desde a fundação | Mais um tipo para manter; quem monta a resposta precisa lembrar de mapear `Page` para `PaginaResponse` | Custo pequeno e pago uma vez; sela algo que já estava reservado na arquitetura |

## Consequências

### Positivas

- Todo módulo futuro com listagem paginada (profissionais, contratações) usa o mesmo contrato,
  sem redecidir isso a cada módulo.
- O contrato da API não muda se o projeto trocar Spring Data por outra camada de persistência.

### Negativas

- Um tipo a mais para manter em `core/paginacao/`, hoje com um único arquivo.

### Neutras

- Filtros de listagem (categoria, faixa de preço, localização — RF004) ficam fora desta decisão
  e desta sprint; `PaginaResponse<T>` não assume nada sobre eles.

## Impacto na Arquitetura

Esta decisão altera `docs/arquitetura-sistema.json`?

- [x] Sim — campo alterado: `backend.pagination` (novo)
- [ ] Não

## Referências

- [Relatório 2026-09-14](../relatorios/2026-09-14-crud-de-servicos-backend-e-listagem-no-front.md) — sessão em que serviços implementou o primeiro endpoint paginado
