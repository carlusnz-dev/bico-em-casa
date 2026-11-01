package br.com.bicoemcasa.api.core;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import java.util.Map;

@Component
public class RegistroAuditoria {
    private final JdbcClient jdbc;
    private final ObjectMapper mapper;
    public RegistroAuditoria(JdbcClient jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }
    public String motivoDesativacao(String id) {
        return jdbc.sql("""
            select case when acao='DESATIVAR_SERVICO' then detalhes->>'motivo' else null end
            from log_acao where alvo_tipo='servico' and alvo_id=:id
            and acao in ('DESATIVAR_SERVICO','REATIVAR_SERVICO')
            order by criado_em desc, id desc limit 1
            """).param("id",id).query(String.class).optional().orElse(null);
    }
    public void registrar(Long autor, String acao, String tipo, String alvo, String ip,
                          String motivo, Map<String, Object> anterior, Map<String, Object> novo) {
        jdbc.sql("""
            INSERT INTO log_acao (autor_usuario_id, acao, alvo_tipo, alvo_id, ip_origem, detalhes)
            VALUES (:autor, :acao, :tipo, :alvo, cast(:ip as inet), cast(:detalhes as jsonb))
            """).param("autor", autor).param("acao", acao).param("tipo", tipo).param("alvo", alvo)
            .param("ip", ip).param("detalhes", mapper.writeValueAsString(Map.of(
                "motivo", motivo, "anterior", anterior, "novo", novo))).update();
    }
}
