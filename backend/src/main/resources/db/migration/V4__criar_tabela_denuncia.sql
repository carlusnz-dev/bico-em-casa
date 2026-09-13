CREATE TYPE tipo_alvo_denuncia AS ENUM ('SERVICO', 'PERFIL' , 'AVALIACAO') ;

CREATE TYPE status_denuncia AS ENUM ('PENDENTE' ,'PROCEDENTE' ,'IMPROCEDENTE');


CREATE TABLE denuncia(
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    autor_perfil_id      BIGINT          NOT NULL , 
    denunciado_perfil_id  BIGINT, 
    contratacao_id      UUID, 
    alvo_tipo           tipo_alvo_denuncia  NOT NULL,
    alvo_id             VARCHAR(64)  NOT NULL,
    motivo              VARCHAR(80)  NOT NULL,
    descricao           VARCHAR(1000)  ,
    fotos               JSONB  ,
    status              status_denuncia NOT NULL DEFAULT 'PENDENTE',
    analisado_por_perfil_id     BIGINT, 
    analisado_em        TIMESTAMPTZ , 
    parecer             VARCHAR(1000),
    criado_em           TIMESTAMPTZ     NOT NULL DEFAULT now(), 
    atualizado_em       TIMESTAMPTZ     NOT NULL DEFAULT now()
);    

CREATE INDEX idx_denuncia_status        on denuncia(status); 
CREATE INDEX idx_denuncia_alvo          on denuncia(alvo_tipo,alvo_id); 
CREATE INDEX idx_denuncia_autor         on denuncia(autor_perfil_id); 
CREATE INDEX idx_denuncia_denunciado      on denuncia(denunciado_perfil_id); 
CREATE INDEX idx_denuncia_contratacao   on denuncia(contratacao_id); 