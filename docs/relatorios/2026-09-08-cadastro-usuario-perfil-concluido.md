# Relatório de Sessão — 2026-09-08

| Campo | Valor |
|---|---|
| **Sessão** | Conclusão do cadastro combinado Usuario+Perfil, resolução de dependência circular entre services e primeiro teste ponta a ponta |
| **Autor** | Carlos Antunes |
| **Data** | `2026-09-08` |
| **Duração aproximada** | ~2h |
| **LLM utilizada** | `Claude Sonnet 5 (Claude Code)` |
| **Branch** | `feat/modulo-autenticacao` |
| **Commits** | Nenhum |
| **Plano relacionado** | Nenhum |

---

## Resumo

Sessão retomou o WIP descrito em `2026-09-07-cadastro-usuario-perfil-em-andamento.md`: cadastro
combinado de `Usuario` + `Perfil` num único endpoint, que na sessão anterior não compilava. O
desenvolvedor corrigiu, ao longo da sessão e com revisão linha a linha da LLM, uma sequência de
problemas reais que só apareceram ao tentar de fato ligar as duas entidades: `Perfil.usuario` é
uma referência de objeto (`@ManyToOne`), não um `Long`, então o `PerfilServiceImpl.criar()`
precisou de um jeito de montar essa referência a partir do `usuarioId` sem round-trip ao banco.
A primeira solução (expor `Usuario referenciaPorId(Long id)` no contrato `UsuarioService`, para
`PerfilServiceImpl` chamar) introduziu uma dependência circular entre os beans
`UsuarioServiceImpl` e `PerfilServiceImpl` — o Spring bloqueou a subida da aplicação por padrão.
A correção final foi `PerfilServiceImpl` injetar `UsuarioRepository` diretamente (mesmo módulo,
não fere a regra de fronteira entre módulos) e usar `getReferenceById(usuarioId)` ali mesmo,
eliminando o método `referenciaPorId` do contrato.

Outros dois problemas de correção foram identificados e corrigidos durante a sessão: `tipo` do
`Perfil` (`NOT NULL` no banco) não estava sendo setado em `PerfilServiceImpl.criar()`, e a ordem
de chamadas em `UsuarioServiceImpl.criar()` tinha `perfilService.criar()` sendo chamado *antes*
de `repository.save(usuarioNovo)` — como `Usuario.id` usa `GenerationType.IDENTITY`, o id só
existe depois do `INSERT`, então a chamada original passaria `null` para o `Perfil`.

Além disso, a tentativa de subir o backend localmente expôs uma lacuna de sessões anteriores:
existe uma entidade `RefreshToken` mapeada em código (módulo `autenticacao`, sessão de
`3a2a026a` — feature de refresh token) sem migration Flyway correspondente. O Hibernate barrava
a subida por falha de `Schema validation: missing table [refresh_token]`. O desenvolvedor
escreveu a migration `V3__criar-tabela-refresh-token.sql`; a LLM não escreveu SQL de migration,
por ser código de aprendizado de banco reservado à equipe (Regra nº 3 do `CLAUDE.md`).

Com os quatro problemas resolvidos, o backend subiu limpo com profile `dev` e o cadastro
combinado foi testado de fato via `curl` contra o Postgres real do `docker-compose` — não só
compilado: `POST /api/usuario` com payload `{ ...dados do usuário, perfil: {...} }` retornou
`201 Created`, e a consulta direta ao banco confirmou usuário e perfil persistidos na mesma
transação.

## O que foi feito

- Cadastro combinado (`POST /api/usuario`, payload único com `usuario` + `perfil` aninhado)
  compila, sobe e persiste as duas entidades na mesma transação — testado com `curl` contra
  Postgres real, não só compilado
- Dependência circular entre `UsuarioServiceImpl` e `PerfilServiceImpl` eliminada: `PerfilService`
  passou a depender de `UsuarioRepository` direto (mesmo módulo) em vez do contrato
  `UsuarioService`
- `PerfilServiceImpl.criar()` corrigido: `tipo` agora é setado a partir do `request`, a referência
  a `Usuario` é montada via `getReferenceById` sem round-trip ao banco
- `UsuarioServiceImpl.criar()` corrigido: ordem de `repository.save(usuarioNovo)` antes de
  `perfilService.criar(...)`, agora dentro de `@Transactional` (primeira ocorrência da anotação
  no projeto)
- `UsuarioRequest` ganhou o campo aninhado `@NotNull @Valid PerfilRequest perfil`, fechando a
  decisão de payload único já registrada na sessão anterior
- Migration `V3__criar-tabela-refresh-token.sql` escrita pelo desenvolvedor, cobrindo a lacuna de
  schema que impedia a aplicação de subir

## Decisões tomadas

| Decisão | Motivo | ADR |
|---|---|---|
| `PerfilServiceImpl` injeta `UsuarioRepository` diretamente em vez de depender do contrato `UsuarioService` | Quebrar a dependência circular `UsuarioServiceImpl ↔ PerfilServiceImpl`; viável porque `Usuario` e `Perfil` são do mesmo módulo (`modulos/usuarios/`), não fere a regra de não injetar repository de outro módulo | Não requer |
| Método `UsuarioService.referenciaPorId(Long id)` removido do contrato | Ficou sem uso após a decisão acima; expunha entidade JPA num contrato público sem necessidade | Não requer |

## Arquivos alterados

| Arquivo | Alteração |
|---|---|
| `backend/.../modulos/usuarios/contrato/PerfilService.java` | Criado — assinatura `criar(Long usuarioId, PerfilRequest request)` |
| `backend/.../modulos/usuarios/contrato/UsuarioService.java` | Alterado — `criar` volta a um parâmetro único (`UsuarioRequest`); `referenciaPorId` removido |
| `backend/.../modulos/usuarios/controller/UsuarioController.java` | Alterado — mantido `criar(UsuarioRequest)`, agora batendo com o contrato de novo |
| `backend/.../modulos/usuarios/dto/UsuarioExiste.java` | Criado — record `(boolean existe, boolean ativo)` |
| `backend/.../modulos/usuarios/dto/UsuarioRequest.java` | Alterado — campo `@NotNull @Valid PerfilRequest perfil` adicionado |
| `backend/.../modulos/usuarios/repository/PerfilRepository.java` | Alterado — `findByNomeExibicao`, `existsByNomeUsuario` |
| `backend/.../modulos/usuarios/repository/UsuarioRepository.java` | Alterado — `findAtivoById` (JPQL) |
| `backend/.../modulos/usuarios/services/PerfilServiceImpl.java` | Criado — `criar()` implementado e funcional; injeta `UsuarioRepository` em vez de `UsuarioService` |
| `backend/.../modulos/usuarios/services/UsuarioServiceImpl.java` | Alterado — `criar()` chama `perfilService.criar()` após `save()`, dentro de `@Transactional`; `referenciaPorId` removido |
| `backend/.../resources/db/migration/V3__criar-tabela-refresh-token.sql` | Criado pelo desenvolvedor — tabela `refresh_token` |

## Verificações executadas

| Comando | Resultado |
|---|---|
| `./mvnw -q compile` | ✅ passou, sem erros |
| `./mvnw -q spring-boot:run -Dspring-boot.run.profiles=dev` (1ª tentativa) | ❌ falhou — `Schema validation: missing table [refresh_token]` |
| `./mvnw -q spring-boot:run -Dspring-boot.run.profiles=dev` (2ª tentativa, após `V3`) | ❌ falhou — dependência circular `UsuarioServiceImpl ↔ PerfilServiceImpl` |
| `./mvnw -q spring-boot:run -Dspring-boot.run.profiles=dev` (3ª tentativa, após remover o ciclo) | ✅ subiu limpo, Flyway validou 3 migrations, Tomcat na porta 8080 |
| `curl -X POST http://localhost:8080/api/usuario` com payload combinado | ✅ `201 Created`, `{"id":4,"nome":"Maria Teste","email":"maria.teste@example.com"}` |
| `SELECT * FROM usuario` / `SELECT * FROM perfil` via `docker exec bec-postgres psql` | ✅ confirmado — usuário id 4 e perfil `mariateste` persistidos na mesma transação |

## Problemas encontrados

1. **`Perfil.usuario` é objeto, não `Long`** — `perfilNovo.setUsuario(usuarioId)` não compilava
   porque `Perfil.usuario` é `@ManyToOne` para `Usuario`. Resolvido expondo uma forma de obter a
   entidade a partir do id sem `SELECT` extra (`getReferenceById`).
2. **Dependência circular entre services** — a primeira solução para o problema acima
   (`UsuarioService.referenciaPorId`, chamado por `PerfilServiceImpl`) criou um ciclo
   `UsuarioServiceImpl → PerfilServiceImpl → UsuarioServiceImpl`, bloqueado pelo Spring Boot por
   padrão desde a versão 2.6. Resolvido injetando `UsuarioRepository` direto em
   `PerfilServiceImpl`, já que ambas as entidades são do mesmo módulo.
3. **Falta de migration para `RefreshToken`** — a entidade existe em código desde sessão anterior
   (feature de refresh token), mas nunca ganhou uma migration Flyway. Só apareceu ao tentar subir
   a aplicação de fato pela primeira vez desde então; até esta sessão, ninguém tinha rodado
   `spring-boot:run` depois daquela feature ser escrita.
4. **`tipo` do `Perfil` não era setado em `criar()`** — `NOT NULL` no banco, ausência não pegava
   em tempo de compilação, só estouraria em runtime no `save()`. Identificado por revisão de
   código, corrigido antes do teste ponta a ponta.
5. **Ordem de chamadas trocada em `UsuarioServiceImpl.criar()`** — `perfilService.criar()` antes
   de `repository.save(usuarioNovo)` passaria `usuarioNovo.getId() == null` (id só existe pós
   `INSERT` com `GenerationType.IDENTITY`). Identificado por revisão, corrigido.

## Pendências

- [ ] Todo usuário cadastrado hoje nasce com `ativo = false` (confirmado na consulta ao banco,
      inclusive o registro de teste desta sessão) — não há fluxo de verificação de e-mail
      implementado, e não há decisão registrada sobre se o cadastro deveria ativar o usuário
      direto ou se `ativo=false` é intencional até esse fluxo existir
- [ ] Registro de teste manual antigo (`usuario.id=3`, `cpf='1123`) continua no banco de
      desenvolvimento — resíduo de sessão anterior a validações de tamanho, sem impacto funcional
      hoje, mas sujeira de dado
- [ ] Logout (`sair()`, `AutenticacaoController`, cookie `httpOnly`) — não avançado nesta sessão,
      é o tema da próxima
- [ ] Pendências de sessões anteriores seguem abertas: 3 dos 4 achados da revisão automática de
      segurança sobre `43cac5b` sem detalhe; `UsuarioController` sem checagem de autorização por
      recurso; divergência de expiração do access token (60 min no código vs. 15 min em
      `design-sistema.md`); `MessageDigest` estático não thread-safe em
      `AutenticacaoServiceImpl`; imports órfãos em `SecurityConfig`; estratégia de hash do
      refresh token sem ADR/nota formal

## Próximos passos

1. Construir `AutenticacaoController` com as rotas `POST /entrar` e `POST /sair`
2. Decidir e implementar o mecanismo de cookie `httpOnly` para o refresh token no login/logout
3. Só então fechar a assinatura final de `sair()` (hoje `UnsupportedOperationException` sem
   parâmetro)
4. Revisitar a pendência de `ativo=false` no cadastro antes de depender desse campo em regra de
   autorização
