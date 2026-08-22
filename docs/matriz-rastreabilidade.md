# Matriz de Rastreabilidade — Bico em Casa

| Campo | Valor |
|---|---|
| **Gerado a partir de** | [`requisitos.json`](./requisitos.json) |
| **Última sincronização** | 2026-08-22 |
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

O campo **Entidades** é **projeção**, não fato: as migrations do Flyway ainda não existem. Ele
vira fato quando o schema for escrito, e é revisado nesse momento.

---

## 1. Requisito → Módulo → Entidade

| Código | Módulos | Entidades previstas | Status | Origem |
|---|---|---|---|---|
| **RF001** | autenticacao | tb_usuarios | mvp | Tabela de requisitos do grupo |
| **RF002** | autenticacao, usuarios | tb_usuarios, tb_usuario_papeis | mvp | Tabela de requisitos do grupo (revisado: papéis múltiplos) |
| **RF003** | profissionais | tb_profissionais, tb_portfolios, tb_portfolio_fotos | mvp | Tabela de requisitos do grupo |
| **RF004** | servicos, profissionais | tb_servicos, tb_categorias, tb_profissionais | mvp | Tabela de requisitos do grupo; PBB (Pesquisa serviço) |
| **RF005** | profissionais | tb_profissionais, tb_disponibilidades | mvp | Tabela de requisitos do grupo |
| **RF006** | avaliacoes | tb_avaliacoes, tb_contratacoes | mvp | Tabela de requisitos do grupo |
| **RF007** | avaliacoes | tb_avaliacoes | mvp | Tabela de requisitos do grupo |
| **RF008** | contratacoes | tb_contratacoes, tb_contratacao_status_historico | mvp | Tabela de requisitos do grupo; PBB (Ver status do serviço realizado) |
| **RF009** | profissionais, servicos | tb_profissionais, tb_avaliacoes | mvp | Tabela de requisitos do grupo; PBB (Ordenar por notas de avaliações) |
| **RF010** | servicos | tb_servicos, tb_categorias | mvp | Tabela de requisitos do grupo; PBB (Cadastrar serviço prestado) |
| **RF011** | servicos | tb_servicos, tb_categorias | mvp | Tabela de requisitos do grupo; PBB (Visualizar preços pré-definidos) |
| **RF012** | contratacoes | tb_contratacoes, tb_notificacoes | mvp | Tabela de requisitos do grupo |
| **RF013** | profissionais, contratacoes | tb_profissionais, tb_enderecos, tb_contratacoes | mvp | Tabela de requisitos do grupo |
| **RF014** | contratacoes | tb_contratacoes | mvp | Tabela de requisitos do grupo; PBB (Enviar solicitação de serviço) |
| **RF015** | contratacoes | tb_contratacoes, tb_contratacao_anexos | mvp | Tabela de requisitos do grupo (revisado: anexo na solicitação, não em chat) |
| **RF016** | contratacoes | tb_contratacoes | mvp | Tabela de requisitos do grupo (era o segundo RF016 duplicado) |
| **RF017** | contratacoes | tb_contratacoes | mvp | Lacuna encontrada na revisão: RF012 notificava um aceite que nenhum requisito criava |
| **RF018** | contratacoes | tb_contratacoes | mvp | PBB (Cancelar serviço) |
| **RF019** | avaliacoes, profissionais | tb_avaliacoes, tb_profissionais | mvp | PBB (Visualizar nota média do profissional) |
| **RF020** | servicos, usuarios | tb_denuncias | mvp | PBB (Denunciar serviços inadequados) |
| **RF021** | usuarios | tb_usuarios | mvp | PBB (Visualizar usuários ativos e inativos no site) |
| **RF022** | usuarios | tb_usuarios, tb_moderacao_acoes | mvp | PBB (Gerenciamento de usuários) |
| **RF023** | contratacoes | tb_conversas, tb_mensagens | pos-mvp | PDF de especificação, quadro é–não é–faz–não faz; PBB (Falar com o profissional) |
| **RF024** | contratacoes | tb_mensagens | pos-mvp | Tabela de requisitos do grupo (era o primeiro RF016 duplicado) |
| **RNF001** | autenticacao, transversal | tb_usuarios | mvp | ADR-0002 |
| **RNF002** | autenticacao, usuarios | tb_usuarios, tb_usuario_papeis | mvp | CLAUDE.md, seção Nunca faça |
| **RNF003** | transversal | — | mvp | Revisão de requisitos 2026-08-22 |
| **RNF004** | transversal | — | mvp | ADR-0002; CLAUDE.md |
| **RNF005** | servicos, profissionais | tb_servicos, tb_profissionais | mvp | Revisão de requisitos 2026-08-22 |
| **RNF006** | transversal | — | mvp | design-sistema.md §10.2 |
| **RNF007** | transversal | — | mvp | Revisão de requisitos 2026-08-22 |
| **RNF008** | transversal | — | mvp | Revisão de requisitos 2026-08-22 |
| **RNF009** | transversal | — | mvp | design-sistema.md §9.1 |
| **RNF010** | transversal | — | mvp | design-sistema.md §5 |
| **RNF011** | transversal | — | mvp | design-sistema.md §3.3 |
| **RNF012** | transversal | — | mvp | design-sistema.md §7.2 |
| **RNF013** | transversal | — | mvp | Revisão de requisitos 2026-08-22 |
| **RNF014** | usuarios, profissionais | tb_usuarios, tb_enderecos | mvp | LGPD, Lei 13.709/2018 |
| **RNF015** | usuarios | tb_usuarios, tb_contratacoes, tb_avaliacoes | mvp | LGPD, Lei 13.709/2018 |
| **RNF016** | transversal | — | mvp | design-sistema.md §10.2 |
| **RNF017** | profissionais, contratacoes | tb_portfolio_fotos, tb_contratacao_anexos | mvp | design-sistema.md §6.1 |
| **RNF018** | profissionais, contratacoes | tb_enderecos | mvp | Decorrência da decisão de manter RF013 no MVP; pendente de ADR-0006 |

---

## 2. Módulo → Requisitos

Leitura inversa. Antes de mexer num módulo, esta é a lista do que ele precisa continuar cumprindo.

| Módulo | Qtd. | Requisitos |
|---|---|---|
| `autenticacao` | 4 | RF001, RF002, RNF001, RNF002 |
| `avaliacoes` | 3 | RF006, RF007, RF019 |
| `contratacoes` | 12 | RF008, RF012, RF013, RF014, RF015, RF016, RF017, RF018, RF023, RF024, RNF017, RNF018 |
| `profissionais` | 10 | RF003, RF004, RF005, RF009, RF013, RF019, RNF005, RNF014, RNF017, RNF018 |
| `servicos` | 6 | RF004, RF009, RF010, RF011, RF020, RNF005 |
| `transversal` | 12 | RNF001, RNF003, RNF004, RNF006, RNF007, RNF008, RNF009, RNF010, RNF011, RNF012, RNF013, RNF016 |
| `usuarios` | 7 | RF002, RF020, RF021, RF022, RNF002, RNF014, RNF015 |

---

## 3. Leitura da cobertura

**Os seis módulos do ADR-0004 têm requisito.** Nenhum módulo foi inventado sem necessidade, e
nenhum requisito ficou órfão. Contagem confere: 42 requisitos (24 RF + 18 RNF), 0 sem módulo.

**`contratacoes` concentra 12 requisitos** — é o módulo mais pesado do sistema, e com folga. Faz
sentido: é onde o produto realmente acontece (solicitar, aceitar, acompanhar, cancelar, avaliar
o resultado). Duas consequências práticas:

- É o primeiro candidato natural a virar **módulo composto** (`controller/`, `service/`,
  `contrato/` materializados), conforme a regra de "pasta só quando há mais de um arquivo"
- É o módulo onde a fronteira mais será testada. `contratacoes` referencia cliente e profissional,
  mas **por UUID**, nunca por `@ManyToOne` cruzando módulo

**`avaliacoes` tem 3 requisitos** — é o módulo mais simples, e deve permanecer como módulo simples
(arquivos na raiz, sem subpastas) até que um quarto requisito prove o contrário.

**`transversal` tem 12 RNFs e nenhum RF.** É o esperado: requisito funcional sempre pertence a
algum domínio. RNF transversal é garantido em `config/`, `comum/` e no CI, não dentro de um módulo.

---

## 4. Rastro para código

Vazio por enquanto — **não existe código de aplicação no repositório**. Esta seção passa a ser
preenchida quando as migrations e os módulos forem escritos, ligando cada requisito ao arquivo
que o implementa e ao teste que o prova.

| Código | Migration | Implementação | Teste |
|---|---|---|---|
| — | — | — | — |

---

## Referências

- [`requisitos.md`](./requisitos.md) — os requisitos em formato legível, com o histórico da revisão
- [`requisitos.json`](./requisitos.json) — fonte da verdade
- [`design-sistema.md`](./design-sistema.md) §10.2 — estrutura dos módulos
- [`adr/0004-estrutura-modular-por-dominio.md`](./adr/0004-estrutura-modular-por-dominio.md)
