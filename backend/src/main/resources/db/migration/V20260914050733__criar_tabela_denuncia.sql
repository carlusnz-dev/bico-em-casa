CREATE TABLE denuncia
(
    id                      UUID                        NOT NULL,
    autor_perfil_id         UUID                        NOT NULL,
    denunciado_perfil_id    UUID,
    contratacao_id          UUID,
    alvo_tipo               VARCHAR(20)                 NOT NULL,
    alvo_id                 VARCHAR(64)                 NOT NULL,
    motivo                  VARCHAR(80)                 NOT NULL,
    descricao               VARCHAR(1000),
    fotos                   JSONB,
    status                  VARCHAR(20)                 NOT NULL,
    analisado_por_perfil_id UUID,
    analisado_em            TIMESTAMP WITHOUT TIME ZONE,
    parecer                 VARCHAR(1000),
    criado_em               TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    atualizado_em           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_denuncia PRIMARY KEY (id)
);

ALTER TABLE denuncia
    ADD CONSTRAINT fk_denuncia_autor FOREIGN KEY (autor_perfil_id) REFERENCES perfil (id) ON DELETE CASCADE;

ALTER TABLE denuncia
    ADD CONSTRAINT fk_denuncia_denunciado FOREIGN KEY (denunciado_perfil_id) REFERENCES perfil (id) ON DELETE CASCADE;

ALTER TABLE denuncia
    ADD CONSTRAINT fk_denuncia_analisado_por FOREIGN KEY (analisado_por_perfil_id) REFERENCES perfil (id) ON DELETE SET NULL;

ALTER TABLE denuncia
    ADD CONSTRAINT fk_denuncia_contratacao FOREIGN KEY (contratacao_id) REFERENCES contratacao (id) ON DELETE SET NULL;

CREATE INDEX idx_denuncia_status ON denuncia (status);
CREATE INDEX idx_denuncia_alvo ON denuncia (alvo_tipo, alvo_id);
CREATE INDEX idx_denuncia_autor ON denuncia (autor_perfil_id);
CREATE INDEX idx_denuncia_denunciado ON denuncia (denunciado_perfil_id);
CREATE INDEX idx_denuncia_contratacao ON denuncia (contratacao_id);
