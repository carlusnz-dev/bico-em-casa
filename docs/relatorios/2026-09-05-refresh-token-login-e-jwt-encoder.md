# Relatório de Sessão — 2026-09-05

| Campo | Valor |
|---|---|
| **Sessão** | Modelo `RefreshToken`, contrato/DTO/service do módulo `autenticacao`, e implementação do login (`entrar()`) com `JwtEncoder` |
| **Autor** | Carlos Antunes |
| **Data** | `2026-09-05` |
| **Duração aproximada** | Sessão longa, várias trocas de contexto (não cronometrada) |
| **LLM utilizada** | `Claude Sonnet 5 (Claude Code)` |
| **Branch** | `feat/modulo-autenticacao` |
| **Commits** | `3a2a06d`, `0926774`, `d521bc2` |
| **Plano relacionado** | Nenhum |

---

## Resumo

Sessão de construção incremental do módulo `autenticacao`, começando pela entidade `RefreshToken`
e terminando com o método `entrar()` (login) emitindo access token via `JwtEncoder`. O padrão de
trabalho foi review linha a linha a cada arquivo que o desenvolvedor escrevia — model, repository,
contrato, DTO, `Impl` — corrigindo bugs antes de avançar pra peça seguinte.

O trecho mais relevante da sessão foi a modelagem da credencial de login: `UsuarioResponse` não
expõe `hash_senha` de propósito, e a solução não foi estendê-lo, e sim criar um DTO dedicado
(`CredenciaisUsuario`) e um método (`UsuarioService.buscarCredenciaisPorEmail`) publicado só pra
esse uso. Esse método devolve `Optional` em vez de lançar exceção quando o e-mail não existe —
decisão de segurança para que "e-mail não encontrado" e "senha errada" cheguem ao cliente como o
mesmo erro genérico, evitando enumeração de usuários cadastrados.

Duas vezes nesta sessão o desenvolvedor pediu explicitamente que a LLM escrevesse código de
aplicação (o `LoginRequest`/`entrar()`, e a explicação do bean `jwtEncoder` — este último o
desenvolvedor decidiu escrever ele mesmo). Nos dois casos a LLM parou e perguntou antes de
escrever, conforme a Regra nº 3 do projeto, e só prosseguiu com confirmação explícita.

Ao final, o desenvolvedor pediu para disparar requisições de teste reais contra a API. Não foi
possível: não existe `AutenticacaoController` (nenhuma rota HTTP pra login), `UsuarioController`
não tem rota de busca por e-mail, e não existe nenhum mecanismo de cookie no código. O
desenvolvedor optou por pular o teste HTTP nesta sessão e fechar só com o commit do que existe.
Ao commitar, a LLM cometeu um erro de git (`git commit` sem pathspec gravou o índice inteiro,
incluindo stage antigo de outros arquivos) — percebido antes de qualquer push, corrigido com
`git reset --soft` (nada foi perdido) e recomposto em três commits com pathspec explícito.

## O que foi feito

- `RefreshToken` (entidade JPA) alinhada ao `modelo-dados.dbml`: `familia_id` corrigido de `int`
  para `UUID`, `substituido_por` corrigido de um `@OneToOne`/`@JoinColumn` inválido (não compilava)
  para coluna simples nulável, `ip_origem` corrigido de `Inet4Address` (sem mapeamento Hibernate,
  restrito a IPv4) para `String`, coluna `expira_em` corrigida de `expirado_em`, índice duplicado
  em `hash_token` removido, índice de `usuario_id` (previsto no `.dbml`) adicionado
- `RefreshTokenRepository`, `RefreshTokenRequest`/`RefreshTokenResponse`, `RefreshTokenService`
  (contrato) e `RefreshTokenServiceImpl` criados — com correção de `@NotBlank` usado em `UUID`/
  `Long` (só vale para `CharSequence`), de métodos de busca que assumiam uma linha por usuário/
  família quando o modelo permite várias, e da falta de `repository.save()` em `criar()`
- `usuarios/dto/CredenciaisUsuario` e `UsuarioService.buscarCredenciaisPorEmail` criados —
  publicam só `id`/`hash_senha` para autenticação, sem estender `UsuarioResponse`
- `AutenticacaoService` (contrato) redesenhado de `criar(RefreshTokenRequest)` (duplicava
  `RefreshTokenService`) para `entrar(LoginRequest)`/`sair()`
- `AutenticacaoServiceImpl.entrar()` implementado: valida credencial com `PasswordEncoder.matches`
  (não `encode`, que gera hash novo a cada chamada e nunca bateria), gera o refresh token bruto
  (`SecureRandom`, 256 bits) e seu hash (`SHA-256`, não Argon2id — justificativa nas decisões),
  persiste via `RefreshTokenService.criar`, monta o `JwtClaimsSet` (subject = id do usuário, sem
  papel/role) e emite o access token via `JwtEncoder`
- `@Bean JwtEncoder` escrito em `SecurityConfig.java` pelo desenvolvedor (chave privada via
  `RsaKeyConverters.pkcs8()`, pública via `x509()`, combinadas num `RSAKey` do Nimbus) — a LLM
  só explicou o formato, a pedido do desenvolvedor
- `LoginRequest`/`LoginResponse` corrigidos de campos de cadastro copiados por engano
  (`nomeExibicao`, `confirmacaoSenha`) para `(email, senha)` e `(accessToken, refreshToken)`
- Compilação (`./mvnw compile`) verificada e passando a cada etapa fechada

## Decisões tomadas

| Decisão | Motivo | ADR |
|---|---|---|
| Refresh token é valor aleatório opaco (`SecureRandom`, 256 bits), hash `SHA-256` — não é JWT nem usa Argon2id | JWT seria redundante: o token já é persistido, consultado e revogado no banco a cada uso, então não há ganho em ser stateless. Argon2id é deliberadamente lento para proteger segredo de baixa entropia (senha); um token já nasce com entropia alta, então o hash lento só adiciona latência sem ganho de segurança | Não requer ainda — mas é decisão de arquitetura não documentada em nenhum lugar; ver pendências |
| `UsuarioService.buscarCredenciaisPorEmail` devolve `Optional<CredenciaisUsuario>` em vez de lançar `EntidadeNaoEncontradaException` | Login precisa tratar "e-mail não existe" e "senha errada" como o mesmo erro genérico — lançar exceção específica na origem permitiria enumerar e-mails cadastrados testando tentativas de login | Não requer |
| `AutenticacaoServiceImpl` depende de `RefreshTokenService` (contrato), não de `RefreshTokenRepository` direto | Evita dois caminhos divergentes de criação de `RefreshToken` dentro do mesmo módulo | Não requer |
| Exceção pontual à Regra nº 3: a LLM escreveu `LoginRequest` e o corpo de `entrar()` | Pedido explícito do desenvolvedor, confirmado via pergunta direta antes de escrever (a LLM ofereceu a alternativa de só explicar) | Não requer |
| `JwtEncoder` e a integração dele em `entrar()` foram escritos pelo desenvolvedor, não pela LLM | O desenvolvedor escolheu a opção "explica e eu escrevo" nas duas vezes em que isso foi perguntado | Não requer |

## Arquivos alterados

| Arquivo | Alteração |
|---|---|
| `backend/.../modulos/autenticacao/RefreshToken.java` | Criado — entidade, várias correções de mapeamento no caminho |
| `backend/.../modulos/autenticacao/RefreshTokenRepository.java` | Criado |
| `backend/.../modulos/autenticacao/contrato/RefreshTokenService.java` | Criado |
| `backend/.../modulos/autenticacao/dto/RefreshTokenRequest.java` | Criado |
| `backend/.../modulos/autenticacao/dto/RefreshTokenResponse.java` | Criado |
| `backend/.../modulos/autenticacao/services/RefreshTokenServiceImpl.java` | Criado |
| `backend/.../modulos/usuarios/contrato/UsuarioService.java` | Alterado — `buscarPorEmail`, `buscarCredenciaisPorEmail` |
| `backend/.../modulos/usuarios/dto/CredenciaisUsuario.java` | Criado |
| `backend/.../modulos/usuarios/services/UsuarioServiceImpl.java` | Alterado — implementação dos dois métodos acima |
| `backend/.../core/excecao/SenhaNaoBateException.java` | Criado |
| `backend/.../modulos/autenticacao/contrato/AutenticacaoService.java` | Criado |
| `backend/.../modulos/autenticacao/dto/LoginRequest.java` | Criado |
| `backend/.../modulos/autenticacao/dto/LoginResponse.java` | Criado |
| `backend/.../modulos/autenticacao/services/AutenticacaoServiceImpl.java` | Criado |
| `backend/.../config/SecurityConfig.java` | Alterado — `@Bean JwtEncoder` |

## Verificações executadas

| Comando | Resultado |
|---|---|
| `./mvnw -q -B compile` (logo após escrever `LoginRequest`/`entrar()`) | ❌ falhou — `cannot find symbol: method filter(...)` em `CredenciaisUsuario`, porque `UsuarioService.buscarCredenciaisPorEmail` tinha revertido para lançar exceção em vez de devolver `Optional` |
| `./mvnw -q -B compile` (após restaurar `Optional` em `UsuarioService`/`Impl`) | ✅ passou |
| `./mvnw -q -B compile` (após o `@Bean jwtEncoder`) | ✅ passou |
| `./mvnw -q -B compile` (antes do commit final, com o `JwtEncoder` já plugado em `entrar()`) | ✅ passou |
| `git status`/`git diff --stat` após cada commit | ✅ conferido — usado para pegar o erro de staging descrito abaixo |

## Problemas encontrados

1. **`RefreshToken.substituidoPor` combinava `@Column` e `@JoinColumn`/`@OneToOne` no mesmo campo
   tipado `UUID`** — não compila (JPA não aceita as duas anotações juntas, e `@OneToOne` exige
   tipo de entidade, não `UUID`). Corrigido para coluna simples.
2. **`substituidoPor` chegou a ficar `nullable = false`** — teria impedido criar qualquer token
   (o campo só ganha valor quando o token é rotacionado, nunca na criação). Corrigido para
   nulável, conforme o `.dbml`.
3. **`findByUsuarioId`/`findByFamiliaId` devolviam `Optional<RefreshToken>`** — quebraria com
   `IncorrectResultSizeDataAccessException` assim que existisse mais de uma sessão por usuário ou
   mais de uma rotação por família (que é o caso normal, não a exceção). Corrigido para `List`.
4. **`passwordEncoder.encode(senha)` sendo comparado com o hash salvo** — `encode` gera salt novo
   a cada chamada, então a comparação nunca bateria, nem com a senha certa. Corrigido para
   `passwordEncoder.matches(senha, hash)`.
5. **Erro de git ao commitar**: `git commit` sem pathspec grava o índice inteiro, não só os
   arquivos passados ao `git add` anterior. Como vários arquivos já estavam com stage antigo de
   edições anteriores da sessão, o primeiro commit (`518c8b4`) saiu com conteúdo desatualizado de
   `AutenticacaoServiceImpl`, `LoginRequest`, `LoginResponse`, `SenhaNaoBateException` e
   `CredenciaisUsuario` — divergente do que tinha sido revisado. Detectado antes de qualquer push
   (nada compartilhado), corrigido com `git reset --soft HEAD~1` (não descarta nada) e recomposto
   em três commits usando `git commit -- <arquivos>`, que usa o conteúdo da working tree
   diretamente em vez do índice.
6. **Testes HTTP pedidos pelo desenvolvedor não puderam ser executados** — não existe
   `AutenticacaoController`, não existe rota de busca de usuário por e-mail, e não existe nenhum
   código que escreva cookie. O desenvolvedor foi consultado e optou por adiar o teste end-to-end
   para depois desses controllers existirem.

## Pendências

- [ ] `AutenticacaoController` (rota HTTP de login/logout) não existe — bloqueia todo teste
      end-to-end pedido nesta sessão
- [ ] `UsuarioController` não expõe busca por e-mail via HTTP
- [ ] Nenhum mecanismo de cookie `httpOnly` para o refresh token foi implementado — `LoginResponse`
      hoje devolve os dois tokens só no corpo
- [ ] `sair()` (logout) lança `UnsupportedOperationException` — a assinatura não tem parâmetro
      nenhum, não há como saber qual refresh token revogar sem redesenhar o contrato
- [ ] `MessageDigest` estático em `AutenticacaoServiceImpl` não é thread-safe (`digest()` muda
      estado interno) — apontado três vezes ao longo da sessão, nunca corrigido
- [ ] Access token com expiração de 60 minutos no código (`entrar()`), mas `design-sistema.md`
      §3.2 documenta 15 minutos — divergência não resolvida
- [ ] Variável local `acessToken` em `AutenticacaoServiceImpl.entrar()` mantém erro de grafia
      (campo do DTO já foi corrigido para `accessToken`)
- [ ] Dois imports não usados em `SecurityConfig.java` (`com.nimbusds.jwt.JWT`,
      `java.security.MessageDigest`) apontados e não removidos
- [ ] **A estratégia de geração/hash do refresh token (`SecureRandom` + `SHA-256`, opaco, não-JWT)
      é decisão de arquitetura tomada nesta sessão e não está em nenhum ADR nem no
      `design-sistema.md`** — pela Regra nº 1, precisa virar nota documentada (ADR ou extensão do
      §3.2) antes de a lacuna crescer
- [ ] Pendências do relatório de 04/09 seguem abertas: 3 dos 4 achados da revisão automática de
      segurança sobre `43cac5b` sem detalhe; `UsuarioController` sem checagem de autorização por
      recurso
- [ ] `UsuarioServiceImpl.criar()` lança `RuntimeException` genérica para senha curta — fora do
      escopo desta sessão, não tocado

## Próximos passos

1. Escrever `AutenticacaoController` (`POST /login`, `POST /logout`) e decidir onde o refresh
   token vira cookie `httpOnly`
2. Resolver a assinatura de `sair()` — provavelmente recebe o refresh token via cookie, não é
   um método sem parâmetro
3. Corrigir o `MessageDigest` estático, a divergência de 60→15 minutos e os imports órfãos em
   `SecurityConfig`
4. Decidir e documentar (ADR ou nota em `design-sistema.md` §3.2) a estratégia de geração/hash do
   refresh token definida nesta sessão
5. Só depois disso, rodar os testes de sucesso/erro pela API que ficaram pendentes nesta sessão
