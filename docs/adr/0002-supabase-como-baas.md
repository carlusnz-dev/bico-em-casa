# ADR-0002 — Supabase como BaaS (Postgres + Auth + Storage)

| Campo | Valor |
|---|---|
| **ADR** | `0002` |
| **Título** | Supabase como BaaS (Postgres + Auth + Storage) |
| **Autor** | Carlos Antunes |
| **Data** | 2026-08-22 |
| **Tópico** | Infra |
| **Status** | **Substituído por [ADR-0006](./0006-remover-supabase-infraestrutura-propria.md)** (2026-08-22) |
| **LLM utilizada** | `Claude Opus 5 (Claude Code)` |

---

> [!WARNING]
> **Esta decisão não vale mais.** O Supabase foi removido do MVP pelo
> [ADR-0006](./0006-remover-supabase-infraestrutura-propria.md), no mesmo dia em que este ADR foi
> escrito.
>
> O texto abaixo é preservado como registro histórico: o raciocínio continua tecnicamente correto,
> e o ADR-0006 o contesta pelo objetivo do projeto, não pela técnica. Ler os dois em sequência é a
> melhor forma de entender o trade-off entre velocidade de entrega e profundidade de aprendizado.

---

## Contexto

O Bico em Casa precisa de três capacidades de infraestrutura antes de qualquer regra de
negócio existir:

1. **Banco relacional** — PostgreSQL, já definido na stack
2. **Identidade** — cadastro, login, refresh token, recuperação de senha para clientes e profissionais
3. **Armazenamento de arquivos** — imagens do portfólio dos profissionais e fotos de perfil

É um projeto acadêmico com equipe pequena e prazo de semestre. Construir identidade do zero
(hash de senha, fluxo de recuperação, rotação de refresh token) é trabalho que consome sprints
inteiras e cujo resultado, feito às pressas, costuma ser pior que o de um provedor pronto —
justamente na parte em que errar é mais caro.

Armazenamento de imagem também não é problema de negócio do projeto: servir arquivo estático
com URL assinada é infraestrutura resolvida.

## Decisão

Adotar o **Supabase** como Backend as a Service, usando três capacidades:

| Capacidade | Uso |
|---|---|
| **Database** | PostgreSQL 18 gerenciado, acessado pelo backend via JDBC e versionado por Flyway |
| **Auth** | Emissor da identidade. O backend Spring atua como **OAuth2 Resource Server**, validando o JWT contra o JWKS do projeto |
| **Storage** | Bucket `portfolios` para imagens de portfólio e `avatares` para fotos de perfil |

**Duas capacidades são deliberadamente recusadas:**

- **RLS como autorização primária.** A autorização de negócio é do backend Spring. O RLS entra
  apenas como defesa em profundidade, nunca como única barreira.
- **Acesso direto via PostgREST.** O frontend **não** lê tabelas de domínio pelo `supabase-js`.
  Todo dado de negócio passa pela API Spring. No cliente, o `supabase-js` serve exclusivamente
  ao fluxo de autenticação e ao upload assinado.

O adapter fica confinado em `br.com.bicoemcasa.api.lib.supabase` no backend e em `src/api/`
no frontend.

## Alternativas Consideradas

| Opção | Prós | Contras | Por que foi recusada |
|---|---|---|---|
| PostgreSQL self-hosted + auth própria (JWT com JJWT) | Controle total, zero lock-in | Implementar hash de senha, recuperação, rotação de refresh token e storage de arquivo consome semanas e é onde erros de segurança custam caro | Custo alto em trabalho que não é o problema de negócio do projeto |
| Supabase só como Postgres gerenciado | Menor lock-in de identidade | Auth e storage continuariam por construir — o custo acima permanece | Resolve o problema mais fácil e deixa os dois difíceis |
| Firebase / Auth0 + Postgres separado | Auth maduro | Dois fornecedores, dois SDKs, dois faturamentos; Firestore não é relacional | Complexidade operacional maior sem ganho correspondente |
| Supabase como BaaS **completo**, com RLS e PostgREST no frontend | Menos código de backend | A regra de negócio migraria para políticas SQL, dispersa e difícil de testar; o backend Spring perderia o sentido | Contraria a Clean Architecture adotada; regra de negócio precisa ser testável em Java |

## Consequências

### Positivas

- Identidade e storage prontos desde o primeiro dia, sem sprint gasta em infraestrutura
- `jjwt` sai das dependências: o backend só **valida** o token, não o emite
- Backup, alta disponibilidade e patch do Postgres viram responsabilidade do fornecedor
- O isolamento em `lib/supabase` mantém o custo de troca de fornecedor contido numa pasta

### Negativas

- Dependência de fornecedor externo para o login funcionar — se o Supabase cair, ninguém entra
- Lock-in real na identidade: migrar usuários entre provedores de auth é doloroso
- O schema `auth` é do Supabase e fica fora do controle do Flyway, criando duas fontes de
  verdade sobre "quem é o usuário" — resolvido correlacionando `sub` com `tb_usuarios.supabase_user_id`
- Free tier tem limites de projeto pausado por inatividade, relevante num projeto acadêmico

### Neutras

- A autorização por papel (`CLIENTE`, `PROFISSIONAL`, `ADMIN`) fica no backend, lida de
  `tb_usuarios`, e **nunca** de claim do JWT — claim de cliente não é fonte de autoridade
- `SUPABASE_SERVICE_ROLE_KEY` existe apenas no backend e jamais é exposta ao frontend

## Impacto na Arquitetura

Esta decisão altera `docs/arquitetura-sistema.json`?

- [x] Sim — campos alterados: `external_services.supabase`, `backend.security_configuration`,
  `backend.dependencies.security`, `backend.dependencies.removed`, `database.hosting`,
  `database.schemas`, `frontend.dependencies.backend_integration`
- [ ] Não

## Referências

- [Supabase Auth — JWT e JWKS](https://supabase.com/docs/guides/auth)
- [Spring Security — OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
