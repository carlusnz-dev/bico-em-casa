# Relatório de Sessão — 2026-09-07

| Campo | Valor |
|---|---|
| **Sessão** | Retomada de contexto do módulo `autenticacao`/`usuarios` e início da ligação Usuario↔Perfil no cadastro |
| **Autor** | Carlos Antunes |
| **Data** | `2026-09-07` |
| **Duração aproximada** | Sessão curta, interrompida para almoço |
| **LLM utilizada** | `Claude Sonnet 5 (Claude Code)` |
| **Branch** | `feat/modulo-autenticacao` |
| **Commits** | Nenhum |
| **Plano relacionado** | Nenhum |

---

## Resumo

Sessão começou com um pedido de retomada de contexto: reler os relatórios de 04/09 e 05/09 e
conferir, lendo o código atual (branch limpa em `ecded65`, sem pendência de commit), o que
realmente existe hoje no módulo de login antes de avançar para cadastro-com-perfil e logout.
Confirmado por leitura direta: `entrar()` (login) está implementado e emitindo JWT; `sair()`
(logout) continua um `UnsupportedOperationException` sem parâmetro; não existe
`AutenticacaoController` (nenhuma rota HTTP de login/logout); cadastro (`UsuarioServiceImpl.criar()`)
persiste `Usuario` mas nunca cria `Perfil`; `PerfilController` é um stub vazio, sem
`PerfilService`/`PerfilServiceImpl`.

Duas decisões de escopo foram levantadas e resolvidas antes de qualquer código: cadastro vai usar
**um único endpoint** (payload combinado de usuário + perfil, gravação transacional), em vez de
dois endpoints separados; e o desenho do logout (onde fica o cookie `httpOnly`, como `sair()`
identifica o refresh token) fica pendente até o desenvolvedor construir o `AutenticacaoController`
— não foi discutido em detalhe nesta sessão.

O desenvolvedor começou a escrever `PerfilService`/`PerfilServiceImpl` e travou em
`PerfilServiceImpl.criar()`, que tentava validar se o usuário existe chamando
`usuarioService.usuarioExiste(request)` passando um `PerfilRequest` onde o método espera um
`Long id` — erro de compilação, e sintoma de um desenho que tinha voltado, sem intenção, para o
modelo de dois passos descartado. A sessão foi de revisão linha a linha: identificar os dois erros
de compilação, explicar por que a checagem de existência é redundante no fluxo de endpoint único
(o `Usuario` acabou de ser salvo na mesma transação — `GenerationType.IDENTITY` garante que
`usuarioNovo.getId()` já vem preenchido logo após `repository.save()`), e apontar um bug de lógica
separado em `UsuarioServiceImpl.usuarioExiste()`, que lança exceção justamente quando o usuário
não existe, esvaziando o propósito do método. Nenhuma correção foi aplicada — a sessão foi
interrompida para almoço antes disso, com o pedido do desenvolvedor de continuar depois.

## O que foi feito

- Contexto dos relatórios de 04/09 e 05/09 revisado e confirmado contra o código atual (nenhuma
  divergência entre o que os relatórios diziam e o estado real do repositório)
- Decisão de escopo registrada: cadastro usa endpoint único (`UsuarioRequest` vai ganhar um campo
  `PerfilRequest perfil`), não dois endpoints separados
- Decisão de escopo registrada: desenho do logout (`sair()`, cookie `httpOnly`) fica para depois
  do desenvolvedor construir o `AutenticacaoController` — não avançado nesta sessão
- Identificados, por leitura direta do WIP não commitado, dois erros de compilação em
  `PerfilServiceImpl.criar()`: passagem de tipo errado para `usuarioService.usuarioExiste(...)`
  (`PerfilRequest` em vez de `Long`) e ausência de `return` no método
- Explicado o mecanismo de `GenerationType.IDENTITY` (o id do `Usuario` fica disponível
  imediatamente após `repository.save()`, sem necessidade de busca posterior) como base para o
  desenho correto: `UsuarioServiceImpl.criar()` chama `PerfilService.criar(usuarioId, request.perfil())`
  internamente, sem checagem de existência
- Apontado bug de lógica em `UsuarioServiceImpl.usuarioExiste()`: lança
  `EntidadeNaoEncontradaException` quando o usuário não existe, então o campo `existe` do DTO
  `UsuarioExiste` nunca retorna `false` na prática
- Confirmado que o projeto ainda não usa `@Transactional` em lugar nenhum — será a primeira
  ocorrência, necessária para atomicidade de `Usuario` + `Perfil`

## Decisões tomadas

| Decisão | Motivo | ADR |
|---|---|---|
| Cadastro usa um único endpoint (`POST /api/usuario` com payload combinado usuário+perfil) em vez de dois endpoints separados | Escolha do desenvolvedor entre as duas opções apresentadas — mais simples para o cliente da tela de cadastro, a um custo de acoplar `UsuarioRequest` a `PerfilRequest` | Não requer |
| `PerfilService.criar()` é chamado internamente por `UsuarioServiceImpl.criar()` (service→service), não por um segundo request HTTP do cliente | Consequência direta da decisão acima; o `usuarioId` nasce dentro do backend (via `GenerationType.IDENTITY`) e nunca precisa transitar pelo cliente | Não requer |
| Desenho do logout (`sair()`, cookie `httpOnly`) adiado até existir `AutenticacaoController` | Pedido explícito do desenvolvedor ("entram depois que eu construir o controller") | Não requer |

## Arquivos alterados

Nenhum commit nesta sessão. Estado do working tree ao final (não commitado):

| Arquivo | Alteração |
|---|---|
| `backend/.../modulos/usuarios/contrato/PerfilService.java` | Criado (não rastreado até agora) — `criar`, `buscarPorId`, `buscarPorSlug` |
| `backend/.../modulos/usuarios/contrato/UsuarioService.java` | Alterado — assinatura `usuarioExiste(Long id)` adicionada |
| `backend/.../modulos/usuarios/dto/UsuarioExiste.java` | Criado — record `(boolean existe, boolean ativo)` |
| `backend/.../modulos/usuarios/repository/PerfilRepository.java` | Alterado — `findByNomeExibicao`, `existsByNomeUsuario` adicionados |
| `backend/.../modulos/usuarios/repository/UsuarioRepository.java` | Alterado — `findAtivoById` (query JPQL) adicionado |
| `backend/.../modulos/usuarios/services/PerfilServiceImpl.java` | Criado — `buscarPorId`/`buscarPorSlug` implementados; `criar()` **não compila** (ver Problemas encontrados) |
| `backend/.../modulos/usuarios/services/UsuarioServiceImpl.java` | Alterado — `usuarioExiste()` implementado, com bug de lógica apontado e não corrigido |

## Verificações executadas

Nenhum comando de build/teste foi executado nesta sessão — o código em `PerfilServiceImpl.criar()`
não compila no estado em que a sessão foi interrompida, então rodar `./mvnw compile` não traria
informação além do que já foi identificado por leitura.

## Problemas encontrados

1. **`PerfilServiceImpl.criar()` não compila** — chama `usuarioService.usuarioExiste(request)`
   passando um `PerfilRequest` para um parâmetro `Long id`, e o método não tem `return` em nenhum
   caminho apesar de declarar retorno `PerfilResponse`. Não corrigido nesta sessão.
2. **Desenho de `PerfilServiceImpl.criar()` tinha revertido, sem intenção, para o modelo de dois
   endpoints** que o desenvolvedor já tinha descartado — a checagem de existência de usuário só
   faz sentido nesse modelo. Identificado a tempo, antes de qualquer commit; realinhado
   verbalmente para o modelo de endpoint único, mas o código ainda não foi ajustado.
3. **`UsuarioServiceImpl.usuarioExiste()` lança exceção no caso que deveria reportar como
   resultado** — `findAtivoById(id).orElseThrow(...)` dispara `EntidadeNaoEncontradaException`
   quando o usuário não existe, então o campo `existe` do `UsuarioExiste` nunca chega a `false`.
   Apontado, não corrigido.

## Pendências

- [ ] Corrigir `PerfilServiceImpl.criar()`: assinatura para `criar(Long usuarioId, PerfilRequest request)`, remover a checagem de existência, implementar o `return` faltante
- [ ] Adicionar campo `PerfilRequest perfil` em `UsuarioRequest`
- [ ] `UsuarioServiceImpl.criar()` chamar `perfilService.criar(usuarioNovo.getId(), request.perfil())` após salvar o `Usuario`, dentro de `@Transactional` (primeira ocorrência da anotação no projeto)
- [ ] Corrigir `UsuarioServiceImpl.usuarioExiste()` para não lançar exceção quando o usuário não existe — decidir se esse método ainda é necessário depois do ajuste do fluxo de cadastro, já que deixa de ser chamado por `PerfilService.criar()`
- [ ] Compilar (`./mvnw compile`) assim que os ajustes acima forem feitos — nada foi verificado nesta sessão
- [ ] Logout (`sair()`, `AutenticacaoController`, cookie `httpOnly`) — não avançado, aguardando o desenvolvedor construir o controller
- [ ] Pendências de sessões anteriores seguem abertas: 3 dos 4 achados da revisão automática de segurança sobre `43cac5b` sem detalhe; `UsuarioController` sem checagem de autorização por recurso; divergência de expiração do access token (60 min no código vs. 15 min em `design-sistema.md`); `MessageDigest` estático não thread-safe em `AutenticacaoServiceImpl`; imports órfãos em `SecurityConfig`; estratégia de hash do refresh token sem ADR/nota formal

## Próximos passos

1. Corrigir os dois erros de compilação em `PerfilServiceImpl.criar()` e alinhar a assinatura ao fluxo de endpoint único
2. Ligar `UsuarioServiceImpl.criar()` a `PerfilService.criar()`, com `@Transactional`
3. Compilar e, se possível, testar o cadastro combinado ponta a ponta
4. Retomar o módulo de logout: `AutenticacaoController`, mecanismo de cookie `httpOnly`, e só então a assinatura final de `sair()`
