# ADR-0017 — Revisão de ofertas e galeria de portfólio

| Campo | Valor |
|---|---|
| **Data** | 2026-11-09 |
| **Status** | Aceito |
| **Tópico** | Back-end · Banco |
| **LLM utilizada** | Codex |

A HU15 mantém a publicação inicial imediata. A fila deriva das ações registradas em `log_acao`.
A reprovação grava o motivo no serviço, desativa a oferta e usa a notificação existente. Um reenvio
retorna à fila mantendo a oferta inativa até decisão de um administrador.

A galeria usa uma linha de `portfolio_midia` para cada objeto confirmado no MinIO. A foto de capa
continua no portfólio. O preço médio é calculado em consulta, agrupado por unidade para manter
preços comparáveis. A estimativa do profissional usa as próprias ofertas ativas e a média pública
usa as ofertas ativas da categoria.
