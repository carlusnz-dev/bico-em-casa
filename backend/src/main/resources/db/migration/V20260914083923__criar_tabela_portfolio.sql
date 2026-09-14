CREATE TABLE portfolio
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY,
    perfil_id     UUID                        NOT NULL,
    titulo        VARCHAR(120)                NOT NULL,
    descricao     VARCHAR(1000),
    slug_url      VARCHAR(120)                NOT NULL,
    foto_capa_url VARCHAR(500),
    criado_em     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    atualizado_em TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_portfolio PRIMARY KEY (id)
);

ALTER TABLE portfolio
    ADD CONSTRAINT fk_portfolio_perfil FOREIGN KEY (perfil_id) REFERENCES perfil (id) ON DELETE CASCADE;

ALTER TABLE portfolio
    ADD CONSTRAINT uq_portfolio_perfil_id UNIQUE (perfil_id);

ALTER TABLE portfolio
    ADD CONSTRAINT uq_portfolio_slug_url UNIQUE (slug_url);
