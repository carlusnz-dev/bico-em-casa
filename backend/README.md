# backend

API da plataforma Bico em Casa.

## Stack

- **Java 21**
- **Spring Boot 4.1.1** (Maven, via wrapper — não é preciso ter o Maven instalado)
- **PostgreSQL 18**, schema versionado por **Flyway** (migrations em `src/main/resources/db/migration`)
- **Testcontainers 2.0**, para teste de integração com PostgreSQL real
- **springdoc-openapi**, **MapStruct**, **Lombok**, **BouncyCastle** (`Argon2PasswordEncoder`),
  AWS SDK v2 `s3` (client MinIO — compatível com a API S3)

Ver [`docs/design-sistema.md`](../docs/design-sistema.md) e
[ADR-0006](../docs/adr/0006-remover-supabase-infraestrutura-propria.md) na raiz do repositório
para o desenho completo da arquitetura.

## Como rodar

1. Suba a infraestrutura (PostgreSQL, MinIO etc.) a partir da raiz do repositório:

   ```bash
   docker compose up -d
   ```

2. Rode a aplicação com o profile `dev`:

   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

### A aplicação não sobe sem o banco de pé — e isso é proposital

`spring.jpa.hibernate.ddl-auto` está fixo em `validate` e o Flyway está habilitado em
`application.yml`. Isso significa que, sem o PostgreSQL da etapa 1 rodando e alcançável, **a
aplicação falha ao subir** — ela não tenta criar ou adivinhar um schema. É o comportamento
correto: falhar cedo na inicialização é preferível a subir servindo requisição com schema errado
ou divergente do que as migrations descrevem.

## Estrutura de pastas

```
backend/src/main/java/br/com/bicoemcasa/api/
└── BicoEmCasaApplication.java
```

Hoje só existe a classe de entrada. A convenção do projeto é: **uma pasta só existe quando há
mais de um arquivo daquele tipo dentro dela** — com um arquivo só, ele fica na raiz do pacote. Por
isso ainda não há `config/`, `lib/`, `comum/` nem `modulos/`: eles nascem no commit em que o
segundo arquivo de cada categoria for escrito, não antes.

Quando os módulos de domínio (`autenticacao`, `usuarios`, `profissionais`, `servicos`,
`contratacoes`, `avaliacoes`) forem criados, cada um segue internamente:
`controller/`, `service/`, `repository/`, `models/`, `dto/`, `contrato/` e, se necessário,
`mapper/`.

`src/main/resources/db/migration/` está vazia de propósito — as migrations V1 a V6 são a próxima
task, fora do escopo desta fundação.

## Convenções que valem neste módulo

- **Interface em `contrato/` com nome limpo, implementação em `service/` com sufixo `Impl`.**
  Sem prefixo `I` (`ProfissionalService` na interface, `ProfissionalServiceImpl` na
  implementação).
- **Injeção por construtor, campos `final`.** Nada de `@Autowired` em campo.
- **`record` para DTO.** Entidade JPA nunca cruza a fronteira do `Controller` — nem como
  parâmetro, nem como retorno.
- **Erros centralizados em `@RestControllerAdvice`**, respondendo com `ProblemDetail`
  (RFC 9457), não com corpo de erro ad-hoc por endpoint.
- **Módulo A fala com módulo B só pela interface exposta em `B/contrato/`.** Nunca injetando o
  `Repository` de outro módulo, nunca com relacionamento JPA cruzando a fronteira do módulo —
  referencie a entidade de outro módulo pelo `id` (`Long` ou `UUID`, conforme
  [ADR-0007](../docs/adr/0007-chave-primaria-mista.md)).
- **Testcontainers com PostgreSQL real em teste de regra de negócio. H2 é proibido** — ele diverge
  do Postgres exatamente nos pontos que costumam importar (tipos, constraints, comportamento de
  transação).

## Provisionar o primeiro administrador

O cadastro público cria contas de cliente e profissional. Depois que existe um administrador,
uma sessão desse papel pode provisionar outra conta pelo painel de usuários.

O primeiro provisionamento é feito pelo operador do PostgreSQL, depois de aplicar as migrations.

1. Cadastre uma conta de cliente dedicada à administração e confirme que você controla o e-mail
   e a senha. Use o ID numérico da conta (tabela `usuario`) e o e-mail exato do cadastro.
2. Na raiz do repositório, conecte-se à base correta com uma identidade autorizada a atualizar
   `usuario`, `perfil` e `refresh_token`, bloquear `perfil` e inserir em `log_acao`.

   ```sh
   psql -h localhost -U bicoemcasa -d bicoemcasa -f backend/scripts/provisionar-primeiro-admin.sql
   ```

3. Informe o ID e o e-mail ao script e faça um novo login. O script promove o perfil dedicado,
   invalida a sessão anterior e registra a operação. A transação aborta se o ID e o e-mail não
   identificarem uma única conta ativa de cliente ou se já existir um perfil ADMIN.

Antes de atualizar uma base usada com o cadastro legado, confira os perfis ADMIN existentes
e a titularidade dessas contas. O fluxo público antigo de usuários aceitava o papel ADMIN.
Se a equipe marcou contas como inativas manualmente, registre esses IDs antes da migration
`V20261109120000` para suspender as contas com o fluxo auditado após a atualização. A migration
ativa os cadastros legados porque antes o cadastro deixava esse campo falso e a autenticação
permitia o acesso.

## Testes

`BicoEmCasaApplicationTests` é um teste de fumaça: sobe o contexto do Spring contra um
`PostgreSQLContainer` (`postgres:18.6-alpine`) via `@ServiceConnection`, sem nenhuma entidade ou
migration. Ele existe para provar que a fundação (POM, autoconfiguração do Boot, driver JDBC,
Testcontainers, Flyway) resolve e sobe — não testa regra de negócio nenhuma.

Requer Docker para rodar (`./mvnw test`).
