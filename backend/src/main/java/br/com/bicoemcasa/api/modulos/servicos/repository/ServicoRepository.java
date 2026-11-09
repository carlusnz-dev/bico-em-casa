package br.com.bicoemcasa.api.modulos.servicos.repository;

import br.com.bicoemcasa.api.modulos.servicos.dto.PrecoMedioResponse;
import br.com.bicoemcasa.api.modulos.servicos.models.Servico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, UUID>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<Servico> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Servico s where s.id = :id")
    java.util.Optional<Servico> buscarParaAtualizar(UUID id);
    Page<Servico> findByPerfilId(UUID perfilId, Pageable pageable);

    String ULTIMA_ACAO_REVISAO = """
            select l.acao from log_acao l
            where l.alvo_tipo = 'servico' and l.alvo_id = cast(s.id as text)
            and l.acao in ('APROVAR_SERVICO', 'REPROVAR_SERVICO', 'REENVIAR_SERVICO', 'DISPENSAR_REVISAO_SERVICO')
            order by l.criado_em desc, l.id desc limit 1
            """;
    String PENDENTES = " from servico s where coalesce((" + ULTIMA_ACAO_REVISAO
            + "), 'REENVIAR_SERVICO') = 'REENVIAR_SERVICO'";

    @Query(value = "select (" + ULTIMA_ACAO_REVISAO + ") from servico s where s.id = :id", nativeQuery = true)
    Optional<String> buscarAcaoRevisao(UUID id);

    default String buscarStatusRevisao(UUID id) {
        return switch (buscarAcaoRevisao(id).orElse("")) {
            case "REPROVAR_SERVICO" -> "REPROVADO";
            case "APROVAR_SERVICO", "DISPENSAR_REVISAO_SERVICO" -> "APROVADO";
            default -> "PENDENTE";
        };
    }

    @Query(value = "select s.*" + PENDENTES + " order by s.criado_em asc, s.id asc",
            countQuery = "select count(*)" + PENDENTES, nativeQuery = true)
    Page<Servico> buscarPendentesDeRevisao(Pageable pagina);

    @Query("""
            select new br.com.bicoemcasa.api.modulos.servicos.dto.PrecoMedioResponse(
                s.unidadePreco, avg(s.precoPrevio), count(s))
            from Servico s join s.tags t
            where s.ativo = true and t.slug = :categoria
            group by s.unidadePreco order by s.unidadePreco
            """)
    List<PrecoMedioResponse> calcularPrecosMediosPorCategoria(String categoria);

    @Query("""
            select new br.com.bicoemcasa.api.modulos.servicos.dto.PrecoMedioResponse(
                s.unidadePreco, avg(s.precoPrevio), count(s))
            from Servico s
            where s.ativo = true and s.perfilId = :perfilId
            group by s.unidadePreco order by s.unidadePreco
            """)
    List<PrecoMedioResponse> calcularOrcamentoMedioPorPerfil(UUID perfilId);

    @Query("SELECT s" +
            " FROM Servico s " +
            " INNER JOIN Contratacao c ON c.servicoId = s.id" +
            " WHERE s.perfilId = :perfilId AND s.ativo = true" +
            " GROUP BY s" +
            " ORDER BY count(c.servicoId) DESC" +
            " LIMIT 5"
    )
    List<Servico> findTop5ByPerfilId(UUID perfilId);
}
