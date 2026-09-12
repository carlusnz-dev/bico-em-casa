# Relatório de Sessão — 2026-09-04

| Campo | Valor |
|---|---|
| **Sessão** | Ativação de profile do Spring, `JwtDecoder` RSA para o resource server, e correção de bug no `UsuarioRepository` |
| **Autor** | Carlos Antunes |
| **Data** | `2026-09-04` |
| **Duração aproximada** | Sessão longa, várias trocas de contexto (não cronometrada) |
| **LLM utilizada** | `Claude Sonnet 5 (Claude Code)` |
| **Branch** | `feat/modulo-autenticacao` (início) → `feat/oauth2-jwt-decoder` (a partir de `develop`, onde a sessão terminou) |
| **Commits** | `43cac5b` (em `feat/modulo-autenticacao`), `32d0222`, `82c340f`, `54539c3` (em `feat/oauth2-jwt-decoder`) |
| **Plano relacionado** | Nenhum |

---

## Resumo

A sessão começou com uma dúvida prática — "como faço rodar o `application.yml` de produção" —
e virou uma sequência de três problemas reais, resolvidos um atrás do outro por debug
sistemático, mais uma boa quantidade de ensino sobre profiles do Spring, Nimbus JOSE, OAuth2/JWT
e a tabela `refresh_token`.

Primeiro problema: a aplicação não subia porque nenhum profile estava ativo e o `datasource` só
existe em `application-dev.yml` (o `application.yml` base nunca teve esse bloco — confirmado
comparando com o `HEAD` do Git). Rodar com `SPRING_PROFILES_ACTIVE=dev` resolveu, e revelou o
segundo problema: `SecurityConfig` já tinha `.oauth2ResourceServer(oauth2 -> oauth2.jwt(...))`
mas nenhum bean `JwtDecoder` existia — havia até uma tentativa comentada e tecnicamente errada
(`OAuth2AuthorizationServerConfiguration`, que é API de authorization server, não de resource
server), removida com autorização do desenvolvedor antes do commit em `feat/modulo-autenticacao`.

A partir daí, o desenvolvedor pediu uma branch nova a partir de `develop` para escrever o
`JwtDecoder` do zero. Antes disso, o trabalho pendente de `usuarios` (`UsuarioService`,
`UsuarioServiceImpl`, `UsuarioController`, `PerfilController`) foi commitado em
`feat/modulo-autenticacao` — e uma revisão de segurança automática em background sinalizou 4
problemas nesse commit, um deles confirmado por leitura direta do código (`UsuarioController`
sem nenhuma checagem de autorização — qualquer usuário autenticado pode buscar o cadastro de
qualquer outro por id). Os outros 3 achados da revisão não foram detalhados pela ferramenta e
**continuam sem investigação**.

Na branch nova, o desenvolvedor gerou o par de chaves RSA local (`openssl`, 2048 bits) em
`~/.bicoemcasa/keys/`, escreveu `RsaKeyProperties` (`@ConfigurationProperties`, campos `String`
com os caminhos dos arquivos) e, depois de uma explicação detalhada sobre `@Bean`/injeção de
dependência, Nimbus JOSE, o modelo simplificado de OAuth2 que o projeto usa (backend como emissor
e validador, sem authorization server externo) e a lógica de rotação/detecção de roubo do
`refresh_token`, escreveu o `@Bean JwtDecoder` no `SecurityConfig`. Isso expôs um terceiro
problema, também pré-existente: `UsuarioRepository.findAllByAtivo()` sem parâmetro — o mesmo bug
já registrado no relatório de 02/09, só que nunca corrigido nesta linhagem (a branch nova veio de
`develop`, que não tem o commit de correção de `feat/modulo-autenticacao`). O desenvolvedor
removeu o método.

Terminada a implementação, `docs/arquitetura-sistema.json` e `docs/design-sistema.md` foram
atualizados (mesmo commit) documentando a convenção de onde a chave RSA de dev mora, e uma
revisão de arquitetura (`arquiteto-sistema`, Sonnet) foi disparada para auditar essa mudança de
documentação — **resultado ainda não recebido no momento em que este relatório foi escrito**.

## O que foi feito

- Explicado o mecanismo de ativação de profile do Spring Boot (`SPRING_PROFILES_ACTIVE`) e
  corrigido o diagnóstico: o `datasource` nunca esteve no `application.yml` base, só em
  `application-dev.yml` — rodar sem profile ativo sempre falharia
- Removido o bloco de código comentado e tecnicamente incorreto (`OAuth2AuthorizationServerConfiguration`)
  do `SecurityConfig.java`, com confirmação do desenvolvedor, antes do commit em `feat/modulo-autenticacao`
- Commitado o WIP do módulo `usuarios` (`UsuarioService`, `UsuarioServiceImpl`, `UsuarioController`,
  `PerfilController`) em `feat/modulo-autenticacao` (`43cac5b`)
- Criada a branch `feat/oauth2-jwt-decoder` a partir de `develop` (local, sincronizada com a
  remota — conferido depois via API do GitHub, `2f031c1` nas duas pontas)
- Gerado o par de chaves RSA de desenvolvimento em `~/.bicoemcasa/keys/{private,public}.pem`
  (2048 bits, via `openssl`)
- Escrito, pelo desenvolvedor, `RsaKeyProperties.java` (`@ConfigurationProperties(prefix = "app.rsa")`)
  e o `@Bean JwtDecoder` no `SecurityConfig.java`, usando `RsaKeyConverters.x509()` e
  `NimbusJwtDecoder.withPublicKey(...)`
- Adicionadas as chaves `app.rsa.private-key-path` / `app.rsa.public-key-path` em
  `application-dev.yml`, usando `${user.home}` para portabilidade entre máquinas da equipe
- Removido `UsuarioRepository.findAllByAtivo()` (sem parâmetro, quebrava o boot) — mesmo bug do
  relatório de 02/09, reaparecido por a branch nova vir de `develop`, sem aquele fix
- Aplicação testada de ponta a ponta com `SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run`:
  sobe limpo, conecta no Postgres local, valida migrations, monta o `JwtDecoder`
- `docs/arquitetura-sistema.json` e `docs/design-sistema.md` atualizados no mesmo commit,
  documentando a convenção de chave RSA local em dev
- Disparada revisão de arquitetura (agente `arquiteto-sistema`, Sonnet) sobre a paridade e
  fidelidade dessa documentação — em andamento, sem resultado ainda

## Decisões tomadas

| Decisão | Motivo | ADR |
|---|---|---|
| Branch nova (`feat/oauth2-jwt-decoder`) a partir de `develop`, não de `feat/modulo-autenticacao` | Pedido explícito do desenvolvedor, para isolar o trabalho de JWT/OAuth do WIP de `usuarios` | Não requer |
| Remover o bloco comentado `OAuth2AuthorizationServerConfiguration` em vez de mantê-lo no histórico | Violava a convenção do projeto (proibido código comentado) e usava a API errada (authorization server, não resource server) | Não requer |
| `RsaKeyProperties` usa `String` (caminho) em vez de `RSAPublicKey`/`RSAPrivateKey` tipado com `RsaKeyConversionServicePostProcessor` | Escolha do desenvolvedor — conversão manual e explícita, mais didática nesta fase de aprendizado | Não requer |
| Convenção de chave RSA de dev: cada desenvolvedor gera a própria em `~/.bicoemcasa/keys/`, fora do repositório, referenciada via `${user.home}` | Mantém a chave fora do Git sem exigir variável de ambiente em dev; documentado como extensão natural do ADR-0006, não decisão estrutural nova | [ADR-0006](../adr/0006-remover-supabase-infraestrutura-propria.md) (extensão) |

## Arquivos alterados

| Arquivo | Alteração |
|---|---|
| `backend/.../modulos/usuarios/contrato/UsuarioService.java` | Alterado (commit `43cac5b`, branch `feat/modulo-autenticacao`) — métodos `buscarPorId`/`criar` |
| `backend/.../modulos/usuarios/services/UsuarioServiceImpl.java` | Alterado (`43cac5b`) — implementação; **não persiste** o usuário criado (sem `repository.save()`), apontado mas não corrigido |
| `backend/.../modulos/usuarios/controller/UsuarioController.java` | Criado (`43cac5b`) — sem checagem de autorização, apontado por revisão automática de segurança |
| `backend/.../modulos/usuarios/controller/PerfilController.java` | Criado (`43cac5b`) — stub vazio |
| `backend/.../config/SecurityConfig.java` | Alterado (`82c340f`, `feat/oauth2-jwt-decoder`) — `@Bean JwtDecoder` adicionado; bloco comentado removido antes do commit em `feat/modulo-autenticacao` |
| `backend/.../config/RsaKeyProperties.java` | Criado (`82c340f`) — `@ConfigurationProperties` com caminhos das chaves RSA |
| `backend/.../modulos/usuarios/repository/UsuarioRepository.java` | Alterado (`32d0222`) — `findAllByAtivo()` removido |
| `backend/src/main/resources/application-dev.yml` | Alterado (`82c340f`) — `app.rsa.private-key-path`/`public-key-path` |
| `docs/arquitetura-sistema.json` | Alterado (`54539c3`) — campo `rsa_key_dev_convention` |
| `docs/design-sistema.md` | Alterado (`54539c3`) — nota espelhando o campo acima, §3.2 |

## Verificações executadas

| Comando | Resultado |
|---|---|
| `./mvnw -B clean compile` (múltiplas vezes, ao longo da sessão) | ✅ passou, sempre limpo |
| `docker compose ps` | ✅ Postgres e MinIO saudáveis, já rodando |
| `SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run` (1ª tentativa) | ❌ falhou — `No qualifying bean of type 'JwtDecoder'` |
| `SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run` (2ª tentativa, após `@Bean` escrito) | ❌ falhou — `UsuarioRepository.findAllByAtivo()` sem argumento |
| `SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run` (3ª tentativa, após remover o método) | ✅ passou — reportado pelo desenvolvedor ("Agora foi") |
| `git ls-remote` / `gh api repos/.../branches/develop` | ✅ `develop` local (`2f031c1`) idêntica à remota |
| `git push` (dry-run) via HTTPS com `gh` como credential helper | ✅ funcionou — SSH direto falha neste ambiente (sem chave configurada) |

## Problemas encontrados

1. **`application.yml` sem `datasource` e nenhum profile ativo.** Erro `'url' attribute is not
   specified`. Causa raiz: arquitetura correta (datasource só em `application-dev.yml`), mas
   ninguém tinha ativado o profile ao rodar. Resolvido ativando `SPRING_PROFILES_ACTIVE=dev`.
2. **`JwtDecoder` ausente.** `SecurityConfig` já chamava `.oauth2ResourceServer().jwt()`, que
   exige esse bean, e ele nunca existiu — só uma tentativa comentada com a API errada
   (`OAuth2AuthorizationServerConfiguration`, de authorization server). Resolvido escrevendo o
   `@Bean` correto com `NimbusJwtDecoder.withPublicKey(...)`.
3. **`UsuarioRepository.findAllByAtivo()` sem parâmetro.** Mesmo bug do relatório de 02/09,
   reaparecido porque a branch nova veio de `develop`, que não tem aquele fix. Resolvido
   removendo o método (não estava em uso).
4. **Revisão de segurança em background sinalizou 4 problemas no commit `43cac5b`** — 1 confirmado
   por leitura direta (`UsuarioController` sem controle de autorização: qualquer usuário
   autenticado acessa o cadastro de qualquer outro por id sequencial), **3 sem detalhe conhecido**.
   Nenhum dos 4 foi corrigido nesta sessão.
5. **`UsuarioServiceImpl.criar()` não persiste** — monta o `Usuario` mas nunca chama
   `repository.save(...)`; `UsuarioResponse` retornado sempre tem `id` nulo. Apontado ao
   desenvolvedor, não corrigido nesta sessão.

## Pendências

- [ ] **3 dos 4 achados da revisão automática de segurança sobre `43cac5b` continuam sem
      detalhe** — rodar `/security-review` completo pra saber o que são
- [ ] **`UsuarioController` sem autorização por recurso** — qualquer usuário autenticado busca
      cadastro de qualquer outro por id; corrigir antes de expor a rota
- [ ] **`UsuarioServiceImpl.criar()` não persiste o usuário** — falta `repository.save(...)`
- [x] **Revisão de arquitetura (`arquiteto-sistema`, Sonnet) sobre `54539c3`** — voltou "conforme
      com ressalvas": MD sem a palavra "literal" que o JSON tinha, afirmação não confirmada sobre
      `application-prod.yml` (arquivo nunca lido, é `deny` de propósito), e contradição aparente
      entre o campo `secrets` e o novo `rsa_key_dev_convention`. Corrigido no commit `40e91c4`.
      Confirmado: não precisa de ADR — é convenção operacional já prevista pelo ADR-0006
- [x] **PR de `feat/oauth2-jwt-decoder` para `develop` aberto** — [#6](https://github.com/carlusnz-dev/bico-em-casa/pull/6)
- [ ] `NimbusJwtEncoder` (emissão de token) e o restante do módulo `autenticacao` — não
      começados; a chave privada gerada nesta sessão só será usada ali
- [ ] Pendências de convenção da migration V2 (relatório de 01/09) seguem abertas, não tocadas

## Próximos passos

1. Conferir o retorno da revisão de arquitetura e ajustar a documentação se algo estiver
   incorreto ou incompleto
2. Abrir o PR de `feat/oauth2-jwt-decoder` para `develop`
3. Rodar `/security-review` completo sobre `43cac5b` para levantar os 3 achados ainda sem
   detalhe, e decidir a correção do `UsuarioController`
4. Corrigir a falta de `repository.save()` em `UsuarioServiceImpl.criar()`
5. Escrever o `NimbusJwtEncoder` e o restante do módulo `autenticacao` (login, emissão de
   access/refresh token)
