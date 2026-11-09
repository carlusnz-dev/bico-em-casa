ALTER TABLE servico ADD COLUMN motivo_reprovacao VARCHAR(1000);

-- As ofertas anteriores à fila preservam a situação de publicação que já tinham.
INSERT INTO log_acao (acao, alvo_tipo, alvo_id, detalhes)
SELECT 'DISPENSAR_REVISAO_SERVICO', 'servico', id::text,
       '{"motivo":"Oferta cadastrada antes da fila de revisão"}'::jsonb
FROM servico;
