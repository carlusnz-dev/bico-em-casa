# Matriz de Rastreabilidade — Bico em Casa

| Campo | Valor |
|---|---|
| **Gerado a partir de** | [`requisitos.json`](./requisitos.json) |
| **Última sincronização** | 2026-09-20 |
| **Comando de manutenção** | `/revisar-matriz` |

---

> [!WARNING]
> **Este arquivo é derivado.** Não edite à mão.
> Altere [`requisitos.json`](./requisitos.json), reflita em [`requisitos.md`](./requisitos.md) e
> rode `/revisar-matriz` para regenerar esta matriz.
>
> Uma matriz editada à mão diverge da fonte no primeiro requisito novo, e uma matriz que diverge
> é pior que nenhuma: ela dá a falsa sensação de que o rastro existe.

---

## Para que serve

A matriz responde três perguntas que aparecem sempre e nunca têm resposta pronta:

1. **"Esse requisito está implementado onde?"** — do requisito para o módulo e para a tabela
2. **"Se eu mexer neste módulo, o que quebra?"** — do módulo de volta para os requisitos
3. **"Esse módulo tem requisito que o justifique?"** — módulo sem requisito é escopo inventado;
   requisito sem módulo é escopo esquecido

O campo **Entidades** deixou de ser projeção: as tabelas estão definidas em
[`modelo-dados.dbml`](./modelo-dados.dbml). Vira fato pleno quando as migrations do Flyway
existirem — o nome da tabela já é o definitivo, o DDL ainda não foi escrito.

---

## 1. Requisito → Módulo → Entidade

| Código | Módulos | Entidades previstas | Status | Origem |
|---|---|---|---|---|
| **RF001** | autenticacao | usuario, refresh_token, token_recuperacao | mvp | Tabela de requisitos do grupo (revisado pelo ADR-0006: autenticação própria) |
| **RF002** | autenticacao, usuarios | usuario, perfil | mvp | Tabela de requisitos do grupo (revisado: papéis múltiplos via um perfil por papel) |
| **RF003** | profissionais | perfil, portfolio, portfolio_foto | mvp | Tabela de requisitos do grupo |
| **RF004** | servicos, profissionais | servico, tag, servico_tag, perfil | mvp | Tabela de requisitos do grupo; PBB (Pesquisa serviço) |
| **RF005** | profissionais | perfil, disponibilidade | mvp | Tabela de requisitos do grupo |
| **RF006** | avaliacoes | avaliacao, contratacao | mvp | Tabela de requisitos do grupo |
| **RF007** | avaliacoes | avaliacao | mvp | Tabela de requisitos do grupo |
| **RF008** | contratacoes | contratacao, contratacao_historico | mvp | Tabela de requisitos do grupo; PBB (Ver status do serviço realizado) |
| **RF009** | profissionais, servicos | perfil, avaliacao | mvp | Tabela de requisitos do grupo; PBB (Ordenar por notas de avaliações) |
| **RF010** | servicos | servico, tag, servico_tag | mvp | Tabela de requisitos do grupo; PBB (Cadastrar serviço prestado) |
| **RF011** | servicos | servico, tag | mvp | Tabela de requisitos do grupo; PBB (Visualizar preços pré-definidos) |
| **RF012** | contratacoes | contratacao, notificacao | mvp | Tabela de requisitos do grupo |
| **RF013** | profissionais, contratacoes | perfil, endereco, contratacao | mvp | Tabela de requisitos do grupo |
| **RF014** | contratacoes | contratacao | mvp | Tabela de requisitos do grupo; PBB (Enviar solicitação de serviço) |
| **RF015** | contratacoes | contratacao, contratacao_anexo | mvp | Tabela de requisitos do grupo (revisado: anexo na solicitação, não em chat) |
| **RF016** | contratacoes | contratacao | mvp | Tabela de requisitos do grupo (era o segundo RF016 duplicado) |
| **RF017** | contratacoes | contratacao | mvp | Lacuna encontrada na revisão: RF012 notificava um aceite que nenhum requisito criava |
| **RF018** | contratacoes | contratacao | mvp | PBB (Cancelar serviço) |
| **RF019** | avaliacoes, profissionais | avaliacao, perfil | mvp | PBB (Visualizar nota média do profissional) |
| **RF020** | denuncias, servicos, usuarios | denuncia | mvp | PBB (Denunciar serviços inadequados) |
| **RF021** | usuarios | usuario | mvp | PBB (Visualizar usuários ativos e inativos no site) |
| **RF022** | usuarios | usuario, log_acao | mvp | PBB (Gerenciamento de usuários) |
| **RF023** | contratacoes | conversa, mensagem | pos-mvp | PDF de especificação, quadro é–não é–faz–não faz; PBB (Falar com o profissional) |
| **RF024** | contratacoes | mensagem | pos-mvp | Tabela de requisitos do grupo (era o primeiro RF016 duplicado) |
| **RF025** | servicos | servico, tag, servico_tag | mvp | PBB (Gerenciamento de serviços); docs/historias-usuario-administrador-servicos.md, HU 11 |
| **RF026** | servicos | servico, log_acao | mvp | PBB (Gerenciamento de serviços); docs/historias-usuario-administrador-servicos.md, HU 12 |
| **RF027** | servicos | tag, servico_tag, servico, log_acao | mvp | PBB (Gerenciamento de serviços); docs/historias-usuario-administrador-servicos.md, HU 13 |
| **RF030** | servicos | servico, contratacao | mvp | Consenso da equipe em 2026-09-18, ao revisar a implementação de ativar/desativar/deletar em ServicoServiceImpl |
| **RNF001** | autenticacao | usuario | mvp | ADR-0006 |
| **RNF002** | autenticacao, usuarios | usuario, perfil | mvp | ADR-0006; CLAUDE.md, seção Nunca faça |
| **RNF003** | transversal | — | mvp | Revisão de requisitos 2026-08-22 |
| **RNF004** | transversal | — | mvp | ADR-0006 |
| **RNF005** | servicos, profissionais | servico, perfil | mvp | Revisão de requisitos 2026-08-22 |
| **RNF006** | transversal | — | mvp | design-sistema.md §11.2 |
| **RNF007** | transversal | — | mvp | Revisão de requisitos 2026-08-22 |
| **RNF008** | transversal | — | mvp | Revisão de requisitos 2026-08-22 |
| **RNF009** | transversal | — | mvp | design-sistema.md §10.1 |
| **RNF010** | transversal | — | mvp | design-sistema.md §5 |
| **RNF011** | transversal | — | mvp | design-sistema.md §3.3 |
| **RNF012** | transversal | — | mvp | design-sistema.md §8.2 |
| **RNF013** | transversal | — | mvp | Revisão de requisitos 2026-08-22 |
| **RNF014** | usuarios, profissionais | usuario, endereco | mvp | LGPD, Lei 13.709/2018 |
| **RNF015** | usuarios | usuario, contratacao, avaliacao | mvp | LGPD, Lei 13.709/2018 |
| **RNF016** | transversal | — | mvp | design-sistema.md §11.2 |
| **RNF017** | profissionais, contratacoes | portfolio_foto, contratacao_anexo | mvp | ADR-0006 |
| **RNF018** | profissionais, contratacoes | endereco | mvp | Decorrência da decisão de manter RF013 no MVP; fornecedor de geocodificação ainda pendente de ADR |
| **RNF019** | autenticacao | refresh_token | mvp | ADR-0006 |
| **RNF020** | autenticacao | usuario, token_recuperacao | mvp | ADR-0006 |
| **RNF021** | autenticacao | token_recuperacao | mvp | ADR-0006 |
| **RNF022** | transversal, usuarios | log_acao | mvp | Revisão de banco 2026-08-22 |

---

## 2. Módulo → Requisitos

Leitura inversa. Antes de mexer num módulo, esta é a lista do que ele precisa continuar cumprindo.

| Módulo | Qtd. | Requisitos |
|---|---|---|
| `autenticacao` | 7 | RF001, RF002, RNF001, RNF002, RNF019, RNF020, RNF021 |
| `avaliacoes` | 3 | RF006, RF007, RF019 |
| `contratacoes` | 12 | RF008, RF012, RF013, RF014, RF015, RF016, RF017, RF018, RF023, RF024, RNF017, RNF018 |
| `denuncias` | 1 | RF020 |
| `profissionais` | 10 | RF003, RF004, RF005, RF009, RF013, RF019, RNF005, RNF014, RNF017, RNF018 |
| `servicos` | 10 | RF004, RF009, RF010, RF011, RF020, RF025, RF026, RF027, RF030, RNF005 |
| `transversal` | 12 | RNF003, RNF004, RNF006, RNF007, RNF008, RNF009, RNF010, RNF011, RNF012, RNF013, RNF016, RNF022 |
| `usuarios` | 8 | RF002, RF020, RF021, RF022, RNF002, RNF014, RNF015, RNF022 |

---

## 3. Leitura da cobertura

**Os módulos do ADR-0004 têm requisito.** Nenhum módulo foi inventado sem necessidade, e
nenhum requisito ficou órfão.

**`servicos` saltou de 6 para 10 requisitos** em duas rodadas do mesmo dia: `RF025`–`RF027`
(v1.2.0) registraram a primeira leva da feature "Gerenciamento de serviços" do administrador,
detalhada em
[`historias-usuario-administrador-servicos.md`](./historias-usuario-administrador-servicos.md);
`RF030` (v1.3.0) veio de uma revisão de código que achou um `deletar()` sem requisito, sem
contrato e sem implementação real — ver `requisitos.md` §4.6.

**`denuncias` é o módulo mais novo da lista** — separado de `avaliacoes` na sessão de 2026-09-14
porque o RF020 (denunciar serviço, perfil ou avaliação) não tem nada em comum com avaliar uma
contratação além de ambos partirem de uma revisão de código enviada por um integrante fora da
convenção vigente na época.

**`autenticacao` saltou de 4 para 7 requisitos** com o [ADR-0006](./adr/0006-remover-supabase-infraestrutura-propria.md).
Esse salto é a medida do que o Supabase estava fazendo pelo projeto de graça: ciclo de vida de
token, proteção contra força bruta e enumeração, e token de recuperação de senha. Nada disso
apareceu porque o produto cresceu — apareceu porque a responsabilidade mudou de dono.

**`contratacoes` concentra 12 requisitos** — é o módulo mais pesado do sistema, e com folga. Faz
sentido: é onde o produto realmente acontece (solicitar, aceitar, acompanhar, cancelar, avaliar
o resultado). Duas consequências práticas:

- É o primeiro candidato natural a virar **módulo composto** (`controller/`, `service/`,
  `contrato/` materializados), conforme a regra de "pasta só quando há mais de um arquivo"
- É o módulo onde a fronteira mais será testada. `contratacoes` referencia cliente e profissional,
  mas **pelo `id`** (`Long` ou `UUID`, conforme [ADR-0007](./adr/0007-chave-primaria-mista.md)),
  nunca por `@ManyToOne` cruzando módulo

**`avaliacoes` tem 3 requisitos** — é o módulo mais simples, e deve permanecer como módulo simples
(arquivos na raiz, sem subpastas) até que um quarto requisito prove o contrário.

**`transversal` tem 12 RNFs e nenhum RF.** É o esperado: requisito funcional sempre pertence a
algum domínio. RNF transversal é garantido em `config/`, `comum/` e no CI, não dentro de um módulo.

---

## 4. Rastro para código

Vazio por enquanto — **não existe código de aplicação no repositório**. Desde 2026-08-27 existe
a *fundação* (`backend/` compila, `frontend/` builda, o compose sobe PostgreSQL e MinIO), mas
fundação não implementa requisito: não há migration, entidade, endpoint nem tela.

Esta seção passa a ser preenchida quando as migrations e os módulos forem escritos, ligando cada
requisito ao arquivo que o implementa e ao teste que o prova. Esse código é **escrito pela
equipe**, não pela LLM (`CLAUDE.md`, regra nº 3).

| Código | Migration | Implementação | Teste |
|---|---|---|---|
| — | — | — | — |

---

## Referências

- [`requisitos.md`](./requisitos.md) — os requisitos em formato legível, com o histórico da revisão
- [`requisitos.json`](./requisitos.json) — fonte da verdade
- [`design-sistema.md`](./design-sistema.md) §11.2 — estrutura dos módulos
- [`adr/0004-estrutura-modular-por-dominio.md`](./adr/0004-estrutura-modular-por-dominio.md)
