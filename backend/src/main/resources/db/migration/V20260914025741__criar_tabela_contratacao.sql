CREATE TABLE contratacao
(
    id                UUID                        NOT NULL,
    servico_id        UUID                        NOT NULL,
    cliente_id        UUID                        NOT NULL,
    profissional_id   UUID                        NOT NULL,
    titulo_servico    VARCHAR(120)                NOT NULL,
    preco_servico     NUMERIC(10, 2)              NOT NULL,
    observacao        VARCHAR(1000),
    status            VARCHAR(20)                 NOT NULL,
    criado_em         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    atualizado_em     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_contratacao PRIMARY KEY (id)
);

ALTER TABLE contratacao
    ADD CONSTRAINT fk_contratacao_servico FOREIGN KEY (servico_id) REFERENCES servico (id);

ALTER TABLE contratacao
    ADD CONSTRAINT fk_contratacao_cliente FOREIGN KEY (cliente_id) REFERENCES perfil (id) ON DELETE CASCADE;

ALTER TABLE contratacao
    ADD CONSTRAINT fk_contratacao_profissional FOREIGN KEY (profissional_id) REFERENCES perfil (id) ON DELETE CASCADE;

CREATE INDEX idx_contratacao_cliente_id ON contratacao (cliente_id);
