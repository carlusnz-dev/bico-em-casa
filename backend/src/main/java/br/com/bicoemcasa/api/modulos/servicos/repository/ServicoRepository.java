package br.com.bicoemcasa.api.modulos.servicos.repository;

import br.com.bicoemcasa.api.modulos.servicos.models.Servico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, UUID> {
    Page<Servico> findByAtivoTrue(Pageable pageable);
}
