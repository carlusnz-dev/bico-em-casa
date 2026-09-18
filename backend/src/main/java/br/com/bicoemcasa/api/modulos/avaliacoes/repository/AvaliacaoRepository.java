package br.com.bicoemcasa.api.modulos.avaliacoes.repository;

import br.com.bicoemcasa.api.modulos.avaliacoes.models.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, UUID> {

    Optional<Avaliacao> findByContratacaoId(UUID contratacaoId);

    List<Avaliacao> findByAvaliadoPerfilId(UUID avaliadoPerfilId);

    List<Avaliacao> findByAutorPerfilId(UUID autorPerfilId);

    List<Avaliacao> findByContratacaoIdIn(List<UUID> contratacaoIds);

    boolean existsByContratacaoId(UUID contratacaoId);

    @Query("SELECT AVG(a.nota) FROM Avaliacao a WHERE a.avaliadoPerfilId = :avaliadoPerfilId")
    Double calcularMediaPorAvaliado(@Param("avaliadoId") Long avaliadoId);

}
