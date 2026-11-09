package br.com.bicoemcasa.api.modulos.usuarios.repository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import br.com.bicoemcasa.api.modulos.usuarios.dto.AvisoModeracaoResponse;
import java.util.*;
@Repository
public class NotificacaoRepository {
    private final JdbcClient jdbc;
    public NotificacaoRepository(JdbcClient jdbc) { this.jdbc = jdbc; }
    public void criar(UUID perfil, UUID servico, String titulo, String mensagem) {
        jdbc.sql("""
            insert into notificacao(destinatario_perfil_id,tipo,titulo,mensagem,alvo_tipo,alvo_id)
            values (:perfil,'MODERACAO_SERVICO',:titulo,:mensagem,'servico',:servico)
            """).param("perfil",perfil).param("servico",servico.toString())
                .param("titulo",titulo).param("mensagem",mensagem).update();
    }
    public List<AvisoModeracaoResponse> listar(UUID perfil) {
        return jdbc.sql("""
            select id,titulo,mensagem,alvo_id from notificacao where destinatario_perfil_id=:perfil
            order by criado_em desc limit 50
            """).param("perfil",perfil).query((rs,n) -> new AvisoModeracaoResponse(
                rs.getObject("id",UUID.class),rs.getString("mensagem"),rs.getString("alvo_id"),
                rs.getString("titulo"))).list();
    }
}
