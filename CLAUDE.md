# Bico em Casa

Plataforma de contratação de profissionais autônomos para serviços rápidos. O cliente encontra o
profissional, vê o portfólio (opcional) e contrata sem burocracia.

**Stack:** Java 21 + Spring Boot 4.1 (Maven) · Next.js 16.3 + TypeScript 7 · PostgreSQL 18 e
identidade própria (JWT RSA + Argon2id), storage no MinIO. Ver [ADR-0006](docs/adr/0006-remover-supabase-infraestrutura-propria.md).

**Estado em 2026-08-27:** a fundação técnica existe e está verificada — `backend/` compila,
`frontend/` builda e `docker-compose.yml` sobe PostgreSQL 18 e MinIO. **Código de aplicação não
existe, e não é a LLM que vai escrever** — ver a regra nº 3.

---

## Regra nº 1 — paridade da documentação

`docs/arquitetura-sistema.json` é a **única fonte da verdade** da arquitetura.
`docs/design-sistema.md` é o espelho legível dele.

**Ordem obrigatória:** alterar o JSON → refletir no markdown → ADR se for estrutural.
As duas alterações vão **no mesmo commit**. Editar o markdown sem antes editar o JSON é proibido.

Um hook `PostToolUse` (`.claude/hooks/paridade-docs.sh`) lembra disso automaticamente, e o
workflow `docs-parity.yml` falha o PR se a paridade for quebrada.

## Regra nº 2 — quem decide é o desenvolvedor, não a LLM

**Toda decisão de arquitetura ou de código é do desenvolvedor.** A LLM propõe, compara,
argumenta e mostra o trade-off; **ela não escolhe**. Se a resposta certa depende de uma
preferência, de um escopo ou de um custo que não está escrito no repositório, a LLM **pergunta
antes de escrever** — não assume o caminho mais provável e segue.

Vale para quem usa a ferramenta também: **aceitar uma sugestão sem entendê-la é decidir por
omissão**, e o resultado continua sendo responsabilidade de quem commitou.

Na prática, a LLM **deve parar e perguntar** quando:

- A escolha muda o schema, a fronteira entre módulos ou uma pasta
- Existe ADR sobre o assunto e a proposta contraria o ADR
- Um requisito de `docs/requisitos.json` deixaria de ter caminho técnico
- Há mais de uma solução defensável e a diferença é de preferência, não de correção

E **deve seguir sozinha** no resto: aplicar convenção já registrada, corrigir erro objetivo
(SQL inválido, tipo inexistente, `NOT NULL` que impede `INSERT`), escrever o que foi pedido.

### Checklist de revisão — antes de fechar uma task ou abrir PR

Isto é **disciplina da equipe, não automação**: não está no workflow de propósito, porque um
check que a máquina responde sozinha deixa de ser lido. Quem fecha a task responde às cinco
perguntas, honestamente, e escreve as respostas na descrição do PR ou no card:

1. **Eu revisei o código que a LLM fez?** — linha a linha, não só o resumo dela
2. **Eu entendo o que a LLM fez?** — se não consegue explicar para outra pessoa, ainda não entendeu
3. **O que foi gerado nessa sessão está de acordo com o desenvolvimento?** — bate com a
   arquitetura vigente, com os ADRs e com o que a task pedia
4. **Há nuances geradas pela LLM?** — coisa que ela mudou, adicionou ou "melhorou" sem ter sido
   pedida. Toda nuance é decisão implícita: ou vira decisão explícita, ou volta atrás
5. **A documentação está nos conformes sobre o que eu fiz?** — JSON, markdown espelho, ADR e
   relatório da sessão

Um "não" em qualquer pergunta **bloqueia o PR**. A ação não é aprovar mesmo assim: é voltar e
resolver o item.

## Regra nº 3 — o código de aplicação é da equipe, não da LLM

Este é um **trabalho de faculdade**. O objetivo dele não é o software existir: é os membros da
equipe aprenderem Spring, Next.js, TypeScript e banco de dados de verdade. Código gerado por LLM
e aceito sem ser escrito não ensina ninguém, e é o resultado que o projeto está tentando evitar.

**A LLM funda e configura; a equipe escreve a aplicação.**

| A LLM faz | A equipe escreve |
|---|---|
| `pom.xml`, `package.json`, `tsconfig.json`, `application.yml` | Controllers, services, entidades, repositories, DTOs |
| `docker-compose.yml`, workflows de CI, `.gitignore` | `src/api/` inteira: `client.ts`, `erros.ts`, contratos Zod |
| Documentação, ADR, README, relatórios de sessão | Componentes, hooks, telas |
| Classe de entrada e teste de fumaça da fundação | **Migrations Flyway** — é onde se aprende banco, e o [ADR-0006](docs/adr/0006-remover-supabase-infraestrutura-propria.md) saiu do Supabase justamente por isso |
| Revisar, apontar erro, comparar alternativa, explicar | Os testes do código que escreveram |

Na dúvida, a fronteira é esta: **se o arquivo tem regra de negócio ou ensina o framework, é da
equipe.** Se é encanamento para a equipe não perder a tarde com defasagem de peer dependency,
é da LLM.

O que a LLM **continua** fazendo sobre o código da equipe, e deve fazer bem: revisar linha a
linha, apontar bug e violação de convenção, explicar por que uma abordagem custa mais que a
outra, e responder pergunta. Ensinar é ajudar; entregar pronto é atrapalhar.

**Se alguém pedir para a LLM escrever código de aplicação, ela pergunta antes de escrever.**
Pode ser que a pessoa queira mesmo — mas essa é uma decisão consciente, não um deslize.

---

## Antes de escrever código

1. Leia `docs/design-sistema.md` — a arquitetura vigente está lá, não invente estrutura
2. Confira `docs/adr/` — decisão já registrada não se rediscute sem novo ADR
3. Use a skill `role-dev` para a postura e as convenções completas

---

## Convenções que mais pegam

**Pasta só existe quando há mais de um arquivo daquele tipo.** Com um arquivo só, ele fica na raiz
do módulo. Ao surgir o segundo, cria-se a pasta e movem-se **ambos** no mesmo commit.

**Backend** — `br.com.bicoemcasa.api`:

| Pasta | Papel |
|---|---|
| `config/` | Configuração da aplicação (security, CORS, OpenAPI, beans) |
| `lib/` | Adapters de serviços externos (`armazenamento/`, `email/`). **Sem regra de negócio** |
| `comum/` | Núcleo compartilhado (`excecao/`, `paginacao/`, `auditoria/`) |
| `modulos/` | Um por domínio: `autenticacao`, `usuarios`, `profissionais`, `servicos`, `contratacoes`, `avaliacoes` |

Dentro do módulo: `controller/`, `service/`, `repository/`, `models/`, `dto/`, `contrato/` e
`mapper/` (opcional). Interface fica em `contrato/` com o nome limpo (`ProfissionalService`);
implementação fica em `service/` com sufixo `Impl`. **Sem prefixo `I`.**

**Frontend** — exatamente `src/{app,components,hooks,api}`. Nada de `types/`, `services/` ou
`utils/` sem ADR. `src/api/` é a única fronteira com o backend.

---

## Nunca faça

- Retornar entidade JPA pelo controller — sempre DTO
- Injetar repository de outro módulo, ou relacionar entidades JPA cruzando módulos — referencie
  pelo `id` (`Long` ou `UUID`, conforme [ADR-0007](docs/adr/0007-chave-primaria-mista.md))
- Autorizar por claim do JWT — o papel é lido de `usuario`
- Usar H2 em teste de regra de negócio — o projeto exige Testcontainers com PostgreSQL
- Chamar `fetch` fora de `src/api/`
- Consumir resposta do backend sem validar por schema Zod
- Escrever tipo de domínio à mão em vez de `z.infer` do schema
- Usar `any` — use `unknown` com narrowing
- Alterar schema do banco por fora do Flyway — toda mudança é migration versionada
- Commitar segredo. Chave RSA de assinatura de JWT e credencial do MinIO só existem no backend

---

## Idioma e commits

Tudo em **português**: código, comentários, Javadoc, TSDoc, documentação e commits. Nomes de
código seguem o domínio em português, exceto termos técnicos consagrados (`Repository`,
`Controller`, `DTO`).

Commits em Conventional Commits, descrição no imperativo, sem ponto final, até 72 caracteres:

```
feat(profissionais): adicionar filtro de busca por cidade
docs(arquitetura): atualizar versao do Spring Boot para 4.1
```

Escopos: `backend`, `frontend`, `db`, `docs`, `ci` ou o nome do módulo.

---

## Ao final da sessão

Rode `/relatorio-sessao` para registrar o que aconteceu em `docs/relatorios/`.
Se uma decisão estrutural foi tomada, ela precisa de ADR **antes** de a sessão acabar.
