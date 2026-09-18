package br.com.bicoemcasa.api.modulos.contratacoes.repository;

import br.com.bicoemcasa.api.modulos.contratacoes.models.Contratacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContratacaoRepository extends JpaRepository<Contratacao, UUID> {
    Page<Contratacao> findByClienteId(UUID clienteId, Pageable pageable);
    List<Contratacao> findByServicoId(UUID servicoId);
}
