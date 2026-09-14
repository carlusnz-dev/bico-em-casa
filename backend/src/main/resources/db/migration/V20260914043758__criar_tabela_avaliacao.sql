CREATE TABLE avaliacao
(
    id                 UUID                        NOT NULL,
    contratacao_id     UUID                        NOT NULL,
    autor_perfil_id    UUID                        NOT NULL,
    avaliado_perfil_id UUID                        NOT NULL,
    nota               INTEGER                     NOT NULL,
    comentario         VARCHAR(1000),
    criado_em          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    atualizado_em      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_avaliacao PRIMARY KEY (id),
    CONSTRAINT uq_avaliacao_contratacao_id UNIQUE (contratacao_id),
    CONSTRAINT ck_avaliacao_nota_valida CHECK (nota BETWEEN 1 AND 5)
);

ALTER TABLE avaliacao
    ADD CONSTRAINT fk_avaliacao_contratacao FOREIGN KEY (contratacao_id) REFERENCES contratacao (id);

ALTER TABLE avaliacao
    ADD CONSTRAINT fk_avaliacao_autor FOREIGN KEY (autor_perfil_id) REFERENCES perfil (id) ON DELETE CASCADE;

ALTER TABLE avaliacao
    ADD CONSTRAINT fk_avaliacao_avaliado FOREIGN KEY (avaliado_perfil_id) REFERENCES perfil (id) ON DELETE CASCADE;

CREATE INDEX idx_avaliacao_avaliado_perfil_id ON avaliacao (avaliado_perfil_id);
