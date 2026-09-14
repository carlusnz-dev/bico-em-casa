# ADR-0011 — Rotação de refresh token e endpoint de renovação

| Campo | Valor |
|---|---|
| **ADR** | `0011` |
| **Título** | Refresh token opaco, rotacionado a cada uso, renovado por endpoint próprio |
| **Autor** | Carlos Antunes |
| **Data** | `2026-09-13` |
| **Tópico** | Backend |
| **Status** | Aceito |
| **LLM utilizada** | `Claude Opus 5 (Claude Code)` |

---

## Contexto

Desde 2026-09-05 o backend emite, no login, um access token JWT e um refresh token opaco de 32
bytes de `SecureRandom`, persistido em `refresh_token` com hash SHA-256. A tabela já nasceu com
`familia_id` e `substituido_por`, colunas que só fazem sentido se houver rotação — mas **nenhuma
das duas era escrita**, porque não existia endpoint de renovação. O ciclo estava pela metade.

Três consequências práticas disso:

1. Um refresh token vazado valia 30 dias inteiros para o atacante, e a vítima não percebia nada.
2. A revisão de 2026-09-10 registrou a ausência do endpoint como **bloqueante nº 3**, sem correção.
3. O frontend já tinha o consumidor escrito: `hooks/useSessao` chamava `renovar()` no mount para
   reidratar a sessão a partir do cookie `httpOnly`. Como o endpoint não existia, a chamada sempre
   falhava e **a sessão nunca sobrevivia a um reload**, mesmo após login bem-sucedido.

A estratégia de token opaco + SHA-256 vinha sendo usada há mais de uma semana sem nunca ter sido
registrada, o que também é uma violação da Regra nº 1 do projeto.

## Decisão

**Adotamos rotação obrigatória do refresh token, exposta por `POST /api/autenticacao/renovar`.**

O endpoint lê o refresh token do cookie `refreshToken`, e o serviço:

1. calcula o hash SHA-256 do token recebido e busca por ele;
2. recusa com `TokenInvalidoException` se for desconhecido, já revogado ou expirado;
3. cria um token sucessor **na mesma `familia_id` e para o mesmo `usuario_id`**;
4. grava `substituido_por` no antigo e o revoga (`revogado_em`);
5. emite um access token novo e devolve o refresh novo em cookie com os mesmos atributos do login.

Os passos 3 a 5 vivem sob `@Transactional` dentro de `RefreshTokenService.substituir(...)`: um
token substituído sem ser revogado, ou revogado sem apontar para o sucessor, deixaria a família
num estado impossível de auditar.

**O refresh token continua opaco, não JWT**, e continua hasheado com **SHA-256, não Argon2id** —
ele é um segredo de 256 bits de alta entropia, não uma senha; não há dicionário para atacar, e o
custo do Argon2 seria pago a cada renovação sem ganho de segurança.

## Alternativas Consideradas

| Opção | Prós | Contras | Por que foi recusada |
|---|---|---|---|
| Renovar sem rotacionar (mesmo refresh token vale até expirar) | Menos código; sem risco de corrida entre abas | Token vazado vale 30 dias; `familia_id` e `substituido_por` continuariam mortos | Mantém exatamente o débito que o endpoint existe para fechar |
| Refresh token como JWT assinado | Sem consulta ao banco para validar | Revogação exige lista de bloqueio, ou seja, o banco volta; o token já é persistido de qualquer forma | Assinatura seria redundante com a persistência |
| Argon2id no hash do refresh token | Uniformidade com a senha | Custo alto por renovação, sem ganho para segredo de 256 bits gerado por CSPRNG | Custo sem benefício |
| Detecção de reuso revogando a família inteira | Mitiga roubo de token de verdade | Exige distinguir reuso malicioso de corrida legítima entre abas, e derrubar sessões válidas | Adiado: a rotação já limita a janela; a família fica gravada para isso ser possível depois |

## Consequências

### Positivas

- A janela de um refresh token vazado cai de 30 dias para o intervalo até a próxima renovação.
- `familia_id` e `substituido_por` passam a ser escritos, deixando a cadeia de rotação auditável.
- A sessão do frontend sobrevive a reload: `useSessao` reidrata pelo cookie `httpOnly`.
- Fecha o bloqueante nº 3 da revisão de 2026-09-10.

### Negativas

- Duas abas renovando ao mesmo tempo podem fazer a perdedora receber `400` e cair para `anonimo`.
  Não há coordenação entre abas hoje.
- Cada renovação escreve duas linhas em `refresh_token`; a tabela cresce e ainda **não há rotina de
  limpeza** de tokens revogados ou expirados.

### Neutras

- A detecção de reuso (revogar a família inteira ao ver um token já rotacionado) fica registrada
  como possível evolução, não como parte desta decisão.
- O access token continua sem claim de papel: autorização segue sendo lida de `perfil`.

## Impacto na Arquitetura

Esta decisão altera `docs/arquitetura-sistema.json`?

- [x] Sim — campos alterados: `security_configuration.token_renewal` (novo),
      `security_configuration.access_token_ttl`
- [ ] Não

## Referências

- [ADR-0006](0006-remover-supabase-infraestrutura-propria.md) — identidade própria, origem desta cadeia
- [ADR-0007](0007-chave-primaria-mista.md) — `usuario.id` é `BIGINT`, e é ele que vai no `sub`
- [Relatório 2026-09-05](../relatorios/2026-09-05-refresh-token-login-e-jwt-encoder.md) — onde a estratégia opaca + SHA-256 nasceu sem registro
- [Relatório 2026-09-10](../relatorios/2026-09-10-revisao-autenticacao-e-planejamento-tela-login.md) — revisão que apontou o bloqueante
- [RFC 9700 §4.14](https://www.rfc-editor.org/rfc/rfc9700.html) — recomendação de rotação e detecção de reuso
