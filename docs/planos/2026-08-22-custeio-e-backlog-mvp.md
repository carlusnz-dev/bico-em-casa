# Plano — Custeio e backlog do MVP

| Campo | Valor |
|---|---|
| **Plano** | Custeio e backlog do MVP |
| **Autor** | Carlos Antunes |
| **Data** | `2026-08-22` |
| **Escopo** | Backend · Frontend · Banco · Infra · Documentação |
| **Status** | Rascunho |
| **LLM utilizada** | `Claude Opus 5 (Claude Code)` |
| **ADRs relacionados** | ADR-0003, ADR-0004, ADR-0005, ADR-0006, ADR-0007 |

---

## Contexto

A documentação está pronta: 46 requisitos versionados, matriz de rastreabilidade, arquitetura em
JSON com espelho legível, sete ADRs e o modelo de dados v4.0.0 com 18 tabelas. **Não existe uma
linha de código de aplicação.**

Antes de abrir o primeiro arquivo Java é preciso saber quanto o MVP custa — em horas e em
dinheiro — porque essa é a informação que decide o que fica de fora se o prazo apertar.

## Objetivo

Um número defensável de esforço por fase, o custo de infraestrutura mensal, e o backlog quebrado
em cards prontos para o Trello.

## Fora de Escopo

- **Estimar o que já está pronto.** Documentação e modelagem não entram na conta
- **`RF023` (chat) e demais requisitos pós-MVP** — já marcados como fora do MVP em `requisitos.json`
- **Definir o fornecedor de geocodificação** — é o ADR-0009, ainda não escrito

---

## 1. Custo em esforço

Unidade: **hora de trabalho focado**, de quem já sabe a stack. Não é hora de calendário, e não
inclui aula, reunião nem o tempo de aprender Spring Security do zero.

| Fase | Entrega | Requisitos | Horas |
|---|---|---|---:|
| **0** | Fundação | — | **46** |
| **1** | Autenticação | RF001, RF002, RNF001, RNF019–021 | **64** |
| **2** | Perfis, endereços e portfólio | RF003, RF005, RNF014, RNF017 | **66** |
| **3** | Serviços e busca | RF004, RF009, RF010, RF011, RNF005 | **46** |
| **4** | Contratações | RF008, RF012–018 | **86** |
| **5** | Avaliações e moderação | RF006, RF007, RF019–022, RNF022 | **62** |
| **6** | Fechamento | RNF015, acessibilidade, deploy | **34** |
| | | **Total** | **404 h** |

### Detalhamento

<details>
<summary><b>Fase 0 — Fundação (46 h)</b></summary>

| Item | h |
|---|---:|
| `docker-compose.yml` — PostgreSQL 18, MinIO, SMTP de desenvolvimento | 4 |
| Esqueleto Spring Boot: `config/`, security, `@RestControllerAdvice` + `ProblemDetail`, OpenAPI | 10 |
| Migrations Flyway `V1`–`V6` — 18 tabelas, enums, checks, índices | 12 |
| Esqueleto Next.js: App Router, `src/api`, Zod, layout base | 8 |
| `backend-ci.yml` e `frontend-ci.yml` | 6 |
| Base de Testcontainers + um teste de integração de referência | 6 |

</details>

<details>
<summary><b>Fase 1 — Autenticação (64 h)</b></summary>

| Item | h |
|---|---:|
| Entidades e repositories: usuário, perfil, refresh token, token de recuperação | 6 |
| Cadastro + verificação de e-mail | 8 |
| Login, emissão de JWT RSA, rotação de refresh e revogação por família (`RNF019`) | 14 |
| Recuperação de senha com token opaco de uso único (`RNF021`) | 8 |
| Limite de tentativas e resposta uniforme contra enumeração (`RNF020`) | 6 |
| Telas: cadastro, login, esqueci a senha, redefinir | 12 |
| Testes de integração | 10 |

> A fase mais subestimada do projeto. Rotação de refresh token com detecção de reuso é a parte
> que o Supabase dava pronta e que o [ADR-0006](../adr/0006-remover-supabase-infraestrutura-propria.md)
> trouxe para dentro de propósito.

</details>

<details>
<summary><b>Fase 2 — Perfis, endereços e portfólio (66 h)</b></summary>

| Item | h |
|---|---:|
| CRUD de perfil e troca de papel CLIENTE ↔ PROFISSIONAL (`RF002`) | 10 |
| Endereço + adapter de geocodificação em `lib/` (`RNF018`) | 10 |
| Portfólio, fotos e URL pré-assinada do MinIO (`RF003`, `RNF017`) | 14 |
| Disponibilidades (`RF005`) | 6 |
| Telas: meu perfil, edição, portfólio público, upload com preview | 18 |
| Testes | 8 |

</details>

<details>
<summary><b>Fase 3 — Serviços e busca (46 h)</b></summary>

| Item | h |
|---|---:|
| CRUD de serviço, tags e a N:N | 10 |
| Busca com filtro por tag, cidade, preço e nota, com paginação (`RF004`, `RNF005`) | 14 |
| Telas: catálogo, resultado de busca, detalhe do serviço | 16 |
| Testes | 6 |

</details>

<details>
<summary><b>Fase 4 — Contratações (86 h)</b></summary>

| Item | h |
|---|---:|
| Máquina de estados + `contratacao_historico` (`RF008`, `RF016`) | 14 |
| Solicitação de orçamento com anexos (`RF014`, `RF015`) | 10 |
| Aceite / recusa com valor proposto (`RF017`) | 8 |
| Cancelamento com motivo (`RF018`) | 4 |
| Cálculo e congelamento de `distancia_km` (`RF013`) | 8 |
| Notificações (`RF012`) | 8 |
| Telas: solicitar, minhas contratações, detalhe, painel do profissional | 22 |
| Testes | 12 |

> A fase mais cara, e é a certa para ser. É o fluxo que o produto existe para entregar.

</details>

<details>
<summary><b>Fase 5 — Avaliações e moderação (62 h)</b></summary>

| Item | h |
|---|---:|
| Avaliação, média e exibição (`RF006`, `RF007`, `RF019`) | 8 |
| Denúncia e fila do administrador (`RF020`) | 10 |
| Painel do admin: suspender e analisar (`RF021`, `RF022`) | 12 |
| Log de auditoria transversal (`RNF022`) | 8 |
| Telas: avaliar, ver avaliações, painel admin | 16 |
| Testes | 8 |

</details>

<details>
<summary><b>Fase 6 — Fechamento (34 h)</b></summary>

| Item | h |
|---|---:|
| Anonimização de conta — LGPD (`RNF015`) | 8 |
| Acessibilidade e responsividade | 10 |
| Deploy, documentação final e apresentação | 16 |

</details>

### O que 404 h significam no calendário

| Cenário | Capacidade semanal | Semanas |
|---|---|---:|
| 3 pessoas × 8 h/semana | 24 h | **17** |
| 4 pessoas × 10 h/semana | 40 h | **10** |
| 2 pessoas × 12 h/semana | 24 h | **17** |

> [!WARNING]
> Estimativa de time acadêmico erra para baixo com frequência. Trate 404 h como **piso**, não
> como meta. Se o prazo for menor que o cenário escolhido, o corte natural é a **Fase 5** —
> avaliação e moderação são o que menos quebra a demonstração do fluxo principal.

---

## 2. Custo em dinheiro

Consequência direta do [ADR-0006](../adr/0006-remover-supabase-infraestrutura-propria.md): sem
BaaS, a infraestrutura é nossa.

| Item | Opção | Custo mensal |
|---|---|---:|
| VPS (API + PostgreSQL + MinIO) | 4 vCPU / 8 GB, Hetzner ou Contabo | R$ 50 – 90 |
| Domínio `.com.br` | Registro.br, R$ 40/ano | ~R$ 4 |
| SMTP transacional | Brevo — 300 e-mails/dia no plano gratuito | R$ 0 |
| Geocodificação | Nominatim (OSM), 1 req/s, uso não comercial | R$ 0 |
| Figma, Trello, GitHub | Planos gratuitos | R$ 0 |
| **Total** | | **~R$ 55 – 95/mês** |

**Em desenvolvimento o custo é zero:** tudo sobe no `docker-compose` local.

Onde isso muda: se a geocodificação sair do Nominatim para o Google Maps, entra em ~US$ 5 por
1.000 requisições — daí a importância de **persistir a coordenada** e geocodificar uma vez por
endereço, nunca a cada busca. Essa é a decisão do ADR-0009.

---

## 3. Backlog do MVP

Épicos na ordem de execução. Cada linha vira um card.

### Épico A — Fundação · Fase 0

| Card | Requisito | h |
|---|---|---:|
| Subir `docker-compose` com PostgreSQL 18, MinIO e SMTP | — | 4 |
| Escrever migrations Flyway `V1`–`V6` a partir do `modelo-dados.dbml` | — | 12 |
| Esqueleto Spring Boot: `config/`, exception handler, OpenAPI | — | 10 |
| Esqueleto Next.js: App Router, `src/api`, Zod | — | 8 |
| `backend-ci.yml` e `frontend-ci.yml` | RNF010 | 6 |
| Base de Testcontainers + teste de integração de referência | RNF011 | 6 |

### Épico B — Autenticação · Fase 1

| Card | Requisito | h |
|---|---|---:|
| Módulo `autenticacao`: entidades, repositories, migrations conferidas | RF001 | 6 |
| Cadastro com verificação de e-mail | RF001 | 8 |
| Login + JWT RSA + rotação de refresh com revogação por família | RNF019 | 14 |
| Recuperação de senha com token opaco de uso único | RNF021 | 8 |
| Limite de tentativas e resposta uniforme (anti-enumeração) | RNF020 | 6 |
| Telas de cadastro, login e recuperação | RF001 | 12 |
| Testes de integração de autenticação | RNF011 | 10 |

### Épico C — Perfis e portfólio · Fase 2

| Card | Requisito | h |
|---|---|---:|
| CRUD de perfil e troca de papel CLIENTE ↔ PROFISSIONAL | RF002 | 10 |
| Endereço + adapter de geocodificação em `lib/` | RF013, RNF018 | 10 |
| Portfólio e galeria com URL pré-assinada do MinIO | RF003, RNF017 | 14 |
| Disponibilidade por dia e faixa de horário | RF005 | 6 |
| Telas de perfil, edição e portfólio público | RF003 | 18 |
| Testes | RNF011 | 8 |

### Épico D — Serviços e busca · Fase 3

| Card | Requisito | h |
|---|---|---:|
| CRUD de serviço e tags (N:N) | RF010, RF011 | 10 |
| Busca com filtro e paginação, alvo de 2 s no p95 | RF004, RNF005 | 14 |
| Telas de catálogo, busca e detalhe do serviço | RF004, RF009 | 16 |
| Testes | RNF011 | 6 |

### Épico E — Contratações · Fase 4

| Card | Requisito | h |
|---|---|---:|
| Máquina de estados da contratação + histórico de transições | RF008, RF016 | 14 |
| Solicitação de orçamento com anexos | RF014, RF015 | 10 |
| Aceite e recusa com valor proposto | RF017 | 8 |
| Cancelamento com motivo e autor | RF018 | 4 |
| Cálculo e congelamento da distância em km | RF013 | 8 |
| Notificações de aceite e recusa | RF012 | 8 |
| Telas de solicitação, lista e painel do profissional | RF008 | 22 |
| Testes | RNF011 | 12 |

### Épico F — Avaliações e moderação · Fase 5

| Card | Requisito | h |
|---|---|---:|
| Avaliação com nota e comentário, liberada só em CONCLUIDA | RF006, RF007 | 8 |
| Média e exibição da reputação | RF019 | — |
| Denúncia de serviço, perfil ou avaliação | RF020 | 10 |
| Painel do admin: fila de denúncias e suspensão | RF021, RF022 | 12 |
| Log de auditoria transversal | RNF022 | 8 |
| Telas de avaliação e painel do admin | RF007 | 16 |
| Testes | RNF011 | 8 |

### Épico G — Fechamento · Fase 6

| Card | Requisito | h |
|---|---|---:|
| Anonimização de conta preservando histórico | RNF015 | 8 |
| Passada de acessibilidade e responsividade | RNF008 | 10 |
| Deploy, documentação final e apresentação | — | 16 |

### Épico H — Design e documentação (paralelo, não bloqueia código)

| Card | Origem |
|---|---|
| Criar projeto no Figma e iniciar o trabalho de logo | Sessão 2026-08-22 |
| Definir identidade visual: paleta, tipografia, tom | Sessão 2026-08-22 |
| Wireframes de baixa fidelidade das telas do fluxo principal | Sessão 2026-08-22 |
| Refinar a documentação no Drive — alinhar com `requisitos.md` e `design-sistema.md` | Sessão 2026-08-22 |
| Corrigir o quadro "é – não é – faz – não faz" do PDF: promete chat, que saiu do MVP | Pendência herdada |
| Reescrever os critérios de aceite das User Stories | Pendência herdada |
| Atualizar o MOC do Obsidian para refletir o ADR-0006 | Pendência herdada |

---

## 4. Pré-requisitos antes de começar a Fase 0

- [ ] Abrir o PR de `docs/requisitos-e-matriz-rastreabilidade` para `main` e ver o
      `docs-parity.yml` rodar contra uma mudança real
- [ ] Escrever o **ADR-0009** da geocodificação — `RF013` é o único requisito do MVP sem caminho
      técnico definido
- [ ] Fechar as pendências pequenas: `LICENSE` vazio, `.gitignore` sem `target/` e `.next/`,
      ADR do `utils/` no frontend

---

## Referências

- [`modelo-dados.md`](../modelo-dados.md) — o modelo v4.0.0 explicado
- [`requisitos.md`](../requisitos.md) — os 46 requisitos
- [`matriz-rastreabilidade.md`](../matriz-rastreabilidade.md)
- [ADR-0006](../adr/0006-remover-supabase-infraestrutura-propria.md) — a origem do custo de infra
- [ADR-0007](../adr/0007-chave-primaria-mista.md)
