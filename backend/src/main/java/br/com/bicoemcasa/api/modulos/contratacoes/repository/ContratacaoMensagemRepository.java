package br.com.bicoemcasa.api.modulos.contratacoes.repository;

import br.com.bicoemcasa.api.modulos.contratacoes.models.ContratacaoMensagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContratacaoMensagemRepository extends JpaRepository<ContratacaoMensagem, UUID> {
    List<ContratacaoMensagem> findByContratacaoIdOrderByCriadoEmAsc(UUID contratacaoId);
}
