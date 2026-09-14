CREATE TABLE servico (
                         id UUID PRIMARY KEY,
                         perfil_id UUID NOT NULL,
                         titulo VARCHAR(150) NOT NULL,
                         descricao TEXT,
                         preco_previo NUMERIC(10, 2),
                         unidade_preco VARCHAR(50),
                         ativo BOOLEAN NOT NULL DEFAULT TRUE,
                         criado_em TIMESTAMP WITH TIME ZONE NOT NULL,
                         atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL
);