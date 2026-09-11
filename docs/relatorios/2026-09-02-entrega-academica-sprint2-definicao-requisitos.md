# Relatório de Sessão — 2026-09-02

| Campo | Valor |
|---|---|
| **Sessão** | Documento .docx de entrega acadêmica — Sprint 2, definição dos requisitos |
| **Autor** | Carlos Antunes |
| **Data** | `2026-09-02` |
| **Duração aproximada** | ~45min (estimativa, sem cronometragem exata) |
| **LLM utilizada** | `Claude Sonnet 5 (Claude Code)` |
| **Branch** | `feat/modulo-autenticacao` |
| **Commits** | Nenhum |
| **Plano relacionado** | Nenhum (plano feito via plan mode do Claude Code, não persistido em `docs/planos/`) |

---

## Resumo

Sessão dedicada a uma entrega acadêmica da disciplina "Criação de Modelos de Soluções
Computacionais" (Sprint 2), não a código do projeto. O pedido foi consolidar os 46 requisitos já
documentados em `docs/requisitos.json`/`docs/requisitos.md` em um `.docx` no formato exigido pela
atividade, para o usuário revisar e entregar ao grupo — explicitamente **sem persistir nada no
repositório**.

Como não havia, no repositório, nenhum enunciado ou rubrica da disciplina definindo a estrutura da
tabela de entrega, três agentes de exploração foram usados em paralelo para levantar contexto (o
único relatório de 01/09 encontrado, que trata de FK/migration do módulo `usuarios` e não tem
relação com esta entrega; o conteúdo completo dos 46 requisitos; e a ausência de um template de
atividade no repo). A estrutura de tabela e demais decisões de conteúdo (nome da disciplina, local
de salvamento fora do repo, critério de "requisitante") foram então confirmadas diretamente com o
usuário via perguntas objetivas, em vez de assumidas.

O documento foi gerado reaproveitando o pipeline `pandoc` + `docs/estilo/reference-doc.docx` já
documentado em `docs/estilo/GUIA-DE-ESTILO.md`, mantendo a identidade visual PUCPR usada nos
demais documentos acadêmicos do grupo. Depois da primeira revisão do usuário, o documento foi
ajustado duas vezes: remoção dos requisitos pós-MVP (RF023/RF024) e reescrita da coluna
"Descrição" para seguir a convenção "O sistema deve..." do `requisitos.json`, em vez do formato
enxuto demais da primeira versão. O usuário aprovou o resultado e encaminhou para revisão de um
colega de equipe (Lucas M.).

## O que foi feito

- Levantamento dos 46 requisitos do projeto (24 funcionais + 22 não-funcionais) a partir de
  `docs/requisitos.json` e `docs/requisitos.md`, confirmando que nenhum deles tinha relação com o
  relatório de sessão de 01/09 (esse tratou de FK/migration do módulo `usuarios`)
- Confirmação com o usuário, via perguntas diretas, de quatro pontos que não estavam no
  repositório: estrutura de tabela exigida pela atividade, nome da disciplina a usar na capa,
  caminho local de destino do arquivo e critério para a coluna "Requisitante"
- Geração do `.docx` "Sprint 2 - definição dos requisitos do projeto" com capa (título +
  participantes) e duas tabelas (Requisitos Funcionais / Requisitos Não-Funcionais), reaproveitando
  o template `docs/estilo/reference-doc.docx` já existente no projeto
- Correção do rodapé do documento gerado, que herdava texto literal de outro documento
  ("Mapa de Contexto do Projeto") embutido no template compartilhado
- Ajuste do documento a pedido do usuário após a primeira revisão: remoção de RF023/RF024
  (pós-MVP) e reescrita da coluna "Descrição" de todos os 22 RF e 22 RNF restantes para seguir a
  convenção "O sistema deve..." do `requisitos.json`, mantendo a coluna "Detalhes" como estava
- Documento salvo em `~/Documentos/PUC/3-P/criacao_solucoes_computacionais/sprint2-definicao-requisitos.docx`,
  fora do repositório, sem nenhum commit ou alteração de arquivo versionado

## Decisões tomadas

| Decisão | Motivo | ADR |
|---|---|---|
| Usar "Criação de Modelos de Soluções Computacionais" como nome da disciplina na capa | Usuário confirmou que o repo cita duas disciplinas diferentes por motivos distintos (Experiência Criativa = entrega de código/tela; esta = diagramas/modelagem) — não é uma correção do repositório, só o nome certo para esta entrega específica | Não requer |
| Estrutura de tabela: `ID \| Descrição \| Requisitante \| Detalhes`, em duas tabelas separadas (Funcionais / Não-Funcionais) | Formato passado pelo usuário, exigido pela atividade da disciplina | Não requer |
| Coluna "Requisitante" preenchida por proposta da LLM (papel do sistema: Cliente/Profissional/Administrador para RF; Patrocinador/Equipe técnica para RNF) | `requisitos.json` não tem campo de stakeholder; usuário pediu explicitamente uma proposta inicial para o grupo revisar e aprovar | Não requer |
| RF023 e RF024 (chat e áudio, pós-MVP) removidos da entrega | Usuário decidiu focar a entrega apenas nos requisitos do MVP | Não requer |
| Documento salvo fora do repositório git | Pedido explícito do usuário ("não persista no repositório") | Não requer |

## Arquivos alterados

Nenhum arquivo do repositório foi criado, alterado ou removido nesta sessão. As modificações
visíveis em `git status` no início e no fim da sessão (`docs/adr/README.md`,
`docs/arquitetura-sistema.json`, `docs/design-sistema.md`, `docs/adr/0010-versionamento-migration-por-timestamp.md`)
já existiam antes desta sessão começar — vieram de outro trabalho em andamento na mesma branch,
não tocado aqui.

| Arquivo (fora do repositório) | Alteração |
|---|---|
| `~/Documentos/PUC/3-P/criacao_solucoes_computacionais/sprint2-definicao-requisitos.docx` | Criado — documento de entrega acadêmica, gerado e revisado nesta sessão |

## Verificações executadas

| Comando | Resultado |
|---|---|
| `pandoc conteudo.md --reference-doc=docs/estilo/reference-doc.docx --resource-path=. -o sprint2-definicao-requisitos.docx` | ✅ passou (executado duas vezes: rascunho inicial e versão final após os ajustes pedidos) |
| `libreoffice --headless --convert-to pdf sprint2-definicao-requisitos.docx` | ✅ passou — 11 páginas na primeira versão, 10 páginas na versão final (após remover RF023/RF024) |
| `pdftoppm` da capa e de páginas de tabela + inspeção visual das imagens | ✅ confirmado: capa com título e participantes corretos, tabelas com as 4 colunas certas, rodapé com o nome do documento (não mais o texto herdado do template) |
| `git status --porcelain` no repositório `bico-em-casa` | ✅ confirmado sem nenhuma alteração desta sessão, idêntico ao estado do início da conversa |

## Problemas encontrados

- **Nenhuma estrutura de atividade documentada no repositório.** A busca por um enunciado ou
  rubrica da disciplina em `docs/` não encontrou nada — só um template de estilo visual
  (`docs/estilo/reference-doc.docx`) e templates internos do projeto (ADR, plano, relatório), sem
  relação com a rubrica da entrega. Resolvido perguntando a estrutura de tabela diretamente ao
  usuário, em vez de supor um formato.
- **Rodapé do `.docx` gerado herdava texto de outro documento.** `docs/estilo/reference-doc.docx`
  guarda o rodapé como XML estático da última vez que o template foi usado para gerar um
  documento, não como placeholder dinâmico — por isso o primeiro rascunho saiu com "Mapa de
  Contexto do Projeto · gerado a partir de docs/" no rodapé. Corrigido com um pós-processamento via
  `python-docx` (mesma técnica descrita em `docs/estilo/GUIA-DE-ESTILO.md`), substituindo o texto
  do rodapé pelo título deste documento.
- **Primeira versão da coluna "Descrição" fugiu da convenção de escrita de requisitos do
  projeto.** Saiu enxuta demais (frases nominais como "Cadastro, login {...}"), sem indicar quem
  faz a ação. O usuário apontou o problema e a coluna foi reescrita usando o texto original de
  `docs/requisitos.json` (que já segue o padrão "O sistema deve..."), mantendo a coluna "Detalhes"
  como estava.

## Pendências

- [ ] Aprovação final do documento pelo restante do grupo — no momento em que a sessão foi
      encerrada, o Lucas M. estava revisando
- [ ] Validar linha a linha a coluna "Requisitante" (stakeholder) — foi uma proposta inicial da
      LLM por papel do sistema, ainda sem confirmação do grupo
- [ ] Não existe, no repositório, nenhum registro do enunciado/rubrica oficial da disciplina
      "Criação de Modelos de Soluções Computacionais" — se a atividade tiver outros critérios de
      estrutura além da tabela usada aqui, vale documentá-los em algum lugar (fora do escopo desta
      sessão, por ser entrega acadêmica e não arquitetura do sistema)

## Próximos passos

1. Aguardar o retorno da revisão do Lucas M. e dos demais integrantes antes de considerar o
   documento pronto para entrega
2. Se houver pedido de ajuste de conteúdo, regenerar o `.docx` a partir do pipeline
   `pandoc` + `docs/estilo/reference-doc.docx` (o markdown de origem ficou apenas no scratchpad da
   sessão, não foi persistido em lugar nenhum)
3. Nenhuma ação pendente no código do projeto Bico em Casa decorre desta sessão
