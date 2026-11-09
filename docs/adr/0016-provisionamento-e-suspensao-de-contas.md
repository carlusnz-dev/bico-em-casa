# ADR-0016 — Provisionamento e suspensão de contas

| Campo | Valor |
|---|---|
| **Data** | 2026-11-09 |
| **Status** | Aceito |
| **Tópico** | Back-end · Banco |
| **LLM utilizada** | Codex |

O cadastro público serve a clientes e profissionais. O primeiro administrador é provisionado por
um operador com acesso ao banco. Um administrador ativo cria os seguintes pelo painel.
O papel continua sendo lido do banco.

A suspensão incrementa a versão de sessão na conta e revoga os refresh tokens. Consultar a versão
ao aceitar o JWT invalida tokens anteriores mesmo após a reativação. O log de auditoria registra
autor e motivo. Os registros anteriores são inicializados como ativos, pois antes todos
podiam autenticar. O README orienta a revisão dos administradores já existentes antes da implantação.
