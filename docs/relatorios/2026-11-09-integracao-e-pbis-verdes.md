# Relatório de sessão — Integração e PBIs verdes

Sessão de 2026-11-09.
Base inicial `main@32d7864` e `develop@a5c82f5`

## Entrega

1. [PR #23](https://github.com/carlusnz-dev/bico-em-casa/pull/23) bloqueia o tipo ADMIN nas duas rotas públicas e adiciona testes HTTP.
2. [PR #24](https://github.com/carlusnz-dev/bico-em-casa/pull/24) integra a main em develop e preserva o chat, o ciclo da contratação e a moderação.
3. [PR #25](https://github.com/carlusnz-dev/bico-em-casa/pull/25) entrega os contratos e regras de back-end.
4. [PR #26](https://github.com/carlusnz-dev/bico-em-casa/pull/26) entrega as telas e a correção de renovação simultânea de sessão.

As branches foram trabalhadas em quatro worktrees isoladas. O plano está em
[`docs/planos/2026-11-09-integracao-e-pbis-verdes.md`](../planos/2026-11-09-integracao-e-pbis-verdes.md).

## Decisões

- O primeiro ADMIN é provisionado com o script SQL do back-end. Administradores ativos criam os demais pelo painel.
- Uma suspensão invalida o JWT e o refresh existentes. Reativar libera novo login.
- Ofertas novas publicam e entram na fila de revisão posterior. O reenvio após reprovação aguarda aprovação com a oferta inativa.
- O orçamento é uma estimativa da média das ofertas ativas do próprio profissional, multiplicada pela quantidade informada na mesma unidade. O resultado é calculado na tela.
- A galeria preserva a foto de capa e usa o MinIO para fotos e vídeos. A avaliação exige o cliente titular e uma contratação concluída.

## Verificações

- GitHub Actions executa a suíte Maven com PostgreSQL via Testcontainers, typecheck e Vitest.
- PostgreSQL 18.6 aplicou as 15 migrations em banco vazio. Em bancos separados com o histórico de cada branch, a atualização com `SPRING_FLYWAY_OUT_OF_ORDER=true` chegou às mesmas 15 e o reinício com a configuração padrão passou.
- A API local respondeu 403 às duas tentativas de cadastro público ADMIN. O endpoint de saúde retornou `UP`.
- Chrome com Playwright verificou cadastro, login, criação de segundo ADMIN, publicação de ofertas, cálculo do orçamento, reprovação, reenvio, aprovação, suspensão, recusa de login, reativação e recusa do JWT antigo.
- O navegador encontrou uma corrida de renovação em modo de desenvolvimento. As requisições concorrentes agora compartilham a renovação em andamento e há teste de regressão para essa regra.

A revisão humana do checklist do projeto segue disponível nos PRs.
