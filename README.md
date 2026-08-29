# Bico em Casa

Um sistema para alocação e contratação de serviços (bicos) de forma
prática e fácil, útil no seu aperreio do dia a dia. Desenvolvido dentro
da matéria de **Experiência Criativa** na PUCPR, com tudo estruturado e 
funcional.

**Stack:** Java 21 + Spring Boot 4.1 · Next.js 16 + TypeScript 7 · PostgreSQL 18 · MinIO

---

## Como começar

### Pré-requisitos

| Ferramenta | Versão | Por quê |
|---|---|---|
| **JDK 21** | 21.x | O Maven vem pelo wrapper (`./mvnw`) — não precisa instalar Maven |
| **Node.js** | `>=20.9.0` | Validado em v26.5.1 |
| **Docker** e **Docker Compose** | qualquer recente | PostgreSQL, MinIO e os testes de integração (Testcontainers) |

No Linux, seu usuário precisa estar no grupo `docker`. Se `docker info` falhar com
`permission denied on /var/run/docker.sock`:

```bash
sudo usermod -aG docker $USER   # depois, relogue a sessão
```

### 1. Suba a infraestrutura local

```bash
docker compose up -d
docker compose ps               # postgres deve aparecer como (healthy)
```

Sobe PostgreSQL 18 na porta 5432 e MinIO nas portas 9000 (API) e 9001 (console), já com os
buckets `portfolios`, `anexos` e `avatares` criados.

As credenciais de desenvolvimento têm padrão embutido no `docker-compose.yml`, então isso
funciona sem nenhuma configuração. Para mudá-las, copie `.env.exemplo` para `.env` e edite —
o `.env` é ignorado pelo git e **nenhum segredo real entra no repositório**.

### 2. Backend

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Detalhes, convenções e testes em [`backend/README.md`](backend/README.md).

### 3. Frontend

```bash
cd frontend
npm ci
npm run dev
```

Detalhes e scripts em [`frontend/README.md`](frontend/README.md).

### Duas coisas que parecem defeito e não são

- **O backend sobe com o banco vazio.** `src/main/resources/db/migration/` ainda não tem
  nenhuma migration e não existe nenhuma entidade JPA — as migrations `V1`–`V6` são a próxima
  task. A aplicação inicia, o Flyway cria a tabela de histórico e não aplica nada. É o estado
  esperado da fundação, não uma instalação quebrada.
- **`npm run lint` falha** com `typescript-eslint does not support TS 7.0`. É limitação conhecida
  do `typescript-eslint` com TypeScript 7
  ([#10940](https://github.com/typescript-eslint/typescript-eslint/issues/10940)), e a decisão
  registrada foi manter o TypeScript 7. `npm run typecheck`, `npm run build` e `npm test` não são
  afetados.

---

## Documentação

| Onde | O quê |
|---|---|
| [`docs/README.md`](docs/README.md) | Índice da documentação e o roteiro de leitura para quem chega |
| [`docs/design-sistema.md`](docs/design-sistema.md) | A arquitetura vigente, em prosa |
| [`docs/arquitetura-sistema.json`](docs/arquitetura-sistema.json) | A **fonte da verdade** da arquitetura |
| [`docs/adr/`](docs/adr/) | As decisões estruturais e o porquê de cada uma |
| [`CLAUDE.md`](CLAUDE.md) | As regras de trabalho do repositório — leia antes de abrir um PR |

Três regras valem para qualquer alteração e estão detalhadas no [`CLAUDE.md`](CLAUDE.md):
a **paridade da documentação** (o JSON muda primeiro, o markdown espelha, no mesmo commit),
**quem decide é o desenvolvedor** e **o código de aplicação é escrito pela equipe**.

---

## Licença

[Apache License 2.0](LICENSE).
