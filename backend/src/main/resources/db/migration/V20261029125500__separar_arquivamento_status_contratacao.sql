ALTER TABLE contratacao
    ADD COLUMN arquivada BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE contratacao
SET arquivada = TRUE
WHERE status = 'ARQUIVADA';

UPDATE contratacao
SET status = 'EM_ANDAMENTO'
WHERE status IN ('ATIVA', 'ARQUIVADA');
