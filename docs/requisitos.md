# Requisitos — Bico em Casa

| Campo | Valor |
|---|---|
| **Versão** | 1.0.0 |
| **Última revisão** | 2026-08-22 |
| **Fonte da verdade** | [`requisitos.json`](./requisitos.json) |
| **Matriz derivada** | [`matriz-rastreabilidade.md`](./matriz-rastreabilidade.md) |
| **Escopo** | MVP acadêmico — PUCPR, Engenharia de Software |

---

> [!IMPORTANT]
> **`requisitos.json` é a fonte da verdade dos requisitos.** Este markdown é o espelho legível.
>
> **Ordem obrigatória de alteração:** editar o JSON → refletir aqui → rodar `/revisar-matriz`.
> As três alterações vão **no mesmo commit**.
>
> Essa é a mesma regra que vale para `arquitetura-sistema.json` e `design-sistema.md`, aplicada a
> um par de arquivos diferente. Requisito **não** entra em `arquitetura-sistema.json` — arquitetura
> e requisito mudam por motivos diferentes e em ritmos diferentes.

---

## Como ler este documento

**Código.** `RF` para requisito funcional, `RNF` para não funcional. O código é **estável**: uma vez
atribuído, nunca é reaproveitado nem renumerado, mesmo que o requisito saia de escopo. Requisito
descartado vira `pos-mvp` ou ganha uma nota, nunca some deixando o número livre.

**Módulo.** É sempre um dos seis módulos reais do backend definidos no
[ADR-0004](./adr/0004-estrutura-modular-por-dominio.md) — `autenticacao`, `usuarios`,
`profissionais`, `servicos`, `contratacoes`, `avaliacoes` — ou `transversal`, para o que não
pertence a nenhum. Não existe módulo "Perfil" nem "Administrador": esses nomes vinham do rascunho
de aula e não têm correspondência no código.

**Status.** `mvp` entra na primeira entrega. `pos-mvp` está aprovado como ideia mas fora do escopo
atual — fica registrado para não ser redescoberto do zero depois.

---

## 1. Requisitos Funcionais — MVP

| Código | Descrição | Módulos | Criado em |
|---|---|---|---|
| **RF001** | O sistema deve permitir cadastro, login, recuperação de senha e encerramento de sessão dos usuários. | autenticacao | 2026-08-19 |
| **RF002** | O sistema deve atribuir a uma conta um ou mais papéis entre cliente, profissional e administrador, permitindo que a mesma pessoa contrate e preste serviços. | autenticacao, usuarios | 2026-08-19 |
| **RF003** | O sistema deve permitir que o profissional monte um portfólio com foto de capa e galeria de imagens dos trabalhos já realizados. | profissionais | 2026-08-19 |
| **RF004** | O sistema deve permitir que o cliente filtre a busca de serviços por categoria, faixa de preço e localização. | servicos, profissionais | 2026-08-19 |
| **RF005** | O sistema deve exibir o status de disponibilidade do profissional e os horários em que ele atende. | profissionais | 2026-08-19 |
| **RF006** | O sistema deve permitir que o cliente avalie com nota de 1 a 5 o profissional de uma contratação que foi concluída. | avaliacoes | 2026-08-19 |
| **RF007** | O sistema deve permitir que o cliente escreva um comentário junto da avaliação do profissional que contratou. | avaliacoes | 2026-08-19 |
| **RF008** | O sistema deve exibir ao cliente a etapa atual da contratação e o histórico de transições de status. | contratacoes | 2026-08-19 |
| **RF009** | O sistema deve listar os profissionais em tabela paginada, ordenada por disponibilidade e nota média por padrão. | profissionais, servicos | 2026-08-19 |
| **RF010** | O sistema deve permitir que o profissional cadastre serviços pré-definidos com título, descrição, categoria e preço de referência. | servicos | 2026-08-19 |
| **RF011** | O sistema deve calcular e exibir o preço médio praticado por categoria de serviço na plataforma. | servicos | 2026-08-19 |
| **RF012** | O sistema deve notificar o cliente quando a sua solicitação for aceita ou recusada pelo profissional. | contratacoes | 2026-08-19 |
| **RF013** | O sistema deve calcular e exibir a distância em quilômetros entre o endereço do serviço e a base de atendimento do profissional. | profissionais, contratacoes | 2026-08-19 |
| **RF014** | O sistema deve permitir que o cliente envie ao profissional uma solicitação de orçamento descrevendo o serviço desejado. | contratacoes | 2026-08-19 |
| **RF015** | O sistema deve permitir que o cliente anexe fotos à solicitação de orçamento para ilustrar o problema. | contratacoes | 2026-08-19 |
| **RF016** | O sistema deve persistir e exibir ao cliente o histórico das contratações que ele já realizou. | contratacoes | 2026-08-19 |
| **RF017** | O sistema deve permitir que o profissional aceite ou recuse uma solicitação de orçamento recebida, respondendo com o valor proposto. | contratacoes | 2026-08-22 |
| **RF018** | O sistema deve permitir que cliente ou profissional cancele uma contratação ainda não concluída, registrando o motivo. | contratacoes | 2026-08-22 |
| **RF019** | O sistema deve calcular e exibir a nota média do profissional e a quantidade de avaliações recebidas. | avaliacoes, profissionais | 2026-08-22 |
| **RF020** | O sistema deve permitir que qualquer usuário denuncie um serviço, perfil ou avaliação inadequado, e que o administrador analise a denúncia. | servicos, usuarios | 2026-08-22 |
| **RF021** | O sistema deve permitir que o administrador liste os usuários da plataforma filtrando por ativos e inativos. | usuarios | 2026-08-22 |
| **RF022** | O sistema deve permitir que o administrador suspenda e reative contas de usuário, registrando o motivo da ação. | usuarios | 2026-08-22 |

## 2. Requisitos Funcionais — fora do MVP

Aprovados como ideia, adiados por custo. Registrados para que a decisão não se perca.

| Código | Descrição | Módulos | Criado em |
|---|---|---|---|
| **RF023** | O sistema deve oferecer um chat entre cliente e profissional para negociação do serviço. | contratacoes | 2026-08-22 |
| **RF024** | O sistema deve permitir o envio de mensagens de áudio na conversa entre cliente e profissional. | contratacoes | 2026-08-19 |

> [!NOTE]
> **Por que o chat ficou de fora.** O quadro "é – não é – faz – não faz" do PDF de especificação
> promete "disponibiliza chats para negociação". Chat em tempo real é um subsistema completo —
> WebSocket, presença, histórico, moderação, notificação de mensagem não lida — e não existe módulo
> `mensagens` na arquitetura. No MVP, a negociação acontece pela solicitação de orçamento (RF014)
> com fotos anexadas (RF015) e a resposta do profissional (RF017). **O quadro do PDF precisa ser
> corrigido antes da entrega**, senão a especificação promete o que o produto não faz.

---

## 3. Requisitos Não Funcionais

Quase todos já eram decisão tomada — estavam espalhados pelos ADRs e pelo `design-sistema.md` sem
nunca terem sido escritos como requisito. Aqui eles viram requisito verificável, com a origem
apontada.

| Código | Descrição | Módulos | Origem |
|---|---|---|---|
| **RNF001** | A identidade é gerenciada pelo Supabase Auth. O backend valida o JWT por JWKS e nunca armazena senha ou hash de senha. | autenticacao, transversal | ADR-0002 |
| **RNF002** | A autorização é decidida pelo papel lido da base de dados, nunca por claim do JWT. | autenticacao, usuarios | CLAUDE.md, seção Nunca faça |
| **RNF003** | Todo tráfego entre frontend, backend e Supabase ocorre sobre HTTPS com TLS 1.2 ou superior. | transversal | Revisão de requisitos 2026-08-22 |
| **RNF004** | Nenhum segredo é versionado. A SUPABASE_SERVICE_ROLE_KEY existe apenas no backend e nunca é exposta ao navegador. | transversal | ADR-0002; CLAUDE.md |
| **RNF005** | A busca de profissionais e serviços responde em até 2 segundos no percentil 95 com 10 mil registros na base. | servicos, profissionais | Revisão de requisitos 2026-08-22 |
| **RNF006** | Toda listagem da API é paginada, com máximo de 50 itens por página, usando os tipos de `comum/paginacao`. | transversal | design-sistema.md §10.2 |
| **RNF007** | A interface é responsiva e utilizável de 360px de largura até desktop, sem rolagem horizontal. | transversal | Revisão de requisitos 2026-08-22 |
| **RNF008** | Os fluxos de cadastro, busca e contratação atendem ao WCAG 2.1 nível AA, incluindo navegação por teclado e contraste mínimo. | transversal | Revisão de requisitos 2026-08-22 |
| **RNF009** | Todo erro da API é devolvido como ProblemDetail no formato RFC 9457, sem expor stack trace nem detalhe interno. | transversal | design-sistema.md §9.1 |
| **RNF010** | Toda alteração de schema nasce como migration versionada do Flyway. Alterar schema pelo painel do Supabase é proibido. | transversal | design-sistema.md §5 |
| **RNF011** | Teste de regra de negócio roda contra PostgreSQL real via Testcontainers. H2 é proibido. | transversal | design-sistema.md §3.3 |
| **RNF012** | Código, comentários, documentação e mensagens de commit são escritos em português, com commits em Conventional Commits. | transversal | design-sistema.md §7.2 |
| **RNF013** | A aplicação funciona nas duas versões mais recentes de Chrome, Firefox, Safari e Edge. | transversal | Revisão de requisitos 2026-08-22 |
| **RNF014** | CPF, telefone e endereço completo só são visíveis ao próprio titular e ao administrador. O cliente vê do profissional apenas dados públicos. | usuarios, profissionais | LGPD, Lei 13.709/2018 |
| **RNF015** | O usuário pode solicitar a exclusão da conta. Os dados pessoais são anonimizados preservando o histórico de contratações e as avaliações. | usuarios | LGPD, Lei 13.709/2018 |
| **RNF016** | Toda tabela de domínio registra `criado_em` e `atualizado_em` pela MappedSuperclass de `comum/auditoria`. | transversal | design-sistema.md §10.2 |
| **RNF017** | Imagens de portfólio e anexos ficam no Supabase Storage, limitados a 5 MB por arquivo nos formatos JPEG, PNG e WebP. | profissionais, contratacoes | design-sistema.md §6.1 |
| **RNF018** | O cálculo de distância do RF013 usa coordenadas persistidas na base. A geocodificação de endereço é feita por adapter isolado em `lib/`, nunca chamada de dentro de um módulo. | profissionais, contratacoes | Decorrência da decisão de manter RF013 no MVP; pendente de ADR-0006 |

---

## 4. O que mudou em relação à tabela original

Esta seção existe para o grupo. Cada linha da tabela de aula que foi alterada está aqui, com o
motivo. Nada foi mudado em silêncio.

### 4.1 Defeitos corrigidos

| Problema | O que era | O que virou |
|---|---|---|
| **Código duplicado** | Dois requisitos diferentes com o código `RF016` — "enviar áudio" e "persistir histórico" | O histórico manteve `RF016`; o áudio virou `RF024`. Escolha do áudio para o código novo porque ele saiu do MVP, deixando `RF001`–`RF022` contíguos |
| **Módulos inexistentes** | *Portfólio*, *Perfil*, *Profissional*, *Serviço*, *Administrador*, *Pesquisa do serviço* | Remapeados para os seis módulos do ADR-0004. Uma matriz que aponta para módulo que não existe no código não rastreia nada |
| **Módulos em branco** | `RF013` a `RF016` sem módulo | Todos preenchidos |
| **`RF011` no módulo errado** | "Calcular preço médio" atribuído a *Perfil* | Passou para `servicos` — preço é atributo de serviço, não de pessoa |
| **`RF008` no módulo errado** | "Status do serviço contratado" atribuído a *Serviço* | Passou para `contratacoes`. Serviço é a oferta do catálogo; contratação é o trabalho contratado. São entidades distintas |
| **`RF014` descrito como UI** | "deve ter um **botão** para solicitar orçamento" | "deve permitir que o cliente **envie** uma solicitação de orçamento". Requisito descreve capacidade, não widget |
| **`RF012` com erro de digitação** | "se o serviço **deve foi** aceito" | Reescrito |
| **Datas inconsistentes** | "19 de agosto, 2026" e "19 de agosto,2026" | ISO 8601 — `2026-08-19` |

### 4.2 Requisitos novos

Seis vieram do canvas PBB e da revisão. Estavam no quadro da aula mas nunca chegaram à tabela.

| Código | De onde veio |
|---|---|
| `RF017` — profissional aceita ou recusa | **Lacuna lógica.** `RF012` notificava o cliente de um aceite que nenhum requisito permitia acontecer |
| `RF018` — cancelar contratação | PBB, cartão "Cancelar serviço" |
| `RF019` — nota média do profissional | PBB, cartão "Visualizar nota média do profissional". `RF006` cria a avaliação; nada dizia que a média seria exibida |
| `RF020` — denunciar conteúdo inadequado | PBB, cartão "Denunciar serviços inadequados" |
| `RF021` — admin lista usuários ativos/inativos | PBB, cartão "Visualizar usuários ativos e inativos no site" |
| `RF022` — admin suspende e reativa contas | PBB, bloco "Gerenciamento de usuários" |

### 4.3 Decisões de escopo

| Decisão | Efeito |
|---|---|
| **Uma conta pode ter mais de um papel** | `RF002` reescrito. O enum único `tipo` do rascunho de schema impedia um pintor de contratar um chaveiro. Corrigir depois exige migrar dados de produção |
| **Distância em km fica no MVP** | `RF013` mantido. Exige coordenadas persistidas e geocodificação de endereço — ver `RNF018`. **Precisa de ADR-0006** para o serviço externo de geocodificação |
| **Chat fica fora do MVP** | `RF023` e `RF024` marcados `pos-mvp` |
| **`RF015` é anexo, não chat** | Foto passa a ser anexo da solicitação de orçamento. Sobrevive ao corte do chat porque não depende dele |

---

## 5. Pendências

- [ ] **ADR-0006** para o serviço externo de geocodificação exigido por `RF013` / `RNF018`
- [ ] **Corrigir o quadro "é – não é – faz – não faz"** no PDF de especificação: ele promete chat,
      que saiu do MVP
- [ ] **Critérios de aceite.** O PDF tem oito histórias de usuário, mas sete delas repetem
      literalmente os critérios do exemplo de filtro por data — inclusive nas histórias de
      profissional e administrador, onde o campo `POSSO` está vazio. Precisam ser reescritas
- [ ] **Validar `RNF005`** (busca em 2s no p95) quando houver código. É uma meta declarada, ainda
      não medida
- [ ] O campo `entidades` do JSON é **projeção**, não fato — as migrations ainda não existem.
      Revisar quando o schema for escrito

---

## Referências

- [`arquitetura-sistema.json`](./arquitetura-sistema.json) — fonte da verdade da arquitetura
- [`design-sistema.md`](./design-sistema.md) — arquitetura em formato legível
- [`adr/0004-estrutura-modular-por-dominio.md`](./adr/0004-estrutura-modular-por-dominio.md) — origem dos seis módulos
- `BES-Especificação do Projeto - Bico em Casa.pdf` — artefatos de aula (3 Objetivos, Visão de Produto, PBB, User Stories)
- `PBB_bico-em-casa.png` — canvas PBB em resolução legível
