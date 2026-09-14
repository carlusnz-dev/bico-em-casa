CREATE TABLE servico
(
    id            UUID                        NOT NULL,
    perfil_id     UUID                        NOT NULL,
    titulo        VARCHAR(120)                NOT NULL,
    descricao     VARCHAR(1000)               NOT NULL,
    preco_previo  NUMERIC(10, 2)              NOT NULL,
    unidade_preco VARCHAR(20)                 NOT NULL,
    ativo         BOOLEAN                     NOT NULL,
    criado_em     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    atualizado_em TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_servico PRIMARY KEY (id)
);

ALTER TABLE servico
    ADD CONSTRAINT fk_servico_perfil FOREIGN KEY (perfil_id) REFERENCES perfil (id) ON DELETE CASCADE;

CREATE INDEX idx_servico_perfil_id ON servico (perfil_id);
CREATE INDEX idx_servico_ativo ON servico (ativo);

CREATE TABLE tag
(
    id            UUID                        NOT NULL,
    nome          VARCHAR(50)                 NOT NULL,
    slug          VARCHAR(60)                 NOT NULL,
    criado_em     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    atualizado_em TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_tag PRIMARY KEY (id)
);

ALTER TABLE tag
    ADD CONSTRAINT uk_tag_nome UNIQUE (nome);

ALTER TABLE tag
    ADD CONSTRAINT uk_tag_slug UNIQUE (slug);

CREATE TABLE servico_tag
(
    servico_id UUID NOT NULL,
    tag_id     UUID NOT NULL,
    CONSTRAINT pk_servico_tag PRIMARY KEY (servico_id, tag_id)
);

ALTER TABLE servico_tag
    ADD CONSTRAINT fk_servico_tag_servico FOREIGN KEY (servico_id) REFERENCES servico (id) ON DELETE CASCADE;

ALTER TABLE servico_tag
    ADD CONSTRAINT fk_servico_tag_tag FOREIGN KEY (tag_id) REFERENCES tag (id) ON DELETE CASCADE;

CREATE INDEX idx_servico_tag_tag_id ON servico_tag (tag_id);
