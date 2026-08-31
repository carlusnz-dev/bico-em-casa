CREATE TABLE usuario (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome VARCHAR(255),
    email VARCHAR(255) NOT NULL,
    email_verificacao BOOLEAN DEFAULT FALSE,
    hash_senha VARCHAR(255) NOT NULL,
    cpf CHAR(11) UNIQUE NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    ultimo_login TIMESTAMP
    WITH
        TIME ZONE,
        criado_em TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
        atualizado_em TIMESTAMP
    WITH
        TIME ZONE,
        CONSTRAINT uq_usuarios_email UNIQUE (email),
        CONSTRAINT uq_usuarios_cpf UNIQUE (cpf)
);

CREATE INDEX idx_usuarios_email ON usuario (email);

CREATE INDEX idx_usuarios_ativo ON usuario (ativo);