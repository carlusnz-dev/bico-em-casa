# ADR-0013 — Auditoria e moderação de serviços

- Status: Aceito
- Data: 2026-11-01
- Escopo: RF025, RF026 e RF027

## Contexto

HU11–HU13 exigem auditoria, bloqueio de reativação pelo dono e notificação da correção.
As tabelas log_acao e notificacao estavam previstas no modelo, sem implementação. O usuário
autorizou código de aplicação, testes e migrations nesta sessão e aprovou endpoint dedicado,
slug imutável e motivo obrigatório para a correção.

## Decisão

Materializar as duas tabelas com Flyway. Registrar auditoria por JdbcClient em RegistroAuditoria,
na raiz de core, respeitando a localização real do núcleo e a regra de pasta para mais de um
arquivo. Não mover o núcleo inteiro nem renomear módulos nesta feature.

Notificações de moderação pertencem a usuarios e são acessadas pelo contrato PerfilService.
Não há novo módulo, envio de e-mail ou implementação geral de notificações RF012 nesta entrega.
O profissional recebe avisos persistidos na tela de seus serviços. A mensagem aceita 1000
caracteres, para preservar o motivo integral da correção.

A moderação usa endpoints próprios sob /api/admin, papel lido do banco e transação única para
alteração, auditoria e notificação. O IP é o endereço remoto da conexão; cabeçalhos enviados
pelo cliente não são confiados como endereço de origem.

Usar a última ação DESATIVAR_SERVICO/REATIVAR_SERVICO de log_acao para determinar bloqueio e motivo.
Bloquear a linha de serviço nas alterações de dono e administrador para serializar reativações.
Bloquear categorias durante vínculos e manutenção; índice único normalizado protege renomeações
concorrentes. Slug permanece imutável. A busca usa normalização sem extensão PostgreSQL.

## Consequências

Não é necessária coluna adicional em servico. O log torna-se necessário também para determinar
bloqueio, portanto não pode ser purgado sem preservar as últimas ações de situação. O acesso de
leitura do log é interno e restrito ao motivo de moderação. A listagem do profissional pode fazer
uma leitura do log por item; o índice por alvo/data mantém essa consulta localizada.

Notificações são persistidas e exibidas; marcar como lida e paginação de notificações ficam fora
do escopo. A consulta de avisos retorna os 50 mais recentes. Auditoria não possui API de alteração
ou exclusão. A credencial operacional do banco ainda deve seguir sua política de acesso.
