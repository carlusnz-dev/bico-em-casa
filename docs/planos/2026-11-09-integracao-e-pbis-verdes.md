# Plano de integração e fechamento dos PBIs verdes

## Ordem de entrega

1. Corrigir o cadastro público de administrador e validar as duas entradas de cadastro.
2. Integrar `main` em uma branch a partir de `develop`. Preservar chat, transições da contratação e painel de moderação. Resolver o conflito de Meus serviços e conferir o índice de ADRs.
3. Completar os PBIs de contas, serviços e portfólio. Rodar os checks em cada PR.
4. Subir o conjunto em localhost e verificar os fluxos com contas de teste no navegador.

## Back-end

- Bloquear `ADMIN` no cadastro público comum e no legado. Criar administradores somente em rota autenticada de administração. Documentar a criação do primeiro administrador.
- Listar contas e registrar motivo e autor de suspensão e reativação. Revogar a sessão antiga e rejeitar login e renovação enquanto a conta estiver suspensa.
- Revisar novas ofertas na fila administrativa. Uma oferta nova entra ativa. Reprovação com motivo desativa a oferta e avisa o profissional. O reenvio fica inativo até aprovação.
- Calcular orçamento médio do profissional com os preços das próprias ofertas ativas, agrupados por unidade. Calcular separadamente a média pública por categoria e unidade.
- Aplicar filtros públicos de cidade, categoria e faixa de preço. Reaproveitar o armazenamento do portfólio para fotos e vídeos.
- Permitir avaliação somente pelo cliente titular de uma contratação concluída.

## Front-end

- Adicionar tela de usuários no menu administrativo com cadastro de administrador, listagem, suspensão e reativação.
- Acrescentar a fila ao painel de serviços e o reenvio à tela do profissional.
- Mostrar estimativa média na área do profissional e média por categoria no catálogo. Manter os filtros ao pesquisar e trocar de página.
- Exibir a galeria do portfólio e permitir upload pelo profissional. Mostrar a nota média no painel do profissional.
- Manter os avisos de moderação junto ao destaque de serviços ativos e liberar a avaliação após a conclusão da contratação.

## Aceite

- Cadastro público recusa `ADMIN`. Um administrador autenticado consegue criar outro. Uma sessão suspensa deixa de funcionar e a conta reativada consegue autenticar de novo.
- Cadastro de oferta, publicação, reprovação, reenvio e aprovação funcionam com as regras da fila.
- Média e orçamento usam preços da mesma unidade. Filtros de cidade, categoria e faixa funcionam no catálogo.
- A aplicação sobe com banco vazio. Validar também bancos que já rodaram migrations de apenas uma das duas branches. Para essa atualização, aplicar uma vez com `SPRING_FLYWAY_OUT_OF_ORDER=true`, depois voltar à configuração padrão. As migrations publicadas mantêm nome e checksum.
- Rodar Maven com PostgreSQL via Testcontainers, Vitest e verificação de tipos. Validar navegação e criação de dados em localhost.
