# Bico em Casa

Plataforma de contratação de profissionais autônomos para serviços rápidos. O cliente encontra o
profissional, vê o portfólio (opcional) e contrata sem burocracia.

**Stack:** Java 21 + Spring Boot 4.1 (Maven) · Next.js 16.3 + TypeScript 7 · PostgreSQL 18 e
identidade via Supabase.

O projeto ainda não tem código de aplicação — a fundação é documentação e arquitetura.

---

## Regra nº 1 — paridade da documentação

`docs/arquitetura-sistema.json` é a **única fonte da verdade** da arquitetura.
`docs/design-sistema.md` é o espelho legível dele.

**Ordem obrigatória:** alterar o JSON → refletir no markdown → ADR se for estrutural.
As duas alterações vão **no mesmo commit**. Editar o markdown sem antes editar o JSON é proibido.

Um hook `PostToolUse` (`.claude/hooks/paridade-docs.sh`) lembra disso automaticamente, e o
workflow `docs-parity.yml` falha o PR se a paridade for quebrada.

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
| `lib/` | Adapters de serviços externos (`supabase/`). **Sem regra de negócio** |
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
- Injetar repository de outro módulo, ou relacionar entidades JPA cruzando módulos (use o UUID)
- Autorizar por claim do JWT — o papel é lido de `tb_usuarios`
- Usar H2 em teste de regra de negócio — o projeto exige Testcontainers com PostgreSQL
- Chamar `fetch` ou `supabase-js` fora de `src/api/`
- Consumir resposta do backend sem validar por schema Zod
- Escrever tipo de domínio à mão em vez de `z.infer` do schema
- Usar `any` — use `unknown` com narrowing
- Alterar schema do banco pelo painel do Supabase — toda mudança é migration do Flyway
- Commitar segredo. `SUPABASE_SERVICE_ROLE_KEY` só existe no backend

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
