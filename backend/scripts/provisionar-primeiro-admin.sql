\set ON_ERROR_STOP on
\prompt 'ID da conta dedicada: ' usuario_id
\prompt 'E-mail exato da conta dedicada: ' email

BEGIN;

-- Login, renovação e suspensão também adquirem primeiro o lock da conta.
SELECT id AS id_validado
FROM usuario
WHERE id = :'usuario_id'::bigint AND email = :'email' AND ativo
  AND (SELECT count(*) FROM usuario WHERE email = :'email') = 1
FOR UPDATE
\gset

LOCK TABLE perfil IN SHARE ROW EXCLUSIVE MODE;

WITH promovido AS (
    UPDATE perfil
    SET tipo = 'ADMIN', atualizado_em = now()
    WHERE usuario_id = :id_validado
      AND tipo = 'CLIENTE'
      AND (SELECT count(*) FROM perfil WHERE usuario_id = :id_validado) = 1
      AND NOT EXISTS (SELECT 1 FROM perfil WHERE tipo = 'ADMIN')
    RETURNING usuario_id
), sessao_invalidada AS (
    UPDATE usuario
    SET versao_sessao = versao_sessao + 1
    WHERE id IN (SELECT usuario_id FROM promovido)
    RETURNING id
), refresh_revogados AS (
    UPDATE refresh_token
    SET revogado_em = now()
    WHERE usuario_id IN (SELECT id FROM sessao_invalidada) AND revogado_em IS NULL
)
INSERT INTO log_acao (autor_usuario_id, acao, alvo_tipo, alvo_id, detalhes)
SELECT id, 'PROVISIONAR_PRIMEIRO_ADMIN', 'usuario', id::text,
       '{"motivo":"Provisionamento operacional","anterior":{"papel":"CLIENTE"},"novo":{"papel":"ADMIN"}}'::jsonb
FROM sessao_invalidada
RETURNING alvo_id AS id_provisionado
\gset

COMMIT;
\echo Primeiro administrador provisionado no usuário :id_provisionado. Faça um novo login.
