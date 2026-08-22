#!/usr/bin/env bash
# Hook PostToolUse (Edit|Write).
#
# Regra do projeto: docs/arquitetura-sistema.json e a fonte da verdade da
# arquitetura e docs/design-sistema.md e o espelho legivel dele. Alterar um sem
# alterar o outro deixa a documentacao mentindo.
#
# Este hook nao bloqueia nada: apenas lembra, na hora exata da edicao, que a
# contrapartida em markdown precisa ir no mesmo commit.

set -uo pipefail

arquivo="$(jq -r '.tool_input.file_path // .tool_response.filePath // empty' 2>/dev/null || true)"

case "$arquivo" in
  */docs/arquitetura-sistema.json | docs/arquitetura-sistema.json) ;;
  *) exit 0 ;;
esac

jq -n '{
  systemMessage: "arquitetura-sistema.json alterado -> atualize docs/design-sistema.md no MESMO commit.",
  suppressOutput: true,
  hookSpecificOutput: {
    hookEventName: "PostToolUse",
    additionalContext: "REGRA DE PARIDADE DA DOCUMENTACAO: voce acabou de alterar docs/arquitetura-sistema.json, que e a fonte da verdade da arquitetura. Antes de encerrar a tarefa voce DEVE refletir a mesma alteracao em docs/design-sistema.md (mesma secao, mesmos valores) e as duas mudancas devem ir no mesmo commit. Se a alteracao for estrutural, registre tambem um ADR em docs/adr/ usando docs/adr/TEMPLATE.md."
  }
}'
