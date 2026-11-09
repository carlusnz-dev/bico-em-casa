ALTER TABLE usuario ADD COLUMN versao_sessao BIGINT NOT NULL DEFAULT 0;

-- Antes desta migration, o cadastro deixava ativo=false e a autenticação ignorava o campo.
-- Preserva o acesso das contas cadastradas antes de a suspensão passar a ser aplicada.
UPDATE usuario SET ativo = true WHERE ativo = false;
