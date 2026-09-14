# Relatório de Sessão — 2026-09-14

| Campo | Valor |
|---|---|
| **Sessão** | PBI "Denunciar serviço/contratação" (RF020) — merge do trabalho do Lucas e refactor do módulo `denuncias` |
| **Autor** | Carlos Antunes |
| **Data** | `2026-09-14` |
| **Duração aproximada** | `~3h` |
| **LLM utilizada** | `Claude Sonnet 5 (Claude Code)` |
| **Branch** | `feat/denuncia` |
| **Commits** | `7c8347b` (merge, 5 commits de Lucas preservados), `151d480`, `a99ab3b`, `a07c5e1` |
| **Plano relacionado** | Nenhum |

---

## Resumo

Sessão de continuação: com o PR #12 (`feat/avaliacao` → `develop`) já mergeado, o objetivo era o
PBI **Denunciar serviço/contratação** (RF020), cujo CRUD de backend já tinha sido escrito pelo
Lucas Cobucci Babula Alves (`srlucascobucci@gmail.com`) na branch remota `origin/feat/denuncia`,
com 5 commits e sem frontend. O pedido explícito do usuário foi preservar esses commits — mesmo
padrão usado nas sessões anteriores com Tayllor e Thiago.

Diferente da sessão de `avaliacoes`, o usuário liberou de antemão, para esta sessão, a exceção à
Regra nº 3 (a LLM escreve código de aplicação) e pediu que as decisões de arquitetura fossem
resolvidas pela LLM e só apresentadas ao final, em vez de pausar a cada ponto com
`AskUserQuestion` como na sessão anterior. O usuário já tinha adiantado, na própria descrição da
task, os pontos reais de incompatibilidade a esperar: módulo errado (`avaliacoes` em vez de
`denuncias`), migration `V4` sequencial (viola ADR-0010), enums como `ENUM` nativo do Postgres
(nenhuma outra tabela do projeto usa isso), `contratacao_id` sem FK, uma `RegraNegocioException`
genérica, e um teste (`DenunciaServiceImplTest`, 187 linhas) de autoria e cobertura incertas.

A investigação confirmou todos os pontos e revelou mais dois não antecipados: o controller do
Lucas confiava a identidade do autor a um header `X-Perfil-id` enviado pelo cliente, sem qualquer
validação contra o JWT — qualquer chamador podia se passar por outro perfil; e o método de posse
(`buscarDoAutor`) devolvia 404 para "não é seu" em vez do 403 nomeado que `Avaliacao` e
`Contratacao` já usam para o mesmo caso. Com a exceção de Regra nº 3 já concedida, a sessão seguiu
mecânica: merge preservando os hashes do Lucas, criação do módulo `modulos/denuncias` na estrutura
composta, correção de autorização via JWT + `PerfilService`, conversão dos enums para
`VARCHAR` + `@Enumerated(STRING)`, duas exceções nomeadas por regra, FK que faltava, migration
renomeada por timestamp, teste reescrito para as assinaturas novas, e um frontend novo (a branch
do Lucas não tinha nenhum) com escopo restrito a criar denúncia a partir de uma contratação.

Compilação, testes (12/12), typecheck e build foram verificados, e o fluxo completo foi validado
via `curl` contra o backend real rodando com o profile `dev` — criação, formato inválido de
`alvoId`, autenticação, posse (403), não encontrado (404), listagem "minhas", atualização,
listagem e análise por admin, e o conflito 409 de denúncia já julgada.

## O que foi feito

- Branch `feat/denuncia` criada a partir de `develop`; os 5 commits de `srlucascobucci` (branch
  `origin/feat/denuncia`) preservados via `git merge --no-ff` (hashes originais intactos)
- Módulo `modulos/denuncias` criado na estrutura composta (`contrato/`, `controller/`, `service/`,
  `repository/`, `models/`, `dto/`), separado de `modulos/avaliacoes` onde o Lucas tinha colocado
  as classes
- Autorização corrigida: controller passa a extrair `usuarioId` do JWT (`@AuthenticationPrincipal
  Jwt`) e o service resolve o perfil via `PerfilService`, no lugar do header `X-Perfil-id`
  confiado sem validação — falha de segurança real, não apenas convenção
- Enums `TipoAlvoDenuncia`/`StatusDenuncia` migrados de `ENUM` nativo do Postgres
  (`@JdbcTypeCode(SqlTypes.NAMED_ENUM)`) para `VARCHAR` + `@Enumerated(EnumType.STRING)`,
  consistente com `StatusContratacao` — nenhuma outra tabela do projeto usava o padrão nativo
- `RegraNegocioException` genérica removida; substituída por `DenunciaNaoPertenceAoPerfilException`
  (403) e `DenunciaNaoPendenteException` (409), no padrão já usado por `Avaliacao`/`Contratacao`
- Verificação de posse em `buscarPorId`/`atualizar`/`excluir` corrigida de 404 (comportamento
  original do Lucas) para 403, consistente com `AvaliacaoNaoPertenceAoPerfilException` e
  `ContratacaoNaoPertenceAoPerfilException`
- Validação de formato do `alvoId` movida de exceção de serviço para `@Pattern` no DTO
  (`DenunciaRequest`), coerente com o resto da validação de entrada do projeto
- `contratacao_id` ganhou a FK que faltava (`fk_denuncia_contratacao`, `ON DELETE SET NULL` — por
  ser vínculo opcional; as demais FKs de perfil usam `ON DELETE CASCADE`, alinhadas com
  `avaliacao`/`contratacao`)
- Migration renomeada de `V4__criar_tabela_denuncia.sql` (sequencial, viola ADR-0010) para
  `V20260914050733__criar_tabela_denuncia.sql`
- Campo `fotos` (lista de URLs, `JSONB`) do desenho original do Lucas preservado — não fazia parte
  dos pontos de incompatibilidade levantados, então não foi removido por conta própria
- `DenunciaServiceImplTest` reescrito para as assinaturas novas (12 testes, todos passando):
  continua teste de unidade com Mockito (não precisa de Testcontainers, não toca banco); um teste
  novo cobre a validação `@Pattern` do `alvoId` via `jakarta.validation.Validator` puro
- Frontend criado do zero (a branch do Lucas não tinha nenhum): contrato Zod em
  `src/api/contratos/denuncia.ts`, cliente em `src/api/denuncias.ts`, `FormDenuncia` com
  radiogroup "profissional ou serviço" (mesma convenção visual do `FormCadastro`), rota
  `(publico)/contratacoes/[id]/denunciar`, link "Denunciar" no detalhe da contratação
- `docs/arquitetura-sistema.json` e `docs/design-sistema.md` atualizados no mesmo commit
  (`modulos_v1`/estrutura de pastas ganham `denuncias`) — Regra nº 1
- `docs/requisitos.json`, `docs/requisitos.md` e `docs/matriz-rastreabilidade.md` atualizados:
  RF020 passa a citar o módulo `denuncias`; matriz regenerada via `/revisar-matriz` (tabelas 1 e 2
  via `jq`, seção 3 com um parágrafo novo sobre o módulo)

## Decisões tomadas

| Decisão | Motivo | ADR |
|---|---|---|
| Regra nº 3 liberada de antemão para toda a sessão, com decisões de arquitetura resolvidas pela LLM e revisadas só no relatório final, em vez de `AskUserQuestion` a cada ponto | Instrução explícita do usuário no início da sessão ("apenas faça... decisões ficam para o final") | Não requer — exceção a sinalizar sempre que se repetir |
| Enums de `denuncia` convertidos de `ENUM` nativo do Postgres para `VARCHAR` + `@Enumerated(STRING)` | O próprio usuário identificou como decisão de arquitetura com mais de uma solução defensável; resolvida a favor do padrão já vigente no projeto (`StatusContratacao`) para não introduzir um segundo padrão de mapeamento de enum sem ganho concreto | Não requer |
| `RegraNegocioException` genérica substituída por exceções nomeadas por regra | O usuário sinalizou como decisão de convenção; resolvida a favor do padrão já vigente (`AvaliacaoJaExisteException`, `ContratacaoNaoPertenceAoPerfilException` etc.), que já cobre exatamente os dois casos de uso do Lucas (posse e status) | Não requer |
| Autorização via header `X-Perfil-id` substituída por JWT + `PerfilService` | Não é decisão de preferência — o header confiado sem validação permite qualquer chamador se passar por qualquer perfil; corrigido para o único padrão de autenticação que existe no projeto | Não requer |
| Verificação de posse (`buscarPorId`/`atualizar`/`excluir`) mudada de 404 para 403 | Consistência com `Avaliacao`/`Contratacao`, que já resolvem exatamente esse caso com exceção nomeada 403; manter 404 teria criado um terceiro comportamento para o mesmo cenário dentro do próprio projeto | Não requer |
| `contratacao_id` ganhou FK com `ON DELETE SET NULL` | Coluna nullable e vínculo opcional (denúncia pode mirar perfil/serviço sem contratação associada); `SET NULL` evita que a denúncia vire órfã de FK sem impedir a exclusão da contratação | Não requer |
| Frontend restrito a criar denúncia a partir de uma contratação (perfil do profissional ou serviço) | Escopo mínimo que cobre o caso de uso mais concreto do RF020; "minhas denúncias", painel de análise do admin e denunciar uma avaliação diretamente ficam de fora — o backend já suporta os três, só falta tela | Não requer |
| Relatório mantém registro factual de que o refactor de backend, a migration e todo o frontend foram escritos pela LLM | Mesma justificativa das duas sessões anteriores (`contratacoes`, `avaliacoes`): o relatório é o registro acadêmico de autoria, e a Regra nº 3 existe para que essa autoria fique visível, mesmo quando liberada por exceção | Não requer — reafirma a regra já registrada |

## Arquivos alterados

| Arquivo | Alteração |
|---|---|
| `backend/.../modulos/avaliacoes/{DenunciaController,DenunciaService,DenunciaServiceImpl,DenunciaRepository}.java`, `.../models/{Denuncia,StatusDenuncia,TipoAlvoDenuncia}.java`, `.../dto/Denuncia*.java` | Removidos (movidos para `modulos/denuncias`) |
| `backend/.../modulos/denuncias/contrato/DenunciaService.java` | Criado — assinaturas com `Long usuarioId` no lugar de `UUID perfilId` |
| `backend/.../modulos/denuncias/controller/DenunciaController.java` | Criado — rota `/api/denuncia`, `@AuthenticationPrincipal Jwt` |
| `backend/.../modulos/denuncias/service/DenunciaServiceImpl.java` | Criado — autorização via `PerfilService`, exceções nomeadas |
| `backend/.../modulos/denuncias/repository/DenunciaRepository.java` | Criado (só pacote novo) |
| `backend/.../modulos/denuncias/models/Denuncia.java` | Criado — enums como `VARCHAR`/`STRING`, `fotos` preservado |
| `backend/.../modulos/denuncias/models/{StatusDenuncia,TipoAlvoDenuncia}.java` | Criado/movido (só pacote novo) |
| `backend/.../modulos/denuncias/dto/DenunciaRequest.java` | Criado — `@Pattern` no `alvoId` |
| `backend/.../modulos/denuncias/dto/{DenunciaResponse,DenunciaAtualizacaoRequest,DenunciaAnaliseRequest}.java` | Criado (pacote novo, mensagens de validação corrigidas) |
| `backend/.../core/excecao/RegraNegocioException.java` | Removido |
| `backend/.../core/excecao/{DenunciaNaoPertenceAoPerfilException,DenunciaNaoPendenteException}.java` | Criados |
| `backend/.../core/ExcecoesGlobalHandler.java` | Alterado — handler genérico removido, 2 handlers novos |
| `backend/src/main/resources/db/migration/V4__criar_tabela_denuncia.sql` | Removido |
| `backend/src/main/resources/db/migration/V20260914050733__criar_tabela_denuncia.sql` | Criado — `VARCHAR` no lugar de `CREATE TYPE`, FK de `contratacao_id` adicionada |
| `backend/src/test/java/.../modulos/{avaliacoes → denuncias}/DenunciaServiceImplTest.java` | Reescrito para as assinaturas novas; teste de validação do `alvoId` adicionado |
| `frontend/src/api/contratos/denuncia.ts` | Criado |
| `frontend/src/api/denuncias.ts` | Criado |
| `frontend/src/components/forms/FormDenuncia.tsx` | Criado |
| `frontend/src/app/(publico)/contratacoes/[id]/denunciar/page.tsx` | Criado |
| `frontend/src/app/(publico)/contratacoes/[id]/page.tsx` | Alterado — link "Denunciar" |
| `docs/arquitetura-sistema.json`, `docs/design-sistema.md` | Alterados — módulo `denuncias` em `modulos_v1` e na árvore de pastas |
| `docs/requisitos.json`, `docs/requisitos.md` | Alterados — RF020 cita `denuncias`; `_meta.modulos_validos` ganha `denuncias` |
| `docs/matriz-rastreabilidade.md` | Regenerado (tabelas 1 e 2), seção 3 com parágrafo novo, data de sincronização atualizada |

## Verificações executadas

| Comando | Resultado |
|---|---|
| `git merge origin/feat/denuncia --no-ff` | ✅ mesclou sem conflito real (`ExcecoesGlobalHandler.java` teve merge automático limpo) |
| `./mvnw -q clean compile` (backend) | ✅ passou |
| `./mvnw -q test-compile` (backend) | ✅ passou |
| `./mvnw -q test -Dtest=DenunciaServiceImplTest` | ✅ 12/12 passou |
| `npm run typecheck` (frontend) | ✅ passou |
| `npm run build` (frontend) | ✅ passou — rota `/contratacoes/[id]/denunciar` listada |
| `npm run lint` (frontend) | ⏭️ não executado — quebrado por incompatibilidade pré-existente TS 7.0/`typescript-eslint`, não relacionada a esta sessão |
| Backend real (`spring-boot:run`, profile `dev`) | ✅ subiu limpo; Flyway aplicou `V20260914050733` com sucesso |
| `curl POST /api/denuncia` (criar, cliente denunciando o serviço) | ✅ `201` |
| `curl POST /api/denuncia` (`alvoId` não-UUID) | ✅ `400`, erro de validação no campo `alvoId` |
| `curl POST /api/denuncia` (sem token) | ✅ `401` |
| `curl GET /api/denuncia/{id}` (autor) | ✅ `200` |
| `curl GET /api/denuncia/{id}` (outro perfil) | ✅ `403` "Esta denúncia não pertence ao seu perfil" |
| `curl GET /api/denuncia/{id-inexistente}` | ✅ `404` "Denúncia não encontrada" |
| `curl GET /api/denuncia/minhas` (autor) | ✅ `200`, página com 1 item |
| `curl PUT /api/denuncia/{id}` (autor, pendente) | ✅ `200` |
| `curl GET /api/denuncia` (não-admin) | ✅ `404` "Perfil não encontrado" — mesmo comportamento já usado por `ContratacaoServiceImpl`/`AvaliacaoServiceImpl` para papel ausente |
| `curl GET /api/denuncia` (admin) | ✅ `200` |
| `curl POST /api/denuncia/{id}/analise` (admin, `PROCEDENTE`) | ✅ `200` |
| `curl DELETE /api/denuncia/{id}` (autor, já analisada) | ✅ `409` "Apenas denúncias pendentes podem ser..." |
| `curl POST /api/denuncia/{id}/analise` (`resultado: PENDENTE`) | ✅ `400`, `@AssertTrue` barrou |
| Dados de teste (usuários, perfis, serviço, contratação, denúncia) | ✅ removidos do banco ao final via `psql` |
| Frontend testado no navegador pelo usuário | ⏭️ não executado nesta sessão — validado só via `build`/`typecheck` e pelos testes de API do backend |
| `./mvnw test` (suíte completa) | ⏭️ não executado — já quebrado desde antes desta sessão (NPE em `SecurityConfig.jwtEncoder()` sem profile), não relacionado a este módulo |

## Problemas encontrados

- **Processo backend antigo (PID 361225) ocupava a porta 8080**, sobrando de antes desta sessão,
  rodando bytecode sem o módulo `denuncias`. Mesmo sintoma já registrado no relatório de
  `avaliacoes`; resolvido encerrando o processo antigo e subindo de novo.
- **Header de autorização confiado sem validação.** O controller do Lucas lia `X-Perfil-id`
  direto do request e usava como identidade do autor, sem checar contra o JWT — qualquer chamador
  autenticado como qualquer usuário podia informar o perfil de outra pessoa e agir em nome dela.
  Não estava na lista de pontos que o usuário já esperava; apareceu na leitura do
  `DenunciaController` original. Corrigido junto com o resto do refactor de autorização.
- **Sem usuário `ADMIN` de teste disponível** (cadastro público bloqueia `cadastroTipo: ADMIN`,
  corretamente). Para validar `listarParaAdmin`/`analisar` fim a fim, o perfil profissional de
  teste foi promovido a `ADMIN` via `UPDATE` direto no Postgres, só durante a verificação, e
  removido junto com o resto dos dados de teste ao final.

## Pendências

- [ ] Validação interativa do frontend no navegador (radiogroup de alvo, envio da denúncia) — não
      feita nesta sessão, só `build`/`typecheck` e API via `curl`
- [ ] Telas de "minhas denúncias", denunciar uma avaliação diretamente e painel de análise do
      admin não foram construídas — o backend já suporta as três, falta só a tela (decisão de
      escopo desta sessão, ver "Decisões tomadas")
- [ ] `docs/matriz-rastreabilidade.md` seção 4 ("Rastro para código") continua com o texto antigo
      ("não existe código de aplicação no repositório"), que já não é verdade desde a fundação de
      `usuarios`/`autenticacao` — não é uma pendência criada nesta sessão, mas foi notada durante
      a regeneração da matriz e não foi corrigida por estar fora do escopo do PBI de denúncia
      (exigiria auditar rastro de todos os 46 requisitos, não só o RF020)
- [ ] `./mvnw test` (suíte completa) continua quebrado, pendência já conhecida de sessões
      anteriores, não tocada nesta
- [ ] `npm run lint` quebrado por incompatibilidade `typescript-eslint`/TypeScript 7.0, pendência
      pré-existente não tocada nesta sessão

## Próximos passos

1. Usuário validar o fluxo de denúncia no navegador (escolher profissional ou serviço, enviar,
   conferir o link na página da contratação)
2. Abrir PR de `feat/denuncia` → `develop` após validação
3. Considerar um ADR pontual para `docs/matriz-rastreabilidade.md` §4 (rastro para código) antes
   que a lacuna cresça mais — hoje o documento afirma algo que não é mais verdade há várias sessões
4. Avaliar, com o time, se vale abrir uma tela de admin para `listarParaAdmin`/`analisar`, já que
   o backend inteiro para isso já existe e está testado
