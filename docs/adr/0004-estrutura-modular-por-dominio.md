# ADR-0004 — Estrutura modular por domínio no backend

| Campo | Valor |
|---|---|
| **ADR** | `0004` |
| **Título** | Estrutura modular por domínio no backend |
| **Autor** | Carlos Antunes |
| **Data** | 2026-08-22 |
| **Tópico** | Backend |
| **Status** | Aceito |
| **LLM utilizada** | `Claude Opus 5 (Claude Code)` |

---

## Contexto

A versão 1.0.0 do `arquitetura-sistema.json` propunha o recorte clássico **por camada
técnica**: `domain/{model,repository,service}`, `api/v1/{controller,dto,mapper}` e
`infrastructure/{client,security}`.

Esse recorte tem um problema conhecido que aparece cedo: **todos os controllers vivem na mesma
pasta, todos os services na mesma pasta**. Com seis domínios, `service/` acumula seis arquivos
sem relação entre si, e entender o módulo de contratações exige abrir cinco pastas diferentes.
A estrutura da pasta deixa de comunicar o que o sistema faz e passa a comunicar apenas com
qual framework ele foi escrito.

O usuário rejeitou explicitamente esse recorte e pediu organização por domínio.

Além disso, o recorte por camada não impede acoplamento acidental: com todos os repositories
visíveis no mesmo pacote, nada sinaliza que injetar `ProfissionalRepository` dentro de
`ContratacaoServiceImpl` é uma violação de fronteira.

## Decisão

Organizar `src/main/java/br/com/bicoemcasa/api/` em quatro áreas:

| Pasta | Responsabilidade |
|---|---|
| `config/` | Configuração **da aplicação**: SecurityFilterChain, CORS, OpenAPI, Jackson, beans, `@ConfigurationProperties` |
| `lib/` | Adapters de serviços **externos** (`supabase/`). Sem regra de negócio — só tradução entre o mundo externo e tipos internos |
| `comum/` | Núcleo compartilhado: `excecao/`, `paginacao/`, `auditoria/`. Não depende de módulo nenhum |
| `modulos/` | Um pacote por domínio de negócio, autocontido |

**Módulos da v1:** `autenticacao`, `usuarios`, `profissionais`, `servicos`, `contratacoes`,
`avaliacoes`.

**Estrutura interna do módulo:** `controller/`, `service/`, `repository/`, `models/`, `dto/`,
`contrato/` e, opcionalmente, `mapper/`.

### Regra: pasta só quando há mais de um arquivo

A pasta só existe quando há **mais de um arquivo** daquele tipo. Com um único arquivo, ele
fica na raiz do módulo. Ao surgir o segundo arquivo de um tipo, cria-se a pasta e movem-se
**ambos** no mesmo commit.

Isso evita a árvore de seis pastas com um arquivo cada, que é ruído puro num módulo simples.

### Convenção de `contrato/`

A interface é `ProfissionalService`, em `contrato/`. A implementação é `ProfissionalServiceImpl`,
em `service/`. Sem prefixo `I` — não é idiomático em Java, e o nome limpo pertence ao contrato.

### Regras de acoplamento

- Módulo A chama módulo B **apenas** pela interface publicada em `B/contrato/`
- **Proibido** injetar o repository de outro módulo
- **Proibido** relacionamento JPA cruzando módulos — referencie pelo `id` (UUID)

A terceira regra sustenta as outras duas: sem `@ManyToOne` cruzando a fronteira, o acoplamento
acidental não tem por onde entrar.

## Alternativas Consideradas

| Opção | Prós | Contras | Por que foi recusada |
|---|---|---|---|
| Recorte por camada (`domain/`, `api/v1/`, `infrastructure/`) | Familiar; padrão de tutorial | Controllers e services de domínios distintos misturados; nada sinaliza violação de fronteira | Rejeitado pelo usuário e não escala com o número de domínios |
| Hexagonal estrito por módulo (`domain/`, `application/`, `adapters/in`, `adapters/out`) | Fronteiras rigorosas; domínio puro | Muita cerimônia: cada CRUD simples exigiria porta, adapter e mapeamento | Custo desproporcional para o escopo e o prazo do projeto |
| Microsserviços por domínio | Escala independente | Complexidade operacional (deploy, rede, transação distribuída) incompatível com equipe pequena | Nenhum requisito de escala justifica |
| Módulos com estrutura **fixa** (sempre as seis pastas) | Previsível; sem julgamento | Módulo simples fica com seis pastas de um arquivo cada — ruído | Contraria a regra de "pasta só com mais de um arquivo" pedida pelo usuário |

## Consequências

### Positivas

- A árvore de pastas comunica o **domínio**, não o framework: quem abre `modulos/` entende o
  que o sistema faz
- Tudo de um domínio fica junto — alterar contratações não exige navegar cinco pastas
- As regras de acoplamento tornam a violação de fronteira visível em code review
- Extrair um módulo para serviço próprio depois fica viável, porque a fronteira já existe

### Negativas

- A regra "pasta só com mais de um arquivo" exige **julgamento contínuo** e é a mais provável
  de ser aplicada de forma inconsistente. O momento de promoção (segundo arquivo) precisa ser
  cobrado em review, ou a estrutura degrada
- Proibir relacionamento JPA entre módulos custa: perde-se `JOIN FETCH` cruzando fronteira, e
  algumas consultas exigem duas idas ao banco ou uma view
- Desenvolvedores acostumados ao recorte por camada precisam de adaptação

### Neutras

- `mapper/` é opcional e só aparece quando o mapeamento deixa de ser trivial
- O `comum/` pode virar depósito de tudo se não for vigiado — só entra o que é usado por
  mais de um módulo

## Impacto na Arquitetura

Esta decisão altera `docs/arquitetura-sistema.json`?

- [x] Sim — campos alterados: `directory_structure.backend` (reescrito por completo),
  `project_metadata.architecture_pattern`, `code_standards_and_style.backend.rules`
- [ ] Não

## Referências

- Simon Brown, *Modular Monoliths*
- [`design-sistema.md` §11.2](../design-sistema.md) — estrutura detalhada com exemplos
