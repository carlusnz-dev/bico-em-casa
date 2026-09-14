# Relatório de Sessão — 2026-09-11

| Campo | Valor |
|---|---|
| **Sessão** | Design das telas de login, home e not-found no Figma, a partir dos tokens da página "Assets" |
| **Autor** | Carlos Antunes |
| **Data** | `2026-09-11` |
| **Duração aproximada** | ~1h30 |
| **LLM utilizada** | `Claude Sonnet 5 (Claude Code)` |
| **Branch** | `feat/ux-autenticacao-home-notfound` |
| **Commits** | Nenhum — sessão trabalhou inteiramente no arquivo Figma, nenhum arquivo do repositório foi alterado |
| **Plano relacionado** | Nenhum |

---

## Resumo

Sessão de design no Figma (arquivo `aBkPjiGi2KLE8Cc21BRO9d`), sem escrever código de aplicação —
por decisão explícita do usuário, aplicando a Regra nº 3 do projeto. Antes de desenhar qualquer
tela, três pontos foram confirmados com o usuário, como pedido: (1) o escopo da home, já que o
MCP do Trello continuou falhando (`CONNECTION_CLOSED`) e o usuário trouxe as PBIs por screenshot
em vez de via ferramenta; (2) o conjunto de componentes de base a construir antes das telas
inteiras; (3) o mapeamento semântico da paleta de 10 cores, que estava pendente desde a sessão
anterior.

Com os três pontos decididos, a sessão seguiu as skills `figma-use`, `figma-generate-library` e
`figma-generate-design` para: inspecionar o arquivo (nenhuma biblioteca de componentes existia,
apenas a página "Assets" com logo, 10 swatches de cor e as fontes Josefin Sans/Albert Sans);
criar variáveis Figma (primitivos de cor, cor semântica, espaçamento e raio) espelhando os hex já
existentes em `frontend/src/app/globals.css`; criar estilos de texto e um estilo de efeito
(sombra de card); construir os 7 componentes de base confirmados (`Botao`, `Input`, `Label`,
`Card`, `Modal` em uma página, `Cabecalho` e `Rodape` em outra); e montar as 3 telas (Login, Home,
Not Found) como instâncias desses componentes, validando cada uma com screenshot.

Nenhum arquivo do repositório foi alterado — todo o trabalho vive no Figma. O mapeamento
semântico de cor (teal = primary, laranja = accent, etc.) foi decidido nesta sessão mas **ainda
não foi levado para `globals.css`**: o código continua com tokens nomeados só por matiz, como
documentado na sessão de 09-11 anterior. Levar esse mapeamento para o código é trabalho da equipe.

## O que foi feito

- Confirmado com o usuário, antes de desenhar: escopo da home (vitrine de profissionais/serviços
  em destaque, sem busca em destaque nesta rodada), conjunto de componentes de base ("amplo":
  Botão, Input, Card, Modal, Label + Cabeçalho, Rodapé) e mapeamento semântico da paleta
  (teal → primary, laranja → accent, mint → sucesso, orange-red → erro, blue → info, navy → texto
  principal, white-warm/cream → fundo, gray → borda/disabled)
- Inspecionado o arquivo Figma (`get_metadata`, `get_libraries`): confirmado que não há biblioteca
  de componentes associada e que os 10 swatches da página "Assets" batem exatamente com os hex de
  `globals.css`
- Criadas 3 coleções de variáveis no Figma: `Cor/Primitivos` (10 cores, alias-only, `scopes: []`),
  `Cor/Semântico` (12 variáveis aliasadas às primitivas, com `codeSyntax` WEB apontando para a
  variável CSS real hoje em `globals.css`) e `Fundação/Espaçamento e Raio` (escala padrão do
  Tailwind v4 — não existe token custom no código ainda, marcado como tal na descrição de cada
  variável)
- Criados 6 estilos de texto (`Titulo/H1-H3` em Albert Sans, `Corpo/Base`, `Corpo/Pequeno`,
  `Rotulo/Padrao` em Josefin Sans) e 1 estilo de efeito (`Sombra/Card`)
- Criadas as páginas `Fundações`, `Componentes UI`, `Componentes Layout`, `Login`, `Home`,
  `Not Found`, além da `Assets` já existente
- Página `Fundações` populada com swatches das cores semânticas e amostra de cada estilo de texto
- 7 componentes construídos com variantes e propriedades de texto ligadas às variáveis/estilos
  acima, todos com `description` documentando o uso pretendido no código:
  - `Botao` (`variant`: primary/accent/secondary/ghost × `state`: default/hover/disabled — 12
    variantes)
  - `Input` (`state`: default/focus/erro/disabled — 4 variantes)
  - `Label` (`obrigatorio`: true/false — 2 variantes)
  - `Card` (`tipo`: profissional/servico — 2 variantes, usado na vitrine da home)
  - `Modal` (componente único — nenhuma das 3 telas usa modal hoje; criado por decisão explícita
    do usuário ao escolher o conjunto "amplo")
  - `Cabecalho` (logo clonado da página Assets, links de navegação, instância de `Botao`)
  - `Rodape` (copyright + links institucionais)
- 3 telas montadas como composição de instâncias dos componentes acima, cada uma validada por
  screenshot:
  - `Login`: cabeçalho, card de formulário (Label + Input para e-mail e senha, botão "Entrar",
    link para cadastro), rodapé
  - `Home`: cabeçalho, seção "Profissionais em destaque" (3 cards), seção "Serviços populares"
    (3 cards), rodapé
  - `Not Found`: cabeçalho, código "404", mensagem, botão "Voltar para a home", rodapé

## Decisões tomadas

| Decisão | Motivo | ADR |
|---|---|---|
| Home prioriza vitrine de profissionais/serviços em destaque no sprint 1, não busca em destaque | Decisão do usuário diante das PBIs trazidas por screenshot ("Selecionar um serviço", "Criar portfólio" pesam mais que "Realizar pesquisa" isoladamente); apresentada com 4 opções e trade-offs | Não requer |
| Conjunto "amplo" de componentes de base (Botão, Input, Card, Modal, Label, Cabeçalho, Rodapé) construído antes das telas, em vez do mínimo | Decisão do usuário — antecipa componentes de módulos futuros além dos 3 solicitados | Não requer |
| Mapeamento semântico da paleta: teal = primary, laranja = accent, mint = sucesso, orange-red = erro, blue = info, navy = texto, white-warm/cream = fundo, gray = borda/disabled | Decisão do usuário entre 3 opções apresentadas com trade-off; pendência registrada na sessão de 09-11 anterior | Não requer — decisão vive no Figma; só quando a equipe levar isso para `globals.css` como tokens semânticos é que uma ADR pode fazer sentido, se alterar a convenção de nomenclatura de `--color-*` |
| Escala de espaçamento/raio no Figma segue o padrão do Tailwind v4 (4/8/16/24/32/48px; raios 4/8/12/9999) | Não existe token custom de espaçamento/raio no código ainda — usar o padrão da ferramenta em vez de inventar uma escala é a opção que menos compromete a equipe | Não requer — sinalizado como convenção de ferramenta de design, não decisão de arquitetura, na descrição de cada variável |
| Um único componente de Cabeçalho compartilhado entre as 3 telas, com botão "Entrar" sempre visível (inclusive na própria tela de Login) | Prioriza reuso do componente sobre uma variante contextual; não foi discutido com o usuário — fica como pendência | Não requer |

## Arquivos alterados

Nenhum arquivo do repositório foi criado ou alterado nesta sessão, além deste relatório. Todo o
trabalho está no arquivo Figma `aBkPjiGi2KLE8Cc21BRO9d` (páginas `Fundações`, `Componentes UI`,
`Componentes Layout`, `Login`, `Home`, `Not Found`).

## Verificações executadas

| Comando | Resultado |
|---|---|
| `get_metadata` / `get_screenshot` (Figma) após cada componente e tela | ✅ passou — validação visual de cada componente e das 3 telas completas conferida por screenshot |

Nenhum comando de build/teste do repositório foi executado nesta sessão — não houve alteração de
código.

## Problemas encontrados

- **MCP do Trello continuou falhando** (`CONNECTION_CLOSED`), pela segunda sessão seguida. O
  usuário contornou colando screenshots das PBIs em vez de reconectar a ferramenta. A causa raiz
  da falha de conexão não foi investigada — seguirá indisponível até alguém investigar do lado da
  configuração do MCP/Trello.
- **`combineAsVariants` rejeitou frames diretamente** (`Cannot move node. A COMPONENT_SET node
  cannot have children of type other than COMPONENT`) — os frames de variante precisam virar
  `COMPONENT` via `figma.createComponentFromNode()` antes de `combineAsVariants`. Corrigido no
  componente `Botao` e replicado nos demais.
- **`layoutSizingHorizontal = "FILL"` lançou erro duas vezes** (`Fundações` e `Modal`) por ser
  setado antes do `appendChild` ao pai de auto-layout, em vez de depois — regra 12 da skill
  `figma-use`. Ambas as vezes o Figma reverteu a mutação parcial da chamada com erro (confirmado
  lendo a página antes de tentar de novo), então não sobrou nó órfão; a correção foi mover a
  atribuição para depois do `appendChild`.
- **Logo clonado da página Assets ficou em branco** ao usar `resize(64, 64)` em vez de
  `rescale(64/382)` — `resize` só muda a caixa delimitadora sem escalar o conteúdo vetorial
  interno, cortando o desenho para fora da área visível. Corrigido trocando para `rescale`.

## Pendências

- [ ] Levar o mapeamento semântico de cor decidido nesta sessão para `globals.css` como tokens
      `--color-primary`, `--color-accent` etc. (ou manter só no Figma) — decisão da equipe
- [ ] O `Cabecalho` mostra "Entrar" mesmo na própria tela de Login — não discutido com o usuário,
      decidir se a tela de login usa uma variante do cabeçalho sem CTA de entrar
- [ ] Seções de vitrine da home (grid de cards) não têm quebra de linha (wrap) — hoje só cabem
      3 cards por linha antes de estourar a largura do frame; decidir se isso é aceitável para o
      sprint 1 ou se precisa de layout responsivo antes da implementação
- [ ] Conteúdo real das telas (código React/Next) — a LLM não escreveu nenhuma tela nesta sessão,
      por decisão explícita do usuário, aplicando a Regra nº 3
- [ ] Reconectar o MCP do Trello — falhou por duas sessões seguidas
- [ ] Pendências já registradas na sessão de 09-11 anterior continuam abertas: `cliente.ts` vs.
      `client.ts`, `loginPage` em camelCase, `app/page.tsx` fora de `(publico)/`, ADR de `utils/`,
      falha do smoke test `BicoEmCasaApplicationTests`, débitos de segurança do relatório 09-10

## Próximos passos

1. Equipe revisa as 3 telas e os 7 componentes no Figma e decide se aprova o mapeamento semântico
   de cor e o layout da home antes de qualquer código
2. Equipe escreve `components/ui/{Botao,Input,Card,Modal,Label}.tsx` e
   `components/layout/{Cabecalho,Rodape}.tsx` a partir dos componentes Figma
3. Equipe escreve as telas de login, home e not-found em `app/(auth)/login`, `app/(publico)/` —
   usando o WIP de login já retomado na sessão anterior como ponto de partida
4. Decidir e resolver a pendência do `Cabecalho` na tela de login antes de implementar
5. Reconectar o Trello antes da próxima sessão que dependa de escopo de PBI
