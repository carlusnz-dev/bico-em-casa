# ADR-0014 — Acesso à contratação por participante e perfil público para os dois papéis

| Campo | Valor |
|---|---|
| **ADR** | `0014` |
| **Título** | A contratação é visível ao cliente **e** ao profissional dela; `GET /api/perfil/{id}` segue público para qualquer papel |
| **Autor** | Lucas Maiorquin |
| **Data** | `2026-11-01` |
| **Tópico** | Backend · Frontend |
| **Status** | Proposto |
| **LLM utilizada** | `Claude Sonnet 5.5 (Claude Code)` |

---

## Contexto

Até aqui o fluxo de contratação só existia do ponto de vista do cliente. No `ContratacaoServiceImpl`:

- `listarMinhas` exigia o perfil `CLIENTE` e filtrava por `clienteId`;
- `buscarPorId`, `editar`, `arquivar` e `desarquivar` passavam por `buscarContratacaoDoCliente`,
  que lança `ContratacaoNaoPertenceAoPerfilException` quando o perfil `CLIENTE` do usuário não é o
  `clienteId` da contratação;
- o `ContratacaoRepository` não tinha consulta por `profissionalId`, embora a coluna já fosse
  gravada em `contratar`.

Resultado: o profissional não conseguia listar nem abrir as contratações do próprio serviço e, por
isso, não tinha como chegar ao perfil do cliente que o contratou. No outro sentido, o cliente só
chegava ao perfil do profissional quando este tinha portfólio, porque a vitrine
(`/profissionais/[id]`) é indexada pelo id do **portfólio**, não do perfil.

O endpoint `GET /api/perfil/{id}` já é público (`permitAll` no `SecurityConfig`) e devolve o
`PerfilResponse` para qualquer `PerfilTipo`, inclusive `CLIENTE`. O `PerfilResponse` não expõe
telefone nem endereço.

## Decisão

1. **Leitura da contratação por participante.** `ContratacaoService.buscarPorId` passa a aceitar o
   cliente **ou** o profissional da contratação. O acesso é decidido comparando o `id` do perfil do
   usuário (lido de `perfil`, nunca de claim do JWT) com `clienteId` e `profissionalId`. Quem não é
   participante continua recebendo `ContratacaoNaoPertenceAoPerfilException`.
2. **Escrita continua só do cliente.** `editar`, `arquivar` e `desarquivar` não mudam e seguem
   usando `buscarContratacaoDoCliente`.
3. **Listagem do profissional.** Novo `GET /api/contratacao/recebidas`
   (`ContratacaoService.listarRecebidas`), paginado como `/minhas`, exige perfil `PROFISSIONAL` e
   usa `ContratacaoRepository.findByProfissionalId`.
4. **Consulta sem exceção no módulo de usuários.** `PerfilService.encontrarPorUsuarioETipo`
   devolve `Optional<PerfilResponse>`, para o módulo de contratações testar a participação sem
   capturar exceção e sem tocar no repository de `usuarios`.
5. **Perfil público para os dois papéis.** `GET /api/perfil/{id}` continua público e **não** é
   alterado; `PerfilResponse` não ganha campos. No frontend, a página `/perfil/[id]` mostra
   qualquer perfil e adapta os blocos ao `tipo` (nota e serviços só para `PROFISSIONAL`).
6. **Entrada de navegação.** O menu do profissional aponta "Contratações" para
   `/contratacoes/recebidas`; a tela de detalhe da contratação mostra os cards de cliente e de
   profissional, ambos com link para `/perfil/{id}`, e esconde editar, avaliar, denunciar e
   arquivar de quem não é o cliente.

## Alternativas Consideradas

| Opção | Prós | Contras | Por que foi recusada |
|---|---|---|---|
| Endpoint separado de detalhe para o profissional | Não altera a regra de acesso existente | Duplica rota, DTO e testes para o mesmo recurso | `buscarPorId` com dois papéis é mais simples e a escrita continua protegida |
| `GET /api/perfil/{id}` só para logados | Perfil de cliente deixa de ser visível a anônimos | Mexe no `SecurityConfig`; quebra a vitrine pública do profissional ou exige regra por tipo | Fora do escopo desta entrega; fica como evolução, ver Consequências |
| Página de perfil separada por papel (`/clientes/[id]`) | Cada tela evolui sozinha | Duas rotas para o mesmo recurso `perfil` | Uma rota única atende os dois e o dado vem do mesmo endpoint |
| Autorizar por claim de papel no JWT | Dispensa consulta ao banco | Proibido pelas regras do projeto: o papel é lido de `usuario`/`perfil` | Contraria a convenção do projeto |

## Consequências

### Positivas

- O profissional lista, abre e chega ao perfil do cliente; o cliente chega ao perfil do
  profissional mesmo sem portfólio.
- Nenhuma migration, nenhum DTO e nenhum schema Zod novo: `ContratacaoResponse` já tinha os dois ids.

### Negativas

- **O perfil de um cliente passa a ser alcançável por qualquer visitante anônimo que conheça o
  UUID** (nome de exibição, bio, foto, data de entrada). Isso já era verdade no endpoint; agora a
  interface passa a linkar para ele. Se o projeto decidir restringir, a mudança é no
  `SecurityConfig` e neste ADR.
- `buscarPorId` faz até duas consultas de perfil (uma por papel) antes de responder.

### Neutras

- O menu do profissional deixou de apontar para `/contratacoes` (que lista como cliente) e passa a
  apontar para `/contratacoes/recebidas`.
- Um usuário com os dois perfis continua enxergando, pela sessão, só o perfil devolvido por
  `/api/perfil/me`; as telas usam `perfil.id` da sessão para decidir se ele é o cliente.

## Impacto na Arquitetura

Esta decisão altera `docs/arquitetura-sistema.json`?

- [ ] Sim
- [x] Não — `arquitetura-sistema.json` e `design-sistema.md` descrevem a estrutura de pastas e as
  convenções; não catalogam endpoints nem rotas de tela, e nenhuma pasta ou fronteira entre módulos
  mudou. A listagem de arquivos de `modulos/contratacoes` no JSON já diverge do código (usa
  `service/` e `ContratacaoStatusController`) e não foi tocada aqui.

## Referências

- [ADR-0004](./0004-estrutura-modular-por-dominio.md) — contrato entre módulos só por interface
- [ADR-0009](./0009-perfil-id-uuid.md) — `perfil.id` como `uuid`
- [ADR-0012](./0012-paginacao-com-tipo-de-resposta-proprio.md) — `PaginaResponse<T>` usado em `/recebidas`
- Numeração: o commit `027a1f6` cita um ADR-0013 que não está nesta branch; o 0014 foi escolhido
  para não colidir com ele.
