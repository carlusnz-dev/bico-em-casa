package br.com.bicoemcasa.api.modulos.denuncias.repository;

import br.com.bicoemcasa.api.modulos.denuncias.models.Denuncia;
import br.com.bicoemcasa.api.modulos.denuncias.models.StatusDenuncia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DenunciaRepository extends JpaRepository<Denuncia, UUID> {

    Page<Denuncia> findByAutorPerfilId(UUID autorPerfilId, Pageable pageable);

    Page<Denuncia> findByStatus(StatusDenuncia status, Pageable pageable);
}
