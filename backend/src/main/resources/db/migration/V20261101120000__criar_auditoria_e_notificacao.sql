CREATE TABLE log_acao (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
 autor_usuario_id bigint REFERENCES usuario(id) ON DELETE SET NULL,
 acao varchar(80) NOT NULL, alvo_tipo varchar(40), alvo_id varchar(64),
 detalhes jsonb, ip_origem inet, criado_em timestamptz NOT NULL DEFAULT clock_timestamp()
);
CREATE INDEX idx_log_acao_autor ON log_acao(autor_usuario_id);
CREATE INDEX idx_log_acao_alvo ON log_acao(alvo_tipo,alvo_id,criado_em DESC);
CREATE INDEX idx_log_acao_criado_em ON log_acao(criado_em);
CREATE INDEX idx_log_acao_acao ON log_acao(acao);
CREATE TABLE notificacao (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
 destinatario_perfil_id uuid NOT NULL REFERENCES perfil(id) ON DELETE CASCADE,
 tipo varchar(60) NOT NULL, titulo varchar(150) NOT NULL, mensagem varchar(1000),
 alvo_tipo varchar(40), alvo_id varchar(64), lida_em timestamptz,
 criado_em timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_notificacao_destinatario_lida ON notificacao(destinatario_perfil_id,lida_em);
CREATE UNIQUE INDEX uq_tag_nome_normalizado ON tag(lower(trim(nome)));
