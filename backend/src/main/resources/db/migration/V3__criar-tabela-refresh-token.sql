CREATE TABLE refresh_token
(
    id              UUID         NOT NULL,
    usuario_id      BIGINT       NOT NULL,
    hash_token      VARCHAR(255) NOT NULL,
    familia_id      UUID         NOT NULL,
    substituido_por UUID,
    expira_em       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    revogado_em     TIMESTAMP WITHOUT TIME ZONE,
    ip_origem       VARCHAR(255),
    criado_em       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_refresh_token PRIMARY KEY (id)
);

ALTER TABLE refresh_token
    ADD CONSTRAINT uc_refresh_token_substituido_por UNIQUE (substituido_por);

ALTER TABLE refresh_token
    ADD CONSTRAINT uq_refresh_token_hash_token UNIQUE (hash_token);

CREATE INDEX idx_refresh_token_familia_id ON refresh_token (familia_id);

CREATE INDEX idx_refresh_token_usuario_id ON refresh_token (usuario_id);