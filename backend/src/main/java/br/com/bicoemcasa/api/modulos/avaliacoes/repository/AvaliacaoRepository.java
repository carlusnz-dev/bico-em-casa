package br.com.bicoemcasa.api.modulos.avaliacoes.repository;

import br.com.bicoemcasa.api.modulos.avaliacoes.models.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, UUID> {

    Optional<Avaliacao> findByContratacaoId(UUID contratacaoId);

    List<Avaliacao> findByAvaliadoPerfilId(UUID avaliadoPerfilId);

    List<Avaliacao> findByAutorPerfilId(UUID autorPerfilId);

    boolean existsByContratacaoId(UUID contratacaoId);
}
