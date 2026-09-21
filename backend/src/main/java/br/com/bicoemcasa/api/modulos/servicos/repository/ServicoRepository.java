package br.com.bicoemcasa.api.modulos.servicos.repository;

import br.com.bicoemcasa.api.modulos.servicos.models.Servico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, UUID> {
    Page<Servico> findByAtivoTrue(Pageable pageable);
    Page<Servico> findByPerfilId(UUID perfilId, Pageable pageable);

    @Query("SELECT s" +
            " FROM Servico s " +
            " INNER JOIN Contratacao c ON c.servicoId = s.id" +
            " WHERE s.perfilId = :perfilId" +
            " GROUP BY s" +
            " ORDER BY count(c.servicoId) DESC" +
            " LIMIT 5"
    )
    List<Servico> findTop5ByPerfilId(UUID perfilId);
}
