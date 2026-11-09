CREATE TABLE contratacao_mensagem
(
    id              UUID                        NOT NULL,
    contratacao_id  UUID                        NOT NULL,
    autor_perfil_id UUID                        NOT NULL,
    autor_nome      VARCHAR(100)                NOT NULL,
    conteudo        VARCHAR(1000)               NOT NULL,
    criado_em       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_contratacao_mensagem PRIMARY KEY (id)
);

ALTER TABLE contratacao_mensagem
    ADD CONSTRAINT fk_contratacao_mensagem_contratacao FOREIGN KEY (contratacao_id) REFERENCES contratacao (id) ON DELETE CASCADE;

ALTER TABLE contratacao_mensagem
    ADD CONSTRAINT fk_contratacao_mensagem_autor FOREIGN KEY (autor_perfil_id) REFERENCES perfil (id) ON DELETE CASCADE;

CREATE INDEX idx_contratacao_mensagem_contratacao_id ON contratacao_mensagem (contratacao_id, criado_em);
