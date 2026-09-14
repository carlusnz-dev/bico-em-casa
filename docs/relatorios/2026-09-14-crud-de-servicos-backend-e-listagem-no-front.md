# Relatório de Sessão — 2026-09-14

| Campo | Valor |
|---|---|
| **Sessão** | CRUD de serviços no backend e listagem pública paginada no frontend |
| **Autor** | Carlos Antunes |
| **Data** | `2026-09-14` |
| **Duração aproximada** | ~2h |
| **LLM utilizada** | `Claude Sonnet 5 (Claude Code)` |
| **Branch** | `feat/servicos` |
| **Commits** | A registrar no fechamento da sessão |
| **Plano relacionado** | Nenhum |

---

## Resumo

Sessão de limpeza + implementação. Começou fechando o que a sessão anterior deixou pendente:
`develop` foi atualizado para o merge do PR #9 (módulo de autenticação), e os ramos e worktree
locais já mesclados (`feat/modulo-autenticacao`, `merge/autenticacao-backend-para-develop`,
`feat/ux-autenticacao-home-notfound`) foram removidos.

A partir daí, `feat/servicos` foi recriada **do zero a partir de `develop`**, não a partir do
ramo remoto `origin/feat/servicos` — esse ramo divergiu em 2026-08-31, antes de todo o módulo de
autenticação e da reestruturação de `usuarios` existirem, e um rebase geraria conflito em arquivos
sem relação com serviços. Os arquivos de `feat/servicos` foram portados manualmente, corrigindo o
que a sessão anterior já tinha mapeado como incompatível com o estado atual: `perfil_id` como
`Long` (violava o [ADR-0009](../adr/0009-perfil-id-uuid.md)) virou `UUID`, resolvido via
`PerfilService.buscarPorUsuarioIdETipo(usuarioId, PROFISSIONAL)` — método novo, adicionado ao
contrato existente.

O CRUD ficou completo no backend (criar, listar, editar, ativar, desativar) e a listagem pública
paginada no frontend, usando `PaginaResponse<T>`, tipo criado nesta sessão em `core/paginacao/`
(reservado desde a fundação do projeto, nunca implementado). A migration Flyway das tabelas
`servico`, `tag` e `servico_tag` foi escrita pela LLM nesta sessão, por decisão explícita do
usuário que reverteu a decisão da sessão anterior de a equipe escrever a migration.

Tudo foi testado de ponta a ponta com o backend e o frontend realmente no ar — não só compilação e
typecheck: cadastro de dois profissionais de teste, criação de serviço, listagem pública,
edição, ativação/desativação, e o bloqueio de um profissional editar o serviço do outro.

## O que foi feito

- `develop` local atualizado para `3257790` (merge do PR #9); ramos e worktree já mesclados
  removidos (`feat/modulo-autenticacao`, `merge/autenticacao-backend-para-develop`,
  `feat/ux-autenticacao-home-notfound`)
- Módulo `servicos` criado no backend: `Servico`, `Tag`, `UnidadePreco`, `ServicoRequest`,
  `ServicoResponse`, `ServicoRepository`, `TagRepository`, `ServicoService` (contrato),
  `ServicoServiceImpl`, `ServicoController`
- `PerfilService.buscarPorUsuarioIdETipo(Long, PerfilTipo)` adicionado ao contrato de `usuarios`
  e implementado em `PerfilServiceImpl` — reaproveita `PerfilRepository.findByUsuarioIdAndTipo`,
  que já existia mas não estava exposto pelo contrato
- `ServicoNaoPertenceAoPerfilException` criada em `core/excecao/` e wireada em
  `ExcecoesGlobalHandler` (403, RFC 9457) — cobre editar/ativar/desativar serviço de outro perfil
- `PaginaResponse<T>` criado em `core/paginacao/`, com `de(Page<T>)` e `de(Page<E>, Function<E,T>)`
  — primeiro conteúdo real da pasta, reservada desde a fundação do projeto
- `SecurityConfig` liberou `GET /api/servico` e `/api/servico/**` como público; os demais métodos
  continuam exigindo autenticação pela regra default (`anyRequest().authenticated()`)
- Migration `V20260913223500__criar_tabelas_servico_e_tag.sql` — três tabelas, respeitando
  [ADR-0009](../adr/0009-perfil-id-uuid.md) (`perfil_id UUID`) e
  [ADR-0010](../adr/0010-versionamento-migration-por-timestamp.md) (nome por timestamp; é a
  primeira migration do projeto a usar esse formato)
- Frontend: `src/api/contratos/pagina.ts` (schema Zod genérico de paginação),
  `src/api/contratos/servico.ts`, `src/api/servicos.ts` (`listarServicos`), e
  `src/app/(publico)/servicos/page.tsx` — listagem pública, Server Component assíncrono lendo
  `searchParams` (`Promise` nesta versão do Next), com paginação por link
- [ADR-0012](../adr/0012-paginacao-com-tipo-de-resposta-proprio.md) — `PaginaResponse<T>` em vez
  de expor `Page<T>` do Spring Data direto; `docs/arquitetura-sistema.json` e
  `docs/design-sistema.md` atualizados na mesma sessão
- `docs/relatorios/2026-09-14-merge-autenticacao-em-develop-e-mapeamento-de-servicos.md` —
  reconstrução do relatório da sessão anterior, que foi cortada pelo limite de uso antes de
  escrevê-lo

## Decisões tomadas

| Decisão | Motivo | ADR |
|---|---|---|
| `feat/servicos` recriada do zero a partir de `develop`, não rebaseada do ramo remoto | O ramo remoto divergiu antes do módulo de autenticação existir; rebase geraria conflito massivo em arquivos sem relação com serviços | Não requer |
| Recorte do CRUD: criar, listar, editar, ativar/desativar (sem exclusão física) | Decisão do usuário na sessão anterior | Não requer |
| Listagem pública e paginada | Decisão do usuário; atende RF009 | Não requer |
| `PaginaResponse<T>` próprio em vez de `Page<T>` do Spring Data exposto direto | Decisão do usuário entre duas opções com trade-off | [ADR-0012](../adr/0012-paginacao-com-tipo-de-resposta-proprio.md) |
| Ativar/desativar como endpoints dedicados (`PATCH .../ativar`, `.../desativar`), não campo no PUT de edição | Decisão do usuário; separa intenção de negócio (mudar visibilidade) de edição de dados | Não requer |
| Migration escrita pela LLM nesta sessão | Decisão explícita do usuário ("você cria as migrations"), revertendo a decisão da sessão anterior de a equipe escrever. Registrado aqui para constar na revisão do PR | Não requer — mas é uma exceção à Regra nº 3 que precisa ser sinalizada no PR |
| `ServicoNaoPertenceAoPerfilException` nova, em vez de reaproveitar `CadastroNaoPermitidoException` | Mensagens e contexto diferentes; segue o padrão existente de uma exceção por violação específica | Não requer |
| Frontend desta sprint cobre só listagem, não os formulários de criar/editar | Escopo explícito do pedido ("CRUD no backend, listagem no frontend") | Não requer |

## Arquivos alterados

| Arquivo | Alteração |
|---|---|
| `backend/.../modulos/servicos/models/{Servico,Tag,UnidadePreco}.java` | Criados |
| `backend/.../modulos/servicos/dto/{ServicoRequest,ServicoResponse}.java` | Criados |
| `backend/.../modulos/servicos/repository/{ServicoRepository,TagRepository}.java` | Criados |
| `backend/.../modulos/servicos/{ServicoService,ServicoServiceImpl,ServicoController}.java` | Criados |
| `backend/.../core/paginacao/PaginaResponse.java` | Criado |
| `backend/.../core/excecao/ServicoNaoPertenceAoPerfilException.java` | Criado |
| `backend/.../core/ExcecoesGlobalHandler.java` | Alterado — handler para `ServicoNaoPertenceAoPerfilException` (403) |
| `backend/.../modulos/usuarios/contrato/PerfilService.java` | Alterado — `buscarPorUsuarioIdETipo(Long, PerfilTipo)` |
| `backend/.../modulos/usuarios/services/PerfilServiceImpl.java` | Alterado — implementação do método novo |
| `backend/.../config/SecurityConfig.java` | Alterado — `GET /api/servico` e `/api/servico/**` públicos |
| `backend/src/main/resources/db/migration/V20260913223500__criar_tabelas_servico_e_tag.sql` | Criado |
| `frontend/src/api/contratos/pagina.ts` | Criado |
| `frontend/src/api/contratos/servico.ts` | Criado |
| `frontend/src/api/servicos.ts` | Criado |
| `frontend/src/app/(publico)/servicos/page.tsx` | Criado |
| `docs/arquitetura-sistema.json` | Alterado — `backend.pagination` |
| `docs/design-sistema.md` | Alterado — §3.4 Paginação |
| `docs/adr/0012-paginacao-com-tipo-de-resposta-proprio.md` | Criado |
| `docs/adr/README.md` | Alterado — índice |
| `docs/relatorios/2026-09-14-merge-autenticacao-em-develop-e-mapeamento-de-servicos.md` | Criado |

## Verificações executadas

| Comando | Resultado |
|---|---|
| `./mvnw -q compile` | ✅ passou |
| `npm run typecheck` | ✅ passou |
| `npm run build` | ✅ passou — `/servicos` listado como rota dinâmica (`ƒ`) |
| `docker compose ps` | ✅ Postgres 18 e MinIO já no ar |
| Backend com `SPRING_PROFILES_ACTIVE=dev` | ✅ subiu — Flyway aplicou a migration nova, Hibernate validou o schema |
| `curl GET /api/servico` (sem auth) | ✅ `200` — lista paginada vazia, depois com itens |
| `curl POST /api/autenticacao/cadastrar` (2 profissionais + 1 cliente de teste) | ✅ `201` cada |
| `curl POST /api/servico` (profissional autenticado) | ✅ `201` — `perfilId` correto (UUID do perfil, não o `usuarioId`) |
| `curl POST /api/servico` (cliente, não profissional) | ✅ `400` — `EntidadeNaoEncontradaException("Perfil não encontrado")`, bloqueio confirmado |
| `curl PATCH /api/servico/{id}/desativar` e `/ativar` | ❌ na primeira tentativa (`500`, ver Problemas) → ✅ `200` depois da correção |
| `curl PUT /api/servico/{id}` | ✅ `200` — título, preço e unidade atualizados |
| `curl PATCH .../desativar` de um perfil sobre serviço de outro | ✅ `403` — `ServicoNaoPertenceAoPerfilException` |
| `curl http://localhost:3000/servicos` | ✅ `200`, títulos dos serviços presentes no HTML renderizado |
| `./mvnw test` | ⏭️ não executado — já quebrado desde 09-11 (NPE em `SecurityConfig.jwtEncoder()` sem profile), não tocado nesta sessão |

## Problemas encontrados

- **`LazyInitializationException` em `ativar`/`desativar`/`listarAtivos`.** `Servico.tags` é
  `@ManyToMany` preguiçoso (padrão), e `open-in-view: false` fecha a sessão do Hibernate assim que
  o repository retorna. `criar`/`editar` não sentiam o problema porque substituem `tags` por um
  `HashSet` novo antes de montar a resposta — deixam de depender do proxy. `ativar`, `desativar` e
  `listarAtivos` liam o proxy depois da sessão fechada. Corrigido com `@Transactional` (e
  `@Transactional(readOnly = true)` na listagem) nesses três métodos, mantendo a sessão aberta até
  a resposta ser montada.
- **`pkill -f` matou o próprio comando duas vezes.** O padrão passado para `pkill -f` continha o
  texto `spring-boot:run`, que também aparece na linha de comando do processo `bash` que executa o
  próprio `pkill` dentro do harness — ele se autoderrubou antes de atingir o alvo. Contornado
  matando por PID explícito (`kill <pid>`) em vez de por padrão de texto.
- **Dois servidores (backend e frontend) da sessão de 09-13 continuavam no ar**, ocupando as
  portas 8080 e 3000, com código anterior ao módulo de serviços. Identificados via `ss -ltnp` e
  `ps aux`, encerrados antes de subir as versões atuais.
- **Nenhum endpoint de Tag existe.** Para testar `criar`, uma tag de teste foi inserida direto via
  `psql` (`INSERT INTO tag ...`). Isso não é um problema desta sessão — só não estava no escopo
  pedido — mas bloqueia qualquer fluxo real de cadastro de serviço até existir.

## Pendências

- [ ] **CRUD de `Tag` não existe.** Sem ele, cadastrar um serviço de verdade exige inserir
      categorias direto no banco. Não estava no escopo desta sessão
- [ ] RF004 (filtro de busca por categoria, faixa de preço e localização) não foi implementado —
      fora do recorte combinado para esta sprint
- [ ] Dados de teste ficaram no Postgres local (2 profissionais, 1 cliente, 1 tag, 3 serviços) —
      sem risco, mas quem for demonstrar o fluxo do zero pode preferir limpar antes
- [ ] `CLAUDE.md` da raiz do projeto ainda descreve a pasta núcleo compartilhado como `comum/`;
      o código e os dois documentos de arquitetura já usam `core/` de forma consistente desde
      antes desta sessão. Não corrigido — é edição de documentação fora do escopo pedido
- [ ] Pendências já conhecidas de `autenticacao`/`usuarios`, não tocadas nesta sessão:
      `EntidadeNaoEncontradaException` devolve `400` em vez de `404`; `UsuarioServiceImpl` lança
      `RuntimeException` genérica para senha curta; `PerfilController` vazio; `POST /api/usuario`
      público permite criar `ADMIN`; `./mvnw test` quebrado

## Próximos passos

1. CRUD de `Tag` (equipe, é regra de negócio simples e boa entrada para quem ainda não mexeu no
   projeto)
2. Formulários de criar/editar serviço no frontend — ficaram fora do escopo desta sessão
3. Revisar as pendências de `autenticacao`/`usuarios` listadas acima antes que acumulem mais dívida
4. Abrir PR de `feat/servicos` contra `develop`, registrando no PR a exceção à Regra nº 3 (migration
   escrita pela LLM por decisão explícita do usuário)
