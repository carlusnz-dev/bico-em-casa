package br.com.bicoemcasa.api.modulos.avaliacoes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AvaliacaoRepository extends JpaRepository <Avaliacao , UUID> {


    Optional<Avaliacao> findByContratacaoId(UUID contratacaoId);
    List<Avaliacao> findByAvaliadoId (Long avaliadoId);
    List<Avaliacao> findByAutorId(Long autorId);
    boolean existsByContratacaoId(UUID contratacaoId);


}
