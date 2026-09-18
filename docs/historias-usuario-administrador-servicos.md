# Histórias de Usuário — Administrador · Gerenciamento de serviços

| Campo | Valor |
|---|---|
| **Feature (PBB)** | Gerenciamento de serviços |
| **Persona** | Administrador |
| **PBIs cobertos** | 5 cartões do canvas `PBB_bico-em-casa.png` |
| **Autor** | Carlos Antunes |
| **Data** | `2026-08-23` |
| **Status** | HU 11–13 registradas como `RF025`–`RF027` em `requisitos.json` v1.2.0 (2026-09-18). HU 14–15 continuam rascunho — dependem de ADR de schema |
| **Fonte** | `BES-Especificação do Projeto - Bico em Casa.pdf`, Artefato 5 · canvas PBB |

---

> [!WARNING]
> **HU 14 e HU 15 ainda não têm requisito funcional.** HU 11–13 foram registradas como `RF025`,
> `RF026` e `RF027` em `requisitos.json` v1.2.0. `RF028` e `RF029` (HU 14 e HU 15) continuam
> **propostos**, não registrados, porque exigem migration e a Regra nº 1 do `CLAUDE.md` pede ADR
> antes de mudar schema. Enquanto o ADR não sair, essas duas permanecem rascunho de discussão,
> não backlog aprovado.

> [!NOTE]
> **Numeração.** O PDF entrega 10 histórias depois da figura de exemplo (7 de Cliente, 3 de
> Profissional com o campo `POSSO` em branco) mais uma de Administrador incompleta. As cinco
> abaixo continuam essa sequência a partir de **11**. Se a numeração do PDF for corrigida antes
> da entrega, renumere aqui junto.

---

## HISTÓRIA DO USUÁRIO 11 — PBI: Visualizar listagem de todos os serviços cadastrados

| | |
|---|---|
| **COMO** | Administrador |
| **POSSO** | visualizar a listagem paginada de todos os serviços cadastrados na plataforma |
| **PARA** | acompanhar o que está sendo oferecido no site e identificar o que precisa de moderação |

| | |
|---|---|
| **Critério de Aceite 1** | **DADO QUE**: o administrador autenticado acessa o painel de serviços.<br>**QUANDO**: a página é carregada.<br>**ENTÃO**: o sistema exibe uma tabela paginada com no máximo **50 serviços por página**, cada linha mostrando título, profissional responsável, categorias vinculadas, preço prévio, situação (ativo / inativo) e data de cadastro, ordenada do cadastro mais recente para o mais antigo. |
| **Critério de Aceite 2** | **DADO QUE**: o administrador está na listagem de serviços.<br>**QUANDO**: seleciona o filtro de situação (**Todos**, **Somente ativos** ou **Somente inativos**) e/ou uma categoria, e aciona o botão "Aplicar filtro".<br>**ENTÃO**: a listagem é atualizada para mostrar apenas os serviços que atendem aos filtros selecionados, o contador total é recalculado e a paginação volta para a primeira página. |
| **Critério de Aceite 3** | **DADO QUE**: o administrador está na listagem de serviços.<br>**QUANDO**: digita um termo no campo de busca e aciona "Buscar".<br>**ENTÃO**: a listagem mostra apenas os serviços cujo **título** contenha o termo, ou cujo **profissional responsável** tenha o termo no nome ou no e-mail, sem diferenciar maiúsculas de minúsculas nem acentuação. |
| **Critério de Aceite 4** | **DADO QUE**: um usuário autenticado **sem** o papel de administrador tenta acessar a listagem de serviços do painel.<br>**QUANDO**: a requisição chega ao backend.<br>**ENTÃO**: o acesso é negado com HTTP 403 no formato `ProblemDetail`, sem expor detalhe interno, e o papel é verificado na base de dados — **nunca** por claim do JWT. |

**Rastreabilidade:** `RF025` (registrado) · apoia-se em `RNF002`, `RNF006`, `RNF009`.
**Dados:** leitura de `servico` (com `perfil_id`, `titulo`, `preco_previo`, `ativo`, `criado_em`) e de `servico_tag` × `tag`. **Não exige migration.**

---

## HISTÓRIA DO USUÁRIO 12 — PBI: Desativar ou excluir serviços inadequados

| | |
|---|---|
| **COMO** | Administrador |
| **POSSO** | desativar um serviço inadequado registrando o motivo, e reativá-lo se a análise mudar |
| **PARA** | tirar do ar ofertas que violam as regras da plataforma sem destruir o histórico das contratações já realizadas |

| | |
|---|---|
| **Critério de Aceite 1** | **DADO QUE**: o administrador visualiza o detalhe de um serviço **ativo**.<br>**QUANDO**: aciona "Desativar", informa um motivo com no mínimo 10 caracteres e confirma.<br>**ENTÃO**: o serviço passa a inativo, deixa imediatamente de aparecer na busca pública e nos resultados do cliente, e a ação é gravada no log de auditoria com autor, tipo de ação, alvo, motivo, data e endereço de origem. |
| **Critério de Aceite 2** | **DADO QUE**: o administrador aciona "Desativar" em um serviço.<br>**QUANDO**: confirma **sem** preencher o motivo, ou com um motivo menor que 10 caracteres.<br>**ENTÃO**: o serviço **não** é desativado, e o sistema devolve erro de validação em `ProblemDetail` apontando o campo `motivo`. |
| **Critério de Aceite 3** | **DADO QUE**: um serviço está inativo por ação do administrador.<br>**QUANDO**: o administrador aciona "Reativar" e confirma.<br>**ENTÃO**: o serviço volta a ficar ativo, reaparece na busca pública, e a reativação também é gravada no log de auditoria — a desativação é **reversível e rastreável**, não um apagamento. |
| **Critério de Aceite 4** | **DADO QUE**: o serviço que será desativado possui contratações em andamento.<br>**QUANDO**: a desativação é efetivada.<br>**ENTÃO**: as contratações existentes permanecem intactas, com status, histórico de transições e valores preservados, e apenas **novas** solicitações de orçamento para aquele serviço passam a ser recusadas. |
| **Critério de Aceite 5** | **DADO QUE**: o profissional dono do serviço acessa o próprio painel.<br>**QUANDO**: um serviço dele foi desativado pelo administrador.<br>**ENTÃO**: ele vê o serviço marcado como desativado pela moderação, com o motivo informado, e **não** consegue reativá-lo por conta própria. |

**Rastreabilidade:** `RF026` (registrado) · complementa `RF020` (denúncia) · apoia-se em `RNF022`.
**Dados:** `UPDATE servico SET ativo = false` + escrita em `log_acao`. **Não exige migration.**

> [!IMPORTANT]
> **O cartão diz "desativar ou excluir"; esta história só desativa.** Decisão tomada em 2026-08-23:
> o `DELETE` físico é tecnicamente possível — `contratacao.servico_id` é `ON DELETE SET NULL` —
> mas a contratação perderia a referência do que foi vendido, e a ação não volta atrás.
> Desativação some da busca do mesmo jeito, é reversível e é auditável.

---

## HISTÓRIA DO USUÁRIO 13 — PBI: Editar categorias e informações de serviços

| | |
|---|---|
| **COMO** | Administrador |
| **POSSO** | manter as categorias da plataforma e corrigir as informações de um serviço cadastrado |
| **PARA** | manter o catálogo organizado e buscável, sem depender do profissional para corrigir um erro |

| | |
|---|---|
| **Critério de Aceite 1** | **DADO QUE**: o administrador acessa a tela de categorias.<br>**QUANDO**: a tela carrega.<br>**ENTÃO**: o sistema exibe a lista de categorias predefinidas da plataforma — semeadas por migration, não cadastradas pelo administrador —, cada uma já disponível para vínculo com serviço e como opção do filtro de busca do cliente. Não existe ação de criar categoria nova nesta tela. |
| **Critério de Aceite 2** | **DADO QUE**: o administrador renomeia uma categoria existente.<br>**QUANDO**: informa um nome que já existe em outra categoria — ignorando maiúsculas, minúsculas e espaços nas pontas.<br>**ENTÃO**: a operação é recusada com erro de conflito em `ProblemDetail`, e o nome anterior é mantido. |
| **Critério de Aceite 3** | **DADO QUE**: uma categoria já está vinculada a serviços cadastrados.<br>**QUANDO**: o administrador altera o nome dessa categoria e confirma.<br>**ENTÃO**: todos os serviços vinculados passam a exibir o nome novo, **nenhum vínculo é perdido**, e o filtro de busca por essa categoria continua devolvendo exatamente os mesmos serviços de antes. |
| **Critério de Aceite 4** | **DADO QUE**: o administrador abre o detalhe de um serviço cadastrado por um profissional.<br>**QUANDO**: corrige título, descrição ou as categorias vinculadas, informa o motivo da correção e confirma.<br>**ENTÃO**: as alterações são salvas, o profissional dono é notificado de que o serviço foi editado pela moderação, e o log de auditoria guarda o valor anterior e o novo de cada campo alterado. |
| **Critério de Aceite 5** | **DADO QUE**: uma categoria está vinculada a pelo menos um serviço.<br>**QUANDO**: o administrador tenta removê-la.<br>**ENTÃO**: a remoção é recusada, e o sistema informa quantos serviços ainda usam aquela categoria — a categoria só pode ser removida quando não houver nenhum vínculo. |

**Rastreabilidade:** `RF027` (registrado) · dá ao administrador o outro lado de `RF004` e `RF010`.
**Dados:** `UPDATE`/`DELETE` em `tag` (sem `INSERT` pela API), ajuste de `servico_tag`,
`UPDATE servico` (título/descrição) + `log_acao`. **Não exige migration de schema** — exige uma
migration de **dado** (seed) com a lista de categorias predefinidas, ver
[`planos/2026-09-18-especificacao-tecnica-hu13-categorias.md`](./planos/2026-09-18-especificacao-tecnica-hu13-categorias.md).

> [!NOTE]
> **"Categoria" é a tabela `tag` do modelo v4.0.0.** O modelo não tem entidade chamada `categoria`;
> `tag` já é o que `RF004` filtra e o que `RF010` grava. Nomear categoria como entidade nova
> criaria duas coisas para o mesmo conceito.

> [!IMPORTANT]
> **Categoria é predefinida, o administrador não cria uma nova.** Decisão tomada em 2026-09-18,
> registrada em
> [`planos/2026-09-18-especificacao-tecnica-hu13-categorias.md`](./planos/2026-09-18-especificacao-tecnica-hu13-categorias.md).
> O cartão do PBB e a redação original do CA1 previam o administrador cadastrando categoria pela
> tela; a decisão trocou isso por uma lista semeada no backend via migration. O administrador
> continua podendo **renomear** (CA2/CA3) e **remover** (CA5) uma categoria existente — só a
> criação deixou de ser ação do admin.

---

## HISTÓRIA DO USUÁRIO 14 — PBI: Definir faixas de preços sugeridos por serviço

| | |
|---|---|
| **COMO** | Administrador |
| **POSSO** | definir uma faixa de preço sugerida — mínimo e máximo — para cada categoria de serviço |
| **PARA** | dar referência de valor ao profissional que cadastra e ao cliente que compara, tornando a contratação mais rápida |

| | |
|---|---|
| **Critério de Aceite 1** | **DADO QUE**: o administrador acessa uma categoria de serviço.<br>**QUANDO**: informa o valor mínimo e o valor máximo sugeridos, ambos não negativos e com mínimo menor ou igual ao máximo, e confirma.<br>**ENTÃO**: a faixa é gravada na categoria e passa a ser exibida em todos os pontos onde aquela categoria aparece. |
| **Critério de Aceite 2** | **DADO QUE**: o administrador está editando a faixa de uma categoria.<br>**QUANDO**: informa um valor negativo, ou um mínimo maior que o máximo, e confirma.<br>**ENTÃO**: a faixa **não** é salva, o sistema devolve erro de validação em `ProblemDetail` apontando o campo inválido, e a faixa anterior permanece como estava. |
| **Critério de Aceite 3** | **DADO QUE**: uma categoria tem faixa sugerida definida.<br>**QUANDO**: o profissional cadastra ou edita um serviço nessa categoria e informa um preço prévio fora da faixa.<br>**ENTÃO**: o formulário exibe a faixa como referência e apresenta um **aviso não bloqueante** — o profissional pode confirmar o preço mesmo assim, porque a faixa é sugestão da plataforma, não tabelamento. |
| **Critério de Aceite 4** | **DADO QUE**: uma categoria tem faixa sugerida definida.<br>**QUANDO**: o cliente visualiza os serviços dessa categoria na busca.<br>**ENTÃO**: a faixa sugerida é exibida ao lado do **preço médio efetivamente praticado** na plataforma, rotulada de forma que fique claro qual é sugestão da plataforma e qual é o valor real dos profissionais. |
| **Critério de Aceite 5** | **DADO QUE**: uma categoria **não** tem faixa sugerida definida.<br>**QUANDO**: o profissional cadastra um serviço ou o cliente visualiza a categoria.<br>**ENTÃO**: nenhuma faixa é exibida, nenhum aviso de preço é gerado, e o filtro de busca por faixa de preço continua funcionando normalmente sobre o preço informado pelos profissionais. |

**Rastreabilidade:** `RF028` (proposto) · conversa com `RF004` (filtro por faixa de preço) e `RF011` (preço médio por categoria).
**Dados:** **exige migration** — duas colunas monetárias opcionais em `tag` com restrição de `mínimo ≤ máximo`.

> [!IMPORTANT]
> **A faixa é por categoria, não por serviço individual.** Decisão tomada em 2026-08-23. O cartão do
> PBB diz "por serviço", mas `RF004` e `RF011` já tratam faixa e média **por categoria**, e uma faixa
> por oferta individual colidiria com `servico.preco_previo`, que é o preço do profissional — além
> de obrigar o administrador a tocar linha a linha conforme o catálogo cresce.

---

## HISTÓRIA DO USUÁRIO 15 — PBI: Aprovar ou reprovar novos serviços cadastrados

| | |
|---|---|
| **COMO** | Administrador |
| **POSSO** | revisar a fila de serviços recém-cadastrados e aprová-los ou reprová-los |
| **PARA** | garantir que os serviços oferecidos na plataforma são compatíveis com a proposta do site |

| | |
|---|---|
| **Critério de Aceite 1** | **DADO QUE**: um profissional conclui o cadastro de um serviço novo.<br>**QUANDO**: o cadastro é salvo.<br>**ENTÃO**: o serviço já entra **ativo e visível** na busca do cliente, e ao mesmo tempo aparece na fila "Aguardando revisão" do administrador, ordenada do cadastro **mais antigo para o mais recente** — quem esperou mais é revisado primeiro. |
| **Critério de Aceite 2** | **DADO QUE**: o administrador abre um serviço na fila "Aguardando revisão".<br>**QUANDO**: aciona "Aprovar".<br>**ENTÃO**: o serviço sai da fila, permanece ativo e visível, **nada muda para o cliente**, e a aprovação é gravada no log de auditoria com autor e data. |
| **Critério de Aceite 3** | **DADO QUE**: o administrador abre um serviço na fila "Aguardando revisão".<br>**QUANDO**: aciona "Reprovar" e informa o motivo da reprovação.<br>**ENTÃO**: o serviço passa a inativo, sai da busca do cliente, o profissional é notificado com o motivo, e a reprovação é gravada no log de auditoria. |
| **Critério de Aceite 4** | **DADO QUE**: um serviço foi reprovado pelo administrador.<br>**QUANDO**: o profissional corrige as informações e reenvia o serviço.<br>**ENTÃO**: o serviço volta para a fila "Aguardando revisão" e **permanece inativo** até que o administrador o aprove — a correção após reprovação não republica sozinha. |
| **Critério de Aceite 5** | **DADO QUE**: existem serviços na fila ainda não revisados.<br>**QUANDO**: o cliente realiza uma busca de serviços.<br>**ENTÃO**: esses serviços aparecem normalmente nos resultados — a revisão é **posterior** à publicação e não segura o profissional esperando aprovação para começar a vender. |

**Rastreabilidade:** `RF029` (proposto) · estende `RF010` (cadastro de serviço pelo profissional).
**Dados:** **exige migration** — coluna de motivo de reprovação em `servico`; a fila é derivada de `criado_em` e do log de revisão, sem enum de status novo.

> [!IMPORTANT]
> **Moderação posterior, não prévia.** Decisão tomada em 2026-08-23. O serviço publica na hora e o
> administrador revisa depois. Moderação prévia mudaria `RF004` e `RF010` — o catálogo passaria a
> filtrar por status de aprovação — e deixaria o profissional parado esperando alguém aprovar.
> O Critério de Aceite 4 fecha a brecha óbvia: quem foi reprovado não se republica editando.

---

## Resumo — do cartão ao requisito

| # | Cartão do PBB | História | RF | Migration? |
|---:|---|---|---|---|
| 11 | Visualizar listagem de todos os serviços cadastrados | Listagem paginada com filtro e busca | `RF025` (registrado) | Não |
| 12 | Desativar ou excluir serviços inadequados | Desativação reversível com motivo | `RF026` (registrado) | Não |
| 13 | Editar categorias e informações de serviços | Renomear/remover categoria predefinida + correção de serviço | `RF027` (registrado) | Dado (seed), não schema |
| 14 | Definir faixas de preços sugeridos por serviço | Faixa mínimo/máximo por categoria | `RF028` (proposto) | **Sim** — 2 colunas em `tag` |
| 15 | Aprovar ou reprovar novos serviços cadastrados | Fila de revisão posterior | `RF029` (proposto) | **Sim** — 1 coluna em `servico` |

## Decisões embutidas nestas histórias

| Decisão | Onde aparece | Alternativa descartada |
|---|---|---|
| Só desativar, sem exclusão física | HU 12 | `DELETE` no serviço, com `contratacao.servico_id` virando nulo |
| Faixa de preço por categoria (`tag`) | HU 14 | Faixa por serviço individual, colidindo com `preco_previo` |
| Moderação **posterior** à publicação | HU 15 | Moderação prévia, com serviço nascendo pendente |
| "Categoria" é a `tag` já existente | HU 13 | Criar entidade `categoria` separada |
| Categoria é **predefinida por seed**, admin não cria pela API | HU 13, CA 1 | Admin cadastra categoria nova pela tela (redação original do CA1) |
| Aviso de preço fora da faixa é **não bloqueante** | HU 14, CA 3 | Bloquear o cadastro fora da faixa — **ainda não confirmado** |

## Pendências

- [x] Registrar `RF025`–`RF027` em `requisitos.json` → refletir em `requisitos.md` → rodar
      `/revisar-matriz`, **no mesmo commit** (Regra nº 1 do `CLAUDE.md`) — feito em v1.2.0
      (2026-09-18)
- [ ] Registrar `RF028`–`RF029` em `requisitos.json` assim que o ADR abaixo sair
- [ ] **ADR** para as duas mudanças de schema das HU 14 e HU 15 — faixa de preço em `tag` e
      motivo de reprovação em `servico`. Schema não muda sem ADR
- [ ] Atualizar `docs/modelo-dados.dbml` e `docs/modelo-dados.md` depois que o ADR sair
- [ ] Confirmar se o aviso de preço fora da faixa (HU 14, CA 3) bloqueia ou apenas avisa
- [ ] Preencher os 5 cartões vazios da coluna "Gerenciamento de serviços" no canvas PBB —
      hoje o `PBB_bico-em-casa.png` tem a feature mas não tem os PBIs
- [ ] Reescrever os critérios de aceite das 10 histórias existentes no PDF, que hoje repetem
      literalmente o exemplo de filtro por data (pendência já registrada em `requisitos.md` §5)

## Referências

- `BES-Especificação do Projeto - Bico em Casa.pdf` — Artefato 5, formato da história de usuário
- [`PBB_bico-em-casa.png`](./PBB_bico-em-casa.png) — canvas PBB, feature "Gerenciamento de serviços"
- [`requisitos.md`](./requisitos.md) — requisitos vigentes, `RF001`–`RF024`
- [`modelo-dados.md`](./modelo-dados.md) — tabelas `servico`, `tag`, `servico_tag`, `log_acao`
- [`adr/0004-estrutura-modular-por-dominio.md`](./adr/0004-estrutura-modular-por-dominio.md) — módulo `servicos`
