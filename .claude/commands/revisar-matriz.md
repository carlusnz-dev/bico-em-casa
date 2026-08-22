---
description: Valida requisitos.json, confere a paridade com requisitos.md e regenera a matriz de rastreabilidade
argument-hint: "[--check para apenas relatar, sem escrever]"
allowed-tools: Read, Write, Edit, Glob, Bash(jq:*), Bash(grep:*), Bash(diff:*), Bash(cat:*), Bash(wc:*), Bash(sort:*), Bash(comm:*), Bash(date:*), Bash(mktemp:*), Bash(git status:*), Bash(git diff:*)
---

# Revisar a Matriz de Rastreabilidade

Mantém `docs/requisitos.json`, `docs/requisitos.md` e `docs/matriz-rastreabilidade.md` em sincronia.

**A ordem de autoridade é fixa:**

```
requisitos.json  ──►  requisitos.md  ──►  matriz-rastreabilidade.md
 (fonte)              (espelho)            (derivado)
```

O JSON manda. O markdown espelha. A matriz é gerada. Nunca o contrário.

---

## Diagnóstico automático

**Data de hoje:** !`date +%Y-%m-%d`

**JSON é válido?** !`jq empty docs/requisitos.json && echo "OK" || echo "FALHA - JSON invalido"`

**Contagem:** !`jq -r '"total=\([to_entries[]|select(.key!="_meta")]|length) RF=\([to_entries[]|select(.key|startswith("RF"))]|length) RNF=\([to_entries[]|select(.key|startswith("RNF"))]|length)"' docs/requisitos.json`

**Chave diverge do campo `codigo`:** !`jq -r 'to_entries[] | select(.key!="_meta") | select(.key != .value.codigo) | "  \(.key) tem codigo=\(.value.codigo)"' docs/requisitos.json | grep . || echo "  nenhuma"`

**Campo obrigatório faltando:** !`jq -r '["codigo","tipo","descricao","modulos","entidades","status","origem","data_criada"] as $req | to_entries[] | select(.key!="_meta") | . as $e | $req[] as $f | select($e.value|has($f)|not) | "  \($e.key) sem campo: \($f)"' docs/requisitos.json | grep . || echo "  nenhum"`

**Módulo fora de `_meta.modulos_validos`:** !`jq -r '(._meta.modulos_validos) as $v | to_entries[] | select(.key!="_meta") | .key as $k | .value.modulos[] as $m | select($m|IN($v[])|not) | "  \($k) -> \($m)"' docs/requisitos.json | grep . || echo "  nenhum"`

**Status fora de `_meta.status_validos`:** !`jq -r '(._meta.status_validos) as $v | to_entries[] | select(.key!="_meta") | . as $e | select($e.value.status|IN($v[])|not) | "  \($e.key) -> \($e.value.status)"' docs/requisitos.json | grep . || echo "  nenhum"`

**Requisito sem módulo:** !`jq -r 'to_entries[] | select(.key!="_meta") | select((.value.modulos|length)==0) | "  \(.key)"' docs/requisitos.json | grep . || echo "  nenhum"`

**Data fora do formato AAAA-MM-DD:** !`jq -r 'to_entries[] | select(.key!="_meta") | select(.value.data_criada | test("^[0-9]{4}-[0-9]{2}-[0-9]{2}$") | not) | "  \(.key) -> \(.value.data_criada)"' docs/requisitos.json | grep . || echo "  nenhuma"`

**Buracos na numeração dos RF:** !`jq -r '[to_entries[]|select(.key|startswith("RF"))|.key|ltrimstr("RF")|tonumber]|sort as $n | ($n|max) as $m | [range(1;$m+1)] - $n | if length==0 then "  nenhum" else "  faltam: \(map("RF" + (if . < 10 then "00" elif . < 100 then "0" else "" end) + (.|tostring))|join(", "))" end' docs/requisitos.json`

**Códigos no JSON mas ausentes de `requisitos.md`:** !`comm -23 <(jq -r 'keys[]|select(.!="_meta")' docs/requisitos.json | sort) <(grep -oE '\*\*R[FN]+[0-9]{3}\*\*' docs/requisitos.md | tr -d '*' | sort -u) | sed 's/^/  /' | grep . || echo "  nenhum"`

**Códigos em `requisitos.md` mas ausentes do JSON:** !`comm -13 <(jq -r 'keys[]|select(.!="_meta")' docs/requisitos.json | sort) <(grep -oE '\*\*R[FN]+[0-9]{3}\*\*' docs/requisitos.md | tr -d '*' | sort -u) | sed 's/^/  /' | grep . || echo "  nenhum"`

**Códigos ausentes da matriz:** !`comm -23 <(jq -r 'keys[]|select(.!="_meta")' docs/requisitos.json | sort) <(grep -oE '\*\*R[FN]+[0-9]{3}\*\*' docs/matriz-rastreabilidade.md | tr -d '*' | sort -u) | sed 's/^/  /' | grep . || echo "  nenhum"`

**Working tree:** !`git status --short docs/ || echo "sem alteracoes"`

---

## O que fazer

### Passo 1 — Parar se o JSON estiver quebrado

Se o diagnóstico acusou **JSON inválido**, **chave divergente do `codigo`**, **campo obrigatório
faltando**, **módulo inválido** ou **status inválido**: **não gere nada.** Relate os problemas e
pare. Gerar matriz a partir de fonte quebrada propaga o defeito para três arquivos.

Buraco na numeração e data fora de formato são **avisos**, não bloqueios — relate e siga.

### Passo 2 — Conferir a paridade das descrições

Os diagnósticos acima comparam apenas os **códigos**. Uma descrição pode ter sido editada só no
markdown e nunca no JSON — o caso mais comum de divergência silenciosa.

Para cada requisito, compare a descrição do JSON com a linha correspondente em `requisitos.md`.
Onde divergirem:

- **O JSON vence.** Corrija o markdown.
- **Exceto** se ficar claro que a edição do markdown foi a intencional e o JSON é que ficou para
  trás. Nesse caso **pergunte antes de escolher um lado** — nunca decida sozinho qual das duas
  versões é a verdadeira.

### Passo 3 — Regenerar a matriz

Reescreva `docs/matriz-rastreabilidade.md` a partir do JSON, preservando a estrutura de seções
que já existe no arquivo. As tabelas são **geradas**, nunca digitadas:

**Seção 1 — Requisito → Módulo → Entidade:**

```bash
jq -r 'to_entries[] | select(.key != "_meta") | .value |
  "| **\(.codigo)** | \(.modulos|join(", ")) | \(if (.entidades|length)==0 then "—" else (.entidades|join(", ")) end) | \(.status) | \(.origem) |"' docs/requisitos.json
```

**Seção 2 — Módulo → Requisitos:**

```bash
jq -r '
  [to_entries[] | select(.key != "_meta") | .value | {codigo, modulos: .modulos[]}]
  | group_by(.modulos)
  | map({modulo: .[0].modulos, reqs: [.[].codigo]})
  | .[]
  | "| `\(.modulo)` | \(.reqs|length) | \(.reqs|join(", ")) |"
' docs/requisitos.json
```

Atualize também:

- O campo **Última sincronização** do cabeçalho, para a data de hoje
- A **seção 3, "Leitura da cobertura"** — ela é análise em prosa, não tabela. Se a distribuição
  mudou de forma relevante (um módulo passou a concentrar requisitos, um módulo ficou vazio, a
  contagem total mudou), reescreva a análise. Se nada mudou de forma relevante, deixe como está
  e **apenas confira que os números citados no texto continuam corretos**
- A **seção 4, "Rastro para código"** — preencha as linhas de requisitos que já tenham migration,
  implementação ou teste no repositório. Confira com `Glob`/`Grep` antes de afirmar que existe;
  requisito sem código continua com `—`

### Passo 4 — Relatar

Responda com:

1. **O que estava divergente** — lista concreta, ou "nada divergente"
2. **O que foi alterado** — arquivo por arquivo
3. **Avisos que não bloquearam** — buracos de numeração, datas, entidades ainda projetadas
4. **O que precisa de decisão humana** — divergência de descrição que você não resolveu sozinho

---

## Modo `--check`

Se `$ARGUMENTS` contiver `--check`: execute os passos 1 e 2, **relate tudo e não escreva nenhum
arquivo.** Útil antes de abrir PR e para rodar em CI.

---

## Regras de honestidade

- **Não invente rastro.** A coluna de implementação só recebe caminho de arquivo que existe.
  Verifique antes de escrever. `—` é uma resposta correta e honesta.
- **Não "conserte" um requisito reescrevendo a descrição.** Divergência entre JSON e markdown é
  sinal de que alguém editou o lado errado — a correção é sincronizar, não redigir melhor.
- **Não renumere.** Código de requisito é estável para sempre, mesmo que o requisito saia de
  escopo. Renumerar quebra toda referência externa: PDF de entrega, issues, commits, conversa do
  grupo.
- **Não remova requisito.** Requisito descartado vira `pos-mvp`, ou ganha nota, e continua no
  arquivo. Some do MVP, não do histórico.
- **Não crie requisito por conta própria.** Se durante a revisão você perceber uma lacuna — um
  requisito que pressupõe outro que não existe — **relate a lacuna**, não a preencha.
